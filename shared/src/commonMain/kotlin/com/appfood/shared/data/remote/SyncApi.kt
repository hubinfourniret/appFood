package com.appfood.shared.data.remote

import com.appfood.shared.api.request.SyncPushRequest
import com.appfood.shared.api.response.SyncPullResponse
import com.appfood.shared.api.response.SyncPushResponse
import io.ktor.client.call.body

/**
 * Client API pour les endpoints de synchronisation (/api/v1/sync).
 */
class SyncApi(private val apiClient: ApiClient) {

    suspend fun pushSync(request: SyncPushRequest): SyncPushResponse {
        return apiClient.postRequest("/api/v1/sync/push", request).body()
    }

    suspend fun pullSync(since: String): SyncPullResponse {
        return apiClient.getRequest("/api/v1/sync/pull?since=$since").body()
    }
}
