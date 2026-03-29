package com.appfood.shared.data.remote

import com.appfood.shared.api.request.UpdateQuotaRequest
import com.appfood.shared.api.response.QuotaListResponse
import com.appfood.shared.api.response.QuotaResponse
import com.appfood.shared.api.response.QuotaStatusListResponse
import io.ktor.client.call.body
import kotlinx.serialization.Serializable

/**
 * Client API for quota endpoints (/api/v1/quotas).
 */
class QuotaApi(private val apiClient: ApiClient) {

    @Serializable
    private data class ApiResponseWrapper(val data: QuotaResponse)

    suspend fun getQuotas(): List<QuotaResponse> {
        val response: QuotaListResponse = apiClient.getRequest("/api/v1/quotas").body()
        return response.data
    }

    suspend fun getQuotaStatus(date: String): QuotaStatusListResponse {
        return apiClient.getRequest("/api/v1/quotas/status?date=$date").body()
    }

    suspend fun updateQuota(nutriment: String, request: UpdateQuotaRequest): QuotaResponse {
        val response: ApiResponseWrapper = apiClient.putRequest("/api/v1/quotas/$nutriment", request).body()
        return response.data
    }

    suspend fun resetQuota(nutriment: String): QuotaResponse {
        val response: ApiResponseWrapper = apiClient.postRequest("/api/v1/quotas/$nutriment/reset", Unit).body()
        return response.data
    }

    suspend fun resetAllQuotas(): List<QuotaResponse> {
        val response: QuotaListResponse = apiClient.postRequest("/api/v1/quotas/reset-all", Unit).body()
        return response.data
    }

    suspend fun recalculate(): List<QuotaResponse> {
        val response: QuotaListResponse = apiClient.postRequest("/api/v1/quotas/recalculate", Unit).body()
        return response.data
    }
}
