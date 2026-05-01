package com.appfood.shared.data.remote

import com.appfood.shared.api.request.CreateRecetteRequest
import com.appfood.shared.api.request.UpdateRecetteRequest
import com.appfood.shared.api.response.RecetteDetailResponse
import com.appfood.shared.api.response.RecetteListResponse
import com.appfood.shared.api.response.RecetteSummaryResponse
import io.ktor.client.call.body

/**
 * Client API for recipe endpoints (/api/v1/recettes).
 */
class RecetteApi(private val apiClient: ApiClient) {

    suspend fun listRecettes(
        regime: String? = null,
        typeRepas: String? = null,
        sort: String? = null,
        query: String? = null,
        page: Int = 1,
        size: Int = 20,
    ): RecetteListResponse {
        val params = buildString {
            append("/api/v1/recettes?page=$page&size=$size")
            if (regime != null) append("&regime=$regime")
            if (typeRepas != null) append("&typeRepas=$typeRepas")
            if (sort != null) append("&sort=$sort")
            if (query != null) append("&q=$query")
        }
        return apiClient.getRequest(params).body()
    }

    suspend fun getRecette(id: String): RecetteDetailResponse {
        return apiClient.getRequest("/api/v1/recettes/$id").body()
    }

    suspend fun createRecette(request: CreateRecetteRequest): RecetteDetailResponse {
        return apiClient.postRequest("/api/v1/recettes", request).body()
    }

    suspend fun listMyRecettes(): RecetteListResponse {
        return apiClient.getRequest("/api/v1/recettes/me").body()
    }

    suspend fun updateRecette(id: String, request: UpdateRecetteRequest): RecetteDetailResponse {
        return apiClient.putRequest("/api/v1/recettes/$id", request).body()
    }

    suspend fun deleteRecette(id: String) {
        apiClient.deleteRequest("/api/v1/recettes/$id")
    }
}
