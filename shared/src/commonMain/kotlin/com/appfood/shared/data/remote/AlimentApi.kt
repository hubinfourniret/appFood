package com.appfood.shared.data.remote

import com.appfood.shared.api.response.AlimentResponse
import com.appfood.shared.api.response.SearchAlimentResponse
import io.ktor.client.call.body

/**
 * Client API for aliment endpoints (/api/v1/aliments).
 * All endpoints require authentication.
 */
class AlimentApi(private val apiClient: ApiClient) {

    suspend fun search(
        query: String,
        regime: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): SearchAlimentResponse {
        val params = buildString {
            append("/api/v1/aliments/search?q=$query")
            if (regime != null) append("&regime=$regime")
            if (page != null) append("&page=$page")
            if (size != null) append("&size=$size")
        }
        return apiClient.getRequest(params).body()
    }

    suspend fun getById(id: String): AlimentResponse {
        return apiClient.getRequest("/api/v1/aliments/$id").body()
    }

    suspend fun getByBarcode(code: String): AlimentResponse {
        return apiClient.getRequest("/api/v1/aliments/barcode/$code").body()
    }
}
