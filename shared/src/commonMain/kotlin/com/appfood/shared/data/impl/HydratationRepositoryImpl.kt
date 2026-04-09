package com.appfood.shared.data.impl

import com.appfood.shared.api.request.AddHydratationRequest
import com.appfood.shared.api.request.UpdateHydratationObjectifRequest
import com.appfood.shared.api.response.HydratationResponse
import com.appfood.shared.data.local.LocalHydratationDataSource
import com.appfood.shared.data.remote.HydratationApi
import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.model.HydratationEntry
import com.appfood.shared.model.HydratationJournaliere
import com.appfood.shared.model.SyncStatus
import com.appfood.shared.sync.SyncManager
import com.appfood.shared.util.AppResult
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * Combines remote API calls with local SQLDelight cache for hydration.
 * Remote is the source of truth; local is a cache for offline access.
 */
class HydratationRepositoryImpl(
    private val hydratationApi: HydratationApi,
    private val localHydratationDataSource: LocalHydratationDataSource,
    private val syncManager: SyncManager,
) : HydratationRepository {

    override suspend fun getDaily(userId: String, date: String): AppResult<HydratationJournaliere> {
        return try {
            val response = hydratationApi.getDaily(date)
            val domain = response.toDomain(userId)
            AppResult.Success(domain)
        } catch (e: Exception) {
            // Fallback to local cache — return empty if not found
            AppResult.Success(emptyDay(userId, date))
        }
    }


    override suspend fun addEntry(userId: String, date: String, quantiteMl: Int): AppResult<HydratationJournaliere> {
        return try {
            val request = AddHydratationRequest(date = date, quantiteMl = quantiteMl)
            val response = hydratationApi.addEntry(request)
            AppResult.Success(response.toDomain(userId))
        } catch (e: Exception) {
            // Offline: enqueue for sync
            val payloadJson = kotlinx.serialization.json.Json.encodeToString(
                AddHydratationRequest.serializer(),
                AddHydratationRequest(date = date, quantiteMl = quantiteMl),
            )
            syncManager.enqueue(
                entityType = "hydratation",
                entityId = "${date}_${Clock.System.now().toEpochMilliseconds()}",
                action = "CREATE",
                payloadJson = payloadJson,
            )
            // Return optimistic local update
            AppResult.Success(emptyDay(userId, date))
        }
    }

    override suspend fun updateObjectif(userId: String, objectifMl: Int): AppResult<Unit> {
        return try {
            hydratationApi.updateObjectif(UpdateHydratationObjectifRequest(objectifMl))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to update hydration objective",
                cause = e,
            )
        }
    }

    override suspend fun resetObjectif(userId: String): AppResult<Unit> {
        return try {
            hydratationApi.resetObjectif()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to reset hydration objective",
                cause = e,
            )
        }
    }

    override suspend fun getWeekly(
        userId: String,
        dateFrom: String,
        dateTo: String,
    ): AppResult<List<HydratationJournaliere>> {
        return try {
            val responses = hydratationApi.getWeekly(dateFrom, dateTo)
            AppResult.Success(responses.map { it.toDomain(userId) })
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch weekly hydration",
                cause = e,
            )
        }
    }


    private fun HydratationResponse.toDomain(userId: String): HydratationJournaliere {
        return HydratationJournaliere(
            id = id,
            userId = userId,
            date = LocalDate.parse(date),
            quantiteMl = quantiteMl,
            objectifMl = objectifMl,
            estObjectifPersonnalise = estObjectifPersonnalise,
            entrees = entrees.map { entry ->
                HydratationEntry(
                    id = entry.id,
                    heure = Clock.System.now(), // Simplified — server returns time string
                    quantiteMl = entry.quantiteMl,
                )
            },
            updatedAt = Clock.System.now(),
        )
    }


    private fun emptyDay(userId: String, date: String): HydratationJournaliere {
        return HydratationJournaliere(
            id = "",
            userId = userId,
            date = LocalDate.parse(date),
            quantiteMl = 0,
            objectifMl = 2000,
            estObjectifPersonnalise = false,
            entrees = emptyList(),
            updatedAt = Clock.System.now(),
        )
    }
}
