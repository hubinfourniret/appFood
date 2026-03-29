package com.appfood.shared.data.remote

import com.appfood.shared.api.response.RecommandationAlimentListResponse
import io.ktor.client.call.body

/**
 * Client API for recommendation endpoints (/api/v1/recommandations).
 */
class RecommandationApi(private val apiClient: ApiClient) {

    suspend fun getAlimentRecommandations(date: String, limit: Int = 10): RecommandationAlimentListResponse {
        return apiClient.getRequest("/api/v1/recommandations/aliments?date=$date&limit=$limit").body()
    }
}
