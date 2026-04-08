package com.appfood.shared.data.remote

import com.appfood.shared.api.response.FaqListResponse
import io.ktor.client.call.body

/**
 * Client API for support endpoints (/api/v1/support).
 * FAQ endpoint does not require authentication.
 */
class SupportApi(private val apiClient: ApiClient) {

    suspend fun getFaq(): FaqListResponse {
        return apiClient.getRequest("/api/v1/support/faq").body()
    }
}
