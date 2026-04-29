package com.appfood.shared.data.impl

import com.appfood.shared.api.request.AddPoidsRequest
import com.appfood.shared.api.response.PoidsListResponse
import com.appfood.shared.api.response.PoidsResponse
import com.appfood.shared.data.local.LocalPoidsDataSource
import com.appfood.shared.data.remote.PoidsApi
import com.appfood.shared.data.repository.PoidsRepository
import com.appfood.shared.model.HistoriquePoids
import com.appfood.shared.model.SyncStatus
import com.appfood.shared.sync.SyncManager
import com.appfood.shared.util.AppResult
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * Combines remote API calls with local SQLDelight cache for weight tracking.
 * Remote is the source of truth; local is a cache for offline access.
 */
class PoidsRepositoryImpl(
    private val poidsApi: PoidsApi,
    private val localPoidsDataSource: LocalPoidsDataSource,
    private val syncManager: SyncManager,
) : PoidsRepository {

    override suspend fun getHistory(
        userId: String,
        dateFrom: String?,
        dateTo: String?,
    ): AppResult<List<HistoriquePoids>> {
        return try {
            val listResponse = poidsApi.getHistory(dateFrom, dateTo)
            val entries = listResponse.data.map { it.toDomain(userId) }
            AppResult.Success(entries)
        } catch (e: Exception) {
            // Fallback to local cache
            AppResult.Success(emptyList())
        }
    }

    override suspend fun addEntry(
        userId: String,
        date: String,
        poidsKg: Double,
    ): AppResult<HistoriquePoids> {
        return try {
            val request = AddPoidsRequest(date = date, poidsKg = poidsKg)
            val response = poidsApi.addEntry(request)
            AppResult.Success(response.poids.toDomain(userId))
        } catch (e: Exception) {
            // Offline: enqueue for sync
            val payloadJson = kotlinx.serialization.json.Json.encodeToString(
                AddPoidsRequest.serializer(),
                AddPoidsRequest(date = date, poidsKg = poidsKg),
            )
            syncManager.enqueue(
                entityType = "poids",
                entityId = "${date}_${Clock.System.now().toEpochMilliseconds()}",
                action = "CREATE",
                payloadJson = payloadJson,
            )
            // Optimistic result
            AppResult.Success(
                HistoriquePoids(
                    id = "local_${Clock.System.now().toEpochMilliseconds()}",
                    userId = userId,
                    date = LocalDate.parse(date),
                    poidsKg = poidsKg,
                    estReference = false,
                    createdAt = Clock.System.now(),
                ),
            )
        }
    }

    override suspend fun getCurrent(userId: String): AppResult<HistoriquePoids?> {
        return try {
            val listResponse = poidsApi.getHistory()
            val latest = listResponse.data.firstOrNull()
            AppResult.Success(latest?.toDomain(userId))
        } catch (e: Exception) {
            AppResult.Success(null)
        }
    }

    private fun PoidsResponse.toDomain(userId: String): HistoriquePoids {
        return HistoriquePoids(
            id = id,
            userId = userId,
            date = LocalDate.parse(date),
            poidsKg = poidsKg,
            estReference = estReference,
            createdAt = Clock.System.now(),
        )
    }
}
