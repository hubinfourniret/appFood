package com.appfood.backend.service

import com.appfood.backend.routes.dto.AddHydratationRequest
import com.appfood.backend.routes.dto.AddJournalEntryRequest
import com.appfood.backend.routes.dto.AddPoidsRequest
import com.appfood.backend.routes.dto.SyncConflict
import com.appfood.backend.routes.dto.SyncError
import com.appfood.backend.routes.dto.SyncPushRequest
import com.appfood.backend.routes.dto.SyncPushResponse
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory

/**
 * Handles offline sync: accepts batched entries from the client
 * and inserts them via the existing services (journal, poids, hydratation).
 * Uses idempotent inserts (client-generated IDs) to avoid duplicates.
 */
class SyncService(
    private val journalService: JournalService,
    private val poidsService: PoidsService,
    private val hydratationService: HydratationService,
) {
    private val logger = LoggerFactory.getLogger("SyncService")

    suspend fun push(userId: String, request: SyncPushRequest): SyncPushResponse {
        var accepted = 0
        val errors = mutableListOf<SyncError>()

        // Process journal entries
        for (entry in request.journalEntries) {
            try {
                journalService.addEntry(
                    userId = userId,
                    idParam = entry.id,
                    dateStr = entry.date,
                    mealTypeStr = entry.mealType,
                    alimentId = entry.alimentId,
                    recetteId = entry.recetteId,
                    quantiteGrammes = entry.quantiteGrammes,
                    nbPortions = entry.nbPortions,
                )
                accepted++
            } catch (e: Exception) {
                logger.warn("Sync push journal entry failed: ${e.message}")
                errors.add(
                    SyncError(
                        entityType = "journal",
                        entityId = entry.id ?: "unknown",
                        error = e.message ?: "Unknown error",
                    ),
                )
            }
        }

        // Process poids entries
        for (entry in request.poidsEntries) {
            try {
                poidsService.addPoids(userId, entry)
                accepted++
            } catch (e: Exception) {
                logger.warn("Sync push poids entry failed: ${e.message}")
                errors.add(
                    SyncError(
                        entityType = "poids",
                        entityId = entry.id ?: "unknown",
                        error = e.message ?: "Unknown error",
                    ),
                )
            }
        }

        // Process hydratation entries
        for (entry in request.hydratationEntries) {
            try {
                hydratationService.addEntry(userId, entry)
                accepted++
            } catch (e: Exception) {
                logger.warn("Sync push hydratation entry failed: ${e.message}")
                errors.add(
                    SyncError(
                        entityType = "hydratation",
                        entityId = entry.id ?: "unknown",
                        error = e.message ?: "Unknown error",
                    ),
                )
            }
        }

        logger.info(
            "SyncService.push userId=$userId accepted=$accepted errors=${errors.size} " +
                "journal=${request.journalEntries.size} poids=${request.poidsEntries.size} " +
                "hydratation=${request.hydratationEntries.size}",
        )

        return SyncPushResponse(
            accepted = accepted,
            conflicts = emptyList(),
            errors = errors,
        )
    }
}
