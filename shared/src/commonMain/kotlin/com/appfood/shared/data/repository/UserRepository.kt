package com.appfood.shared.data.repository

import com.appfood.shared.api.request.CreateProfileRequest
import com.appfood.shared.api.request.LoginRequest
import com.appfood.shared.api.request.RegisterRequest
import com.appfood.shared.api.request.UpdatePreferencesRequest
import com.appfood.shared.api.request.UpdateProfileRequest
import com.appfood.shared.api.request.UpdateSocialProfileRequest
import com.appfood.shared.api.response.AuthResponse
import com.appfood.shared.api.response.PreferencesResponse
import com.appfood.shared.api.response.ProfileResponse
import com.appfood.shared.api.response.UserExportResponse
import com.appfood.shared.api.response.UserProfileResponse
import com.appfood.shared.util.AppResult

/**
 * Repository interface for user-related operations.
 * Combines remote API calls with local cache (SQLDelight).
 */
interface UserRepository {

    suspend fun register(request: RegisterRequest): AppResult<AuthResponse>

    suspend fun login(request: LoginRequest): AppResult<AuthResponse>

    suspend fun getCurrentUser(): AppResult<UserProfileResponse>

    suspend fun createProfile(request: CreateProfileRequest): AppResult<ProfileResponse>

    suspend fun updateProfile(request: UpdateProfileRequest): AppResult<ProfileResponse>

    suspend fun updatePreferences(request: UpdatePreferencesRequest): AppResult<PreferencesResponse>

    suspend fun logout(): AppResult<Unit>

    suspend fun deleteAccount(): AppResult<Unit>

    suspend fun exportData(): AppResult<UserExportResponse>

    // TACHE-600 : profil social
    suspend fun checkHandleAvailable(handle: String): AppResult<Boolean>

    suspend fun updateSocialProfile(request: UpdateSocialProfileRequest): AppResult<UserProfileResponse>
}
