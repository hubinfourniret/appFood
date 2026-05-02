package com.appfood.shared.data.remote

import com.appfood.shared.api.request.CreateProfileRequest
import com.appfood.shared.api.request.UpdatePreferencesRequest
import com.appfood.shared.api.request.UpdateProfileRequest
import com.appfood.shared.api.request.UpdateSocialProfileRequest
import com.appfood.shared.api.response.HandleAvailabilityResponse
import com.appfood.shared.api.response.PreferencesResponse
import com.appfood.shared.api.response.ProfileResponse
import com.appfood.shared.api.response.UserExportResponse
import com.appfood.shared.api.response.UserProfileResponse
import io.ktor.client.call.body
import io.ktor.http.encodeURLParameter

/**
 * Client API for user endpoints (/api/v1/users).
 * All endpoints require authentication.
 */
class UserApi(private val apiClient: ApiClient) {

    suspend fun getMe(): UserProfileResponse {
        return apiClient.getRequest("/api/v1/users/me").body()
    }

    suspend fun createProfile(request: CreateProfileRequest): ProfileResponse {
        return apiClient.postRequest("/api/v1/users/me/profile", request).body()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): ProfileResponse {
        return apiClient.putRequest("/api/v1/users/me/profile", request).body()
    }

    suspend fun updatePreferences(request: UpdatePreferencesRequest): PreferencesResponse {
        return apiClient.putRequest("/api/v1/users/me/preferences", request).body()
    }

    suspend fun exportData(): UserExportResponse {
        return apiClient.getRequest("/api/v1/users/me/export").body()
    }

    // TACHE-600 : profil social
    suspend fun checkHandleAvailable(handle: String): HandleAvailabilityResponse {
        val encoded = handle.encodeURLParameter()
        return apiClient.getRequest("/api/v1/users/handle-available?handle=$encoded").body()
    }

    suspend fun updateSocialProfile(request: UpdateSocialProfileRequest): UserProfileResponse {
        return apiClient.putRequest("/api/v1/users/me/social", request).body()
    }
}
