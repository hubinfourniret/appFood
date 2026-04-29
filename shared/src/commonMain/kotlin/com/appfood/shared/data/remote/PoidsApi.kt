package com.appfood.shared.data.remote

import com.appfood.shared.api.request.AddPoidsRequest
import com.appfood.shared.api.response.AddPoidsResponse
import com.appfood.shared.api.response.PoidsListResponse
import io.ktor.client.call.body

/**
 * Client API for weight endpoints (/api/v1/poids).
 */
class PoidsApi(private val apiClient: ApiClient) {

    suspend fun getHistory(dateFrom: String? = null, dateTo: String? = null): PoidsListResponse {
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

    suspend fun addEntry(request: AddPoidsRequest): AddPoidsResponse {
        return apiClient.postRequest("/api/v1/poids", request).body()
    }
}
