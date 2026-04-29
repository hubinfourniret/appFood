package com.appfood.shared.data.remote

import com.appfood.shared.api.request.CreatePortionRequest
import com.appfood.shared.api.request.UpdatePortionRequest
import com.appfood.shared.api.response.PortionListResponse
import com.appfood.shared.api.response.PortionResponse
import io.ktor.client.call.body

/**
 * Client API for portion endpoints (/api/v1/portions).
 * All endpoints require authentication.
 */
class PortionApi(private val apiClient: ApiClient) {

    suspend fun getPortions(alimentId: String? = null): PortionListResponse {
        val path = if (alimentId != null) "/api/v1/portions?alimentId=$alimentId" else "/api/v1/portions"
        return apiClient.getRequest(path).body()
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
