package com.appfood.shared.sync

import com.appfood.shared.api.request.AddHydratationRequest
import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.AddPoidsRequest
import com.appfood.shared.api.request.SyncPushRequest
import com.appfood.shared.data.local.LocalSyncQueueDataSource
import com.appfood.shared.data.remote.SyncApi
import com.appfood.shared.model.SyncStatus
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Orchestre la synchronisation push/pull entre le cache local et le serveur.
 * Triggers : app foreground, connexion restauree, nouvelle entree (si connecte).
 */
class SyncManager(
    private val syncApi: SyncApi,
    private val syncQueueDataSource: LocalSyncQueueDataSource,
    private val connectivityMonitor: ConnectivityMonitor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val MAX_RETRIES = 5L
        private const val ENTITY_JOURNAL = "journal"
        private const val ENTITY_POIDS = "poids"
        private const val ENTITY_HYDRATATION = "hydratation"
    }

    /** Derniere date de sync reussie (stockee en memoire, a persister via SharedPreferences). */
    private var lastSyncTimestamp: String? = null

    /**
     * Demarre l'observation de la connectivite.
     * Quand la connexion est restauree, declenche un push puis un pull.
     */
    fun startObserving() {
        connectivityMonitor.observeConnectivity()
            .onEach { connected ->
                if (connected) {
                    syncAll()
                }
            }
            .launchIn(scope)
    }

    /**
     * Lance un cycle complet de synchronisation : push d'abord, puis pull.
     */
    suspend fun syncAll(): AppResult<Unit> {
        val pushResult = pushPendingEntries()
        if (pushResult is AppResult.Error) return pushResult

        val since = lastSyncTimestamp ?: "1970-01-01T00:00:00Z"
        return pullUpdates(since)
    }

    /**
     * Envoie les entrees en attente vers le serveur.
     * Lit sync_queue ASC, groupe par entity_type, POST /sync/push.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun pushPendingEntries(): AppResult<Unit> {
        val retryableEntries = syncQueueDataSource.findRetryable(MAX_RETRIES)
        if (retryableEntries.isEmpty()) return AppResult.Success(Unit)

        val journalPayloads = mutableListOf<AddJournalEntryRequest>()
        val poidsPayloads = mutableListOf<AddPoidsRequest>()
        val hydratationPayloads = mutableListOf<AddHydratationRequest>()

        for (entry in retryableEntries) {
            try {
                when (entry.entity_type) {
                    ENTITY_JOURNAL -> journalPayloads.add(
                        json.decodeFromString<AddJournalEntryRequest>(entry.payload_json)
                    )
                    ENTITY_POIDS -> poidsPayloads.add(
                        json.decodeFromString<AddPoidsRequest>(entry.payload_json)
                    )
                    ENTITY_HYDRATATION -> hydratationPayloads.add(
                        json.decodeFromString<AddHydratationRequest>(entry.payload_json)
                    )
                }
            } catch (_: Exception) {
                syncQueueDataSource.incrementRetry(entry.id, "Invalid payload JSON")
            }
        }

        val request = SyncPushRequest(
            journalEntries = journalPayloads,
            poidsEntries = poidsPayloads,
            hydratationEntries = hydratationPayloads,
            timestamp = Clock.System.now().toString(),
        )

        return try {
            val response = syncApi.pushSync(request)

            for (entry in retryableEntries) {
                val isConflict = response.conflicts.any { conflict ->
                    conflict.entityType == entry.entity_type && conflict.entityId == entry.entity_id
                }
                val isError = response.errors.any { err ->
                    err.entityType == entry.entity_type && err.entityId == entry.entity_id
                }

                when {
                    isConflict -> {
                        // Server wins — supprimer de la queue, donnees recuperees au pull
                        syncQueueDataSource.deleteById(entry.id)
                    }
                    isError -> {
                        val errorMsg = response.errors
                            .firstOrNull { err -> err.entityType == entry.entity_type && err.entityId == entry.entity_id }
                            ?.error ?: "Unknown error"
                        syncQueueDataSource.incrementRetry(entry.id, errorMsg)
                    }
                    else -> {
                        // Accepte — supprimer de la queue
                        syncQueueDataSource.deleteById(entry.id)
                    }
                }
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(
                code = "SYNC_PUSH_ERROR",
                message = e.message ?: "Failed to push sync entries",
                cause = e,
            )
        }
    }

    /**
     * Recupere les modifications serveur depuis le timestamp donne.
     * Stocke le nouveau timestamp pour le prochain pull.
     *
     * Note: l'upsert des donnees locales (journal, poids, hydratation)
     * est delegue aux repositories respectifs via des callbacks.
     * Au MVP, on stocke simplement le timestamp et les repos
     * font leur propre pull quand necessaire.
     */
    suspend fun pullUpdates(since: String): AppResult<Unit> {
        return try {
            val response = syncApi.pullSync(since)
            // Stocker le nouveau timestamp pour le prochain pull
            lastSyncTimestamp = response.timestamp
            // TODO: Deleguer l'upsert aux repositories (journal, poids, hydratation)
            // pour eviter un couplage fort avec les data sources locales.
            // Les repositories feront leur propre reconciliation.
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(
                code = "SYNC_PULL_ERROR",
                message = e.message ?: "Failed to pull sync updates",
                cause = e,
            )
        }
    }

    /**
     * Ajoute une entree a la sync_queue pour synchronisation ulterieure.
     * Si connecte, declenche immediatement un push.
     */
    @OptIn(ExperimentalTime::class)
    fun enqueue(
        entityType: String,
        entityId: String,
        action: String,
        payloadJson: String,
    ) {
        val entry = com.appfood.shared.db.Sync_queue(
            id = "${entityType}_${entityId}_${Clock.System.now().toEpochMilliseconds()}",
            entity_type = entityType,
            entity_id = entityId,
            action = action,
            payload_json = payloadJson,
            created_at = Clock.System.now().toEpochMilliseconds(),
            retry_count = 0L,
            last_error = null,
        )
        syncQueueDataSource.insert(entry)

        if (connectivityMonitor.isConnected()) {
            scope.launch { pushPendingEntries() }
        }
    }
}
