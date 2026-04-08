package com.appfood.shared.data.remote

import com.appfood.shared.api.request.CreateRecetteRequest
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
        limit: Int = 20,
    ): RecetteListResponse {
        val params = buildString {
            append("/api/v1/recettes?page=$page&limit=$limit")
            if (regime != null) append("&regime=$regime")
            if (typeRepas != null) append("&typeRepas=$typeRepas")
            if (sort != null) append("&sort=$sort")
            if (query != null) append("&query=$query")
        }
        return apiClient.getRequest(params).body()
    }

    suspend fun getRecette(id: String): RecetteSummaryResponse {
        return apiClient.getRequest("/api/v1/recettes/$id").body()
    }

    suspend fun createRecette(request: CreateRecetteRequest): RecetteDetailResponse {
        return apiClient.postRequest("/api/v1/recettes", request).body()
    }
}
