package com.appfood.shared.data.remote

import com.appfood.shared.api.request.CreatePortionRequest
import com.appfood.shared.api.request.UpdatePortionRequest
import com.appfood.shared.api.response.PortionListResponse
import com.appfood.shared.api.response.PortionResponse
import io.ktor.client.call.body
import io.ktor.http.encodeURLParameter

/**
 * Client API for portion endpoints (/api/v1/portions).
 * All endpoints require authentication.
 */
class PortionApi(private val apiClient: ApiClient) {

    suspend fun getPortions(alimentId: String? = null, alimentNom: String? = null): PortionListResponse {
        val params = buildString {
            append("/api/v1/portions")
            val parts = mutableListOf<String>()
            if (alimentId != null) parts.add("alimentId=$alimentId")
            if (alimentNom != null) parts.add("alimentNom=${alimentNom.encodeURLParameter()}")
            if (parts.isNotEmpty()) {
                append("?")
                append(parts.joinToString("&"))
            }
        }
        return apiClient.getRequest(params).body()
    }

    suspend fun createPortion(request: CreatePortionRequest): PortionResponse {
        return apiClient.postRequest("/api/v1/portions", request).body()
    }

    suspend fun updatePortion(id: String, request: UpdatePortionRequest): PortionResponse {
        return apiClient.putRequest("/api/v1/portions/$id", request).body()
    }

    suspend fun deletePortion(id: String) {
        apiClient.deleteRequest("/api/v1/portions/$id")
    }
}
