package com.appfood.shared.sync

import com.appfood.shared.api.response.SyncPushResponse
import com.appfood.shared.db.Sync_queue
import kotlin.math.min
import kotlin.math.pow

/**
 * Resout les conflits et erreurs apres un push sync.
 * Extrait de SyncManager pour faciliter les tests unitaires.
 *
 * Strategie "server wins" : les conflits sont supprimes de la queue locale
 * (les donnees serveur seront recuperees au prochain pull).
 */
object SyncConflictResolver {

    /**
     * Resultat de la resolution pour une entree de la queue.
     */
    sealed interface Resolution {
        /** Entree acceptee ou conflit resolu (server wins) — supprimer de la queue. */
        data class Delete(val entryId: String) : Resolution

        /** Erreur cote serveur — incrementer le retry. */
        data class IncrementRetry(val entryId: String, val errorMessage: String) : Resolution
    }

    /**
     * Determine la resolution pour chaque entree de la queue
     * en fonction de la reponse du push.
     */
    fun resolve(
        entries: List<Sync_queue>,
        response: SyncPushResponse,
    ): List<Resolution> {
        return entries.map { entry ->
            val isConflict = response.conflicts.any { conflict ->
                conflict.entityType == entry.entity_type && conflict.entityId == entry.entity_id
            }
            val isError = response.errors.any { err ->
                err.entityType == entry.entity_type && err.entityId == entry.entity_id
            }

            when {
                isConflict -> Resolution.Delete(entry.id)
                isError -> {
                    val errorMsg = response.errors
                        .firstOrNull { it.entityType == entry.entity_type && it.entityId == entry.entity_id }
                        ?.error ?: "Unknown error"
                    Resolution.IncrementRetry(entry.id, errorMsg)
                }
                else -> Resolution.Delete(entry.id) // accepted
            }
        }
    }

    /**
     * Calcule le delai de backoff en secondes pour un retry donne.
     * Formule : min(2^retryCount, maxBackoffSeconds)
     */
    fun calculateBackoffSeconds(retryCount: Int, maxBackoffSeconds: Long = 32L): Long {
        return min(2.0.pow(retryCount).toLong(), maxBackoffSeconds)
    }

    /**
     * Determine si une entree est encore eligible pour un retry.
     */
    fun isRetryable(entry: Sync_queue, maxRetries: Long): Boolean {
        return entry.retry_count < maxRetries
    }
}
