package com.appfood.shared.data.remote

import com.appfood.shared.api.response.RecommandationAlimentListResponse
import com.appfood.shared.api.response.RecommandationRecetteListResponse
import io.ktor.client.call.body

/**
 * Client API for recommendation endpoints (/api/v1/recommandations).
 */
class RecommandationApi(private val apiClient: ApiClient) {

    suspend fun getAlimentRecommandations(date: String, limit: Int = 10): RecommandationAlimentListResponse {
        return apiClient.getRequest("/api/v1/recommandations/aliments?date=$date&limit=$limit").body()
    }

    suspend fun getRecetteRecommandations(date: String? = null, limit: Int? = null): RecommandationRecetteListResponse {
        val params = buildString {
            append("/api/v1/recommandations/recettes?")
            val parts = mutableListOf<String>()
            if (date != null) parts.add("date=$date")
            if (limit != null) parts.add("limit=$limit")
            append(parts.joinToString("&"))
        }
        return apiClient.getRequest(params).body()
    }
}
