package com.appfood.shared.data.remote

import com.appfood.shared.api.request.AddPoidsRequest
import com.appfood.shared.api.response.PoidsResponse
import io.ktor.client.call.body

/**
 * Client API for weight endpoints (/api/v1/poids).
 */
class PoidsApi(private val apiClient: ApiClient) {

    suspend fun getHistory(dateFrom: String? = null, dateTo: String? = null): List<PoidsResponse> {
        val params = buildString {
            append("/api/v1/poids")
            val parts = mutableListOf<String>()
            if (dateFrom != null) parts.add("dateFrom=$dateFrom")
            if (dateTo != null) parts.add("dateTo=$dateTo")
            if (parts.isNotEmpty()) {
                append("?")
                append(parts.joinToString("&"))
            }
        }
        return apiClient.getRequest(params).body()
    }

    suspend fun addEntry(request: AddPoidsRequest): PoidsResponse {
        return apiClient.postRequest("/api/v1/poids", request).body()
    }

    suspend fun getCurrent(): PoidsResponse {
        return apiClient.getRequest("/api/v1/poids/current").body()
    }
}
