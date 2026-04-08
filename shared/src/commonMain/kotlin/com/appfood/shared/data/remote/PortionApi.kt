package com.appfood.shared.data.remote

import com.appfood.shared.api.response.PortionListResponse
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
}
