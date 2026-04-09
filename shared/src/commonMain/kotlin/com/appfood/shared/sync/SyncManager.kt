package com.appfood.shared.sync

import com.appfood.shared.api.request.AddHydratationRequest
import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.AddPoidsRequest
import com.appfood.shared.api.request.SyncPushRequest
import com.appfood.shared.api.response.HydratationResponse
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.api.response.PoidsResponse
import com.appfood.shared.data.local.LocalHydratationDataSource
import com.appfood.shared.data.local.LocalJournalDataSource
import com.appfood.shared.data.local.LocalPoidsDataSource
import com.appfood.shared.data.local.LocalSyncQueueDataSource
import com.appfood.shared.data.local.LocalUserDataSource
import com.appfood.shared.data.remote.SyncApi
import com.appfood.shared.db.Local_hydratation
import com.appfood.shared.db.Local_journal_entry
import com.appfood.shared.db.Local_poids
import com.appfood.shared.model.SyncStatus
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.math.min
import kotlin.math.pow

/**
 * Sync state observable by the UI layer.
 */
sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Success(val timestamp: String) : SyncState
    data class Error(val message: String) : SyncState
}

/**
 * Orchestre la synchronisation push/pull entre le cache local et le serveur.
 * Triggers : app foreground, connexion restauree, nouvelle entree (si connecte).
 */
class SyncManager(
    private val syncApi: SyncApi,
    private val syncQueueDataSource: LocalSyncQueueDataSource,
    private val connectivityMonitor: ConnectivityMonitor,
    private val localJournalDataSource: LocalJournalDataSource,
    private val localPoidsDataSource: LocalPoidsDataSource,
    private val localHydratationDataSource: LocalHydratationDataSource,
    private val syncPreferences: SyncPreferences,
    private val localUserDataSource: LocalUserDataSource,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)

    /** Observable sync state for UI layer. */
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    companion object {
        private const val MAX_RETRIES = 5L
        private const val ENTITY_JOURNAL = "journal"
        private const val ENTITY_POIDS = "poids"
        private const val ENTITY_HYDRATATION = "hydratation"
        private const val MAX_BACKOFF_SECONDS = 32L
    }

    /** Derniere date de sync reussie, persistee via SyncPreferences. */
    private var lastSyncTimestamp: String?
        get() = syncPreferences.getLastSyncTimestamp()
        set(value) { syncPreferences.setLastSyncTimestamp(value) }

    /**
     * Demarre l'observation de la connectivite.
     * Quand la connexion est restauree, declenche un push puis un pull.
     */
    fun startObserving() {
        connectivityMonitor.observeConnectivity()
            .onEach { connected ->
                if (connected) {
                    scope.launch { syncAll() }
                }
            }
            .launchIn(scope)
    }

    /**
     * Lance un cycle complet de synchronisation : push d'abord, puis pull.
     * Push et pull ont chacun leur propre boucle de retry avec exponential backoff.
     */

    suspend fun syncAll(): AppResult<Unit> {
        _syncState.value = SyncState.Syncing

        // Push avec retry independant
        val pushResult = retryWithBackoff { pushPendingEntries() }
        if (pushResult is AppResult.Error) {
            _syncState.value = SyncState.Error(pushResult.message)
            return pushResult
        }

        // Pull avec retry independant
        val since = lastSyncTimestamp ?: "1970-01-01T00:00:00Z"
        val pullResult = retryWithBackoff { pullUpdates(since) }
        if (pullResult is AppResult.Error) {
            _syncState.value = SyncState.Error(pullResult.message)
            return pullResult
        }

        val timestamp = lastSyncTimestamp ?: Clock.System.now().toString()
        _syncState.value = SyncState.Success(timestamp)
        return AppResult.Success(Unit)
    }

    private suspend fun retryWithBackoff(action: suspend () -> AppResult<Unit>): AppResult<Unit> {
        var retryCount = 0
        while (true) {
            val result = action()
            if (result is AppResult.Success) return result
            retryCount++
            if (retryCount > MAX_RETRIES) return result
            val delaySeconds = min(2.0.pow(retryCount).toLong(), MAX_BACKOFF_SECONDS)
            delay(delaySeconds * 1000)
        }
    }

    /**
     * Envoie les entrees en attente vers le serveur.
     * Lit sync_queue ASC, groupe par entity_type, POST /sync/push.
     */

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

        // Rien a envoyer si tous les payloads sont invalides
        if (journalPayloads.isEmpty() && poidsPayloads.isEmpty() && hydratationPayloads.isEmpty()) {
            return AppResult.Success(Unit)
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
     * Upsert les donnees locales (journal, poids, hydratation) dans les data sources.
     * Stocke le nouveau timestamp pour le prochain pull.
     */
    suspend fun pullUpdates(since: String): AppResult<Unit> {
        return try {
            val response = syncApi.pullSync(since)

            // Upsert journal entries locally
            for (entry in response.journalEntries) {
                upsertJournalEntry(entry)
            }

            // Upsert poids entries locally
            for (entry in response.poidsEntries) {
                upsertPoidsEntry(entry)
            }

            // Upsert hydratation entries locally
            for (entry in response.hydratationEntries) {
                upsertHydratationEntry(entry)
            }

            // Stocker le nouveau timestamp pour le prochain pull
            lastSyncTimestamp = response.timestamp

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
     * Recupere le userId du cache local (premier utilisateur en base).
     * Fallback sur chaine vide si aucun utilisateur n'est connecte.
     */
    private fun getCurrentUserId(): String {
        return localUserDataSource.findAll().firstOrNull()?.id ?: ""
    }

    private fun upsertJournalEntry(entry: JournalEntryResponse) {
        val userId = getCurrentUserId()
        val localEntry = Local_journal_entry(
            id = entry.id,
            user_id = userId,
            aliment_id = entry.alimentId,
            recette_id = entry.recetteId,
            nom = entry.nom,
            meal_type = entry.mealType,
            quantite_grammes = entry.quantiteGrammes,
            nb_portions = entry.nbPortions,
            date = entry.date,
            calories = entry.nutrimentsCalcules.calories,
            proteines = entry.nutrimentsCalcules.proteines,
            glucides = entry.nutrimentsCalcules.glucides,
            lipides = entry.nutrimentsCalcules.lipides,
            fibres = entry.nutrimentsCalcules.fibres,
            sel = entry.nutrimentsCalcules.sel,
            sucres = entry.nutrimentsCalcules.sucres,
            fer = entry.nutrimentsCalcules.fer,
            calcium = entry.nutrimentsCalcules.calcium,
            zinc = entry.nutrimentsCalcules.zinc,
            magnesium = entry.nutrimentsCalcules.magnesium,
            vitamine_b12 = entry.nutrimentsCalcules.vitamineB12,
            vitamine_d = entry.nutrimentsCalcules.vitamineD,
            vitamine_c = entry.nutrimentsCalcules.vitamineC,
            omega_3 = entry.nutrimentsCalcules.omega3,
            omega_6 = entry.nutrimentsCalcules.omega6,
            sync_status = SyncStatus.SYNCED.name,
            created_at = parseTimestampToEpochMs(entry.createdAt),
            updated_at = parseTimestampToEpochMs(entry.updatedAt),
        )
        localJournalDataSource.insertOrReplace(localEntry)
    }

    private fun upsertPoidsEntry(entry: PoidsResponse) {
        val userId = getCurrentUserId()
        val localPoids = Local_poids(
            id = entry.id,
            user_id = userId,
            poids_kg = entry.poidsKg,
            date = entry.date,
            sync_status = SyncStatus.SYNCED.name,
            created_at = parseTimestampToEpochMs(entry.createdAt),
        )
        localPoidsDataSource.insertOrReplace(localPoids)
    }

    private fun upsertHydratationEntry(entry: HydratationResponse) {
        val userId = getCurrentUserId()
        val localHydratation = Local_hydratation(
            id = entry.id,
            user_id = userId,
            date = entry.date,
            objectif_ml = entry.objectifMl.toDouble(),
            total_ml = entry.quantiteMl.toDouble(),
            sync_status = SyncStatus.SYNCED.name,
            updated_at = parseTimestampToEpochMs(lastSyncTimestamp ?: "")
        )
        localHydratationDataSource.insertOrReplace(localHydratation)
    }

    /**
     * Ajoute une entree a la sync_queue pour synchronisation ulterieure.
     * Si connecte, declenche immediatement un push.
     */

    fun enqueue(
        entityType: String,
        entityId: String,
        action: String,
        payloadJson: String,
    ) {
        val now = Clock.System.now()
        val entry = com.appfood.shared.db.Sync_queue(
            id = "${entityType}_${entityId}_${now.toEpochMilliseconds()}",
            entity_type = entityType,
            entity_id = entityId,
            action = action,
            payload_json = payloadJson,
            created_at = now.toEpochMilliseconds(),
            retry_count = 0L,
            last_error = null,
        )
        syncQueueDataSource.insert(entry)

        if (connectivityMonitor.isConnected()) {
            scope.launch { pushPendingEntries() }
        }
    }

    private fun parseTimestampToEpochMs(timestamp: String): Long {
        return try {
            kotlin.time.Instant.parse(timestamp).toEpochMilliseconds()
        } catch (_: Exception) {
            0L
        }
    }
}
