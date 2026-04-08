package com.appfood.shared.data.repository

import com.appfood.shared.api.response.DailySummaryResponse
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.api.response.JournalListResponse
import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.UpdateJournalEntryRequest
import com.appfood.shared.util.AppResult

/**
 * Repository interface for journal-related operations.
 * Manages food journal entries (add, update, delete, list).
 */
interface JournalRepository {

    suspend fun getEntries(date: String? = null, mealType: String? = null): AppResult<JournalListResponse>

    suspend fun addEntry(request: AddJournalEntryRequest): AppResult<JournalEntryResponse>

    suspend fun updateEntry(id: String, request: UpdateJournalEntryRequest): AppResult<JournalEntryResponse>

    suspend fun deleteEntry(id: String): AppResult<Unit>

    suspend fun getDailySummary(date: String? = null): AppResult<DailySummaryResponse>

    suspend fun getRecents(limit: Int? = null): AppResult<JournalListResponse>

    suspend fun getFavoris(): AppResult<JournalListResponse>

    suspend fun addFavori(alimentId: String): AppResult<Unit>

    suspend fun removeFavori(alimentId: String): AppResult<Unit>
}
