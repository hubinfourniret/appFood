package com.appfood.shared.data.remote

import com.appfood.shared.api.request.InitialConsentRequest
import com.appfood.shared.api.request.UpdateConsentRequest
import com.appfood.shared.api.response.ConsentListResponse
import com.appfood.shared.api.response.ConsentResponse
import io.ktor.client.call.body

/**
 * Client API for consent endpoints (/api/v1/consents).
 * All endpoints require authentication.
 */
class ConsentApi(private val apiClient: ApiClient) {

    suspend fun getConsents(): ConsentListResponse {
        return apiClient.getRequest("/api/v1/consents").body()
    }

    suspend fun updateConsent(type: String, request: UpdateConsentRequest): ConsentResponse {
        return apiClient.putRequest("/api/v1/consents/$type", request).body()
    }

    suspend fun saveInitialConsents(request: InitialConsentRequest): ConsentListResponse {
        return apiClient.postRequest("/api/v1/consents/initial", request).body()
    }
}
