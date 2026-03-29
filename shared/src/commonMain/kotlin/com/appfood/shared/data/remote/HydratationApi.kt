package com.appfood.shared.data.remote

import com.appfood.shared.api.request.AddHydratationRequest
import com.appfood.shared.api.request.UpdateHydratationObjectifRequest
import com.appfood.shared.api.response.HydratationResponse
import io.ktor.client.call.body

/**
 * Client API for hydration endpoints (/api/v1/hydratation).
 */
class HydratationApi(private val apiClient: ApiClient) {

    suspend fun getDaily(date: String): HydratationResponse {
        return apiClient.getRequest("/api/v1/hydratation/daily?date=$date").body()
    }

    suspend fun addEntry(request: AddHydratationRequest): HydratationResponse {
        return apiClient.postRequest("/api/v1/hydratation", request).body()
    }

    suspend fun updateObjectif(request: UpdateHydratationObjectifRequest): HydratationResponse {
        return apiClient.putRequest("/api/v1/hydratation/objectif", request).body()
    }

    suspend fun resetObjectif(): HydratationResponse {
        return apiClient.postRequest("/api/v1/hydratation/objectif/reset", Unit).body()
    }

    suspend fun getWeekly(dateFrom: String, dateTo: String): List<HydratationResponse> {
        return apiClient.getRequest("/api/v1/hydratation/weekly?dateFrom=$dateFrom&dateTo=$dateTo").body()
    }
}
