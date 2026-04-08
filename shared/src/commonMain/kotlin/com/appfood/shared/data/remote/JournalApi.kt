package com.appfood.shared.data.remote

import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.UpdateJournalEntryRequest
import com.appfood.shared.api.response.DailySummaryResponse
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.api.response.JournalListResponse
import io.ktor.client.call.body

/**
 * Client API for journal endpoints (/api/v1/journal).
 * All endpoints require authentication.
 */
class JournalApi(private val apiClient: ApiClient) {

    suspend fun getEntries(
        date: String? = null,
        mealType: String? = null,
    ): JournalListResponse {
        val params = buildString {
            append("/api/v1/journal")
            val queryParts = mutableListOf<String>()
            if (date != null) queryParts.add("date=$date")
            if (mealType != null) queryParts.add("mealType=$mealType")
            if (queryParts.isNotEmpty()) append("?${queryParts.joinToString("&")}")
        }
        return apiClient.getRequest(params).body()
    }

    suspend fun addEntry(request: AddJournalEntryRequest): JournalEntryResponse {
        return apiClient.postRequest("/api/v1/journal", request).body()
    }

    suspend fun updateEntry(id: String, request: UpdateJournalEntryRequest): JournalEntryResponse {
        return apiClient.putRequest("/api/v1/journal/$id", request).body()
    }

    suspend fun deleteEntry(id: String) {
        apiClient.deleteRequest("/api/v1/journal/$id")
    }

    suspend fun getDailySummary(date: String? = null): DailySummaryResponse {
        val path = if (date != null) "/api/v1/journal/summary?date=$date" else "/api/v1/journal/summary"
        return apiClient.getRequest(path).body()
    }

    suspend fun getRecents(limit: Int? = null): JournalListResponse {
        val path = if (limit != null) "/api/v1/journal/recents?limit=$limit" else "/api/v1/journal/recents"
        return apiClient.getRequest(path).body()
    }

    suspend fun getFavoris(): JournalListResponse {
        return apiClient.getRequest("/api/v1/journal/favoris").body()
    }

    suspend fun addFavori(alimentId: String) {
        apiClient.postRequest("/api/v1/journal/favoris/$alimentId", emptyMap<String, String>())
    }

    suspend fun removeFavori(alimentId: String) {
        apiClient.deleteRequest("/api/v1/journal/favoris/$alimentId")
    }
}
