package com.appfood.shared.data.impl

import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.UpdateJournalEntryRequest
import com.appfood.shared.api.response.DailySummaryResponse
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.api.response.JournalListResponse
import com.appfood.shared.data.remote.JournalApi
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.util.AppResult

/**
 * Fetches journal data from the remote API.
 * Wraps all API calls in try/catch, returning AppResult.
 */
class JournalRepositoryImpl(
    private val journalApi: JournalApi,
) : JournalRepository {

    override suspend fun getEntries(date: String?, mealType: String?): AppResult<JournalListResponse> {
        return try {
            val response = journalApi.getEntries(date, mealType)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch journal entries",
                cause = e,
            )
        }
    }

    override suspend fun addEntry(request: AddJournalEntryRequest): AppResult<JournalEntryResponse> {
        return try {
            val response = journalApi.addEntry(request)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to add journal entry",
                cause = e,
            )
        }
    }

    override suspend fun updateEntry(id: String, request: UpdateJournalEntryRequest): AppResult<JournalEntryResponse> {
        return try {
            val response = journalApi.updateEntry(id, request)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to update journal entry",
                cause = e,
            )
        }
    }

    override suspend fun deleteEntry(id: String): AppResult<Unit> {
        return try {
            journalApi.deleteEntry(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to delete journal entry",
                cause = e,
            )
        }
    }

    override suspend fun getDailySummary(date: String?): AppResult<DailySummaryResponse> {
        return try {
            val response = journalApi.getDailySummary(date)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch daily summary",
                cause = e,
            )
        }
    }

    override suspend fun getRecents(limit: Int?): AppResult<JournalListResponse> {
        return try {
            val response = journalApi.getRecents(limit)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch recent entries",
                cause = e,
            )
        }
    }

    override suspend fun getFavoris(): AppResult<JournalListResponse> {
        return try {
            val response = journalApi.getFavoris()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch favorite entries",
                cause = e,
            )
        }
    }
}
