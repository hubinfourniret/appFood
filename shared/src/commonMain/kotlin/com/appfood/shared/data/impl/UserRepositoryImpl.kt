package com.appfood.shared.data.impl

import com.appfood.shared.api.request.CreateProfileRequest
import com.appfood.shared.api.request.LoginRequest
import com.appfood.shared.api.request.RegisterRequest
import com.appfood.shared.api.request.UpdatePreferencesRequest
import com.appfood.shared.api.request.UpdateProfileRequest
import com.appfood.shared.api.response.AuthResponse
import com.appfood.shared.api.response.PreferencesResponse
import com.appfood.shared.api.response.ProfileResponse
import com.appfood.shared.api.response.UserExportResponse
import com.appfood.shared.api.response.UserProfileResponse
import com.appfood.shared.data.local.LocalUserDataSource
import com.appfood.shared.data.remote.AuthApi
import com.appfood.shared.data.remote.UserApi
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.db.Local_user
import com.appfood.shared.db.Local_user_profile
import com.appfood.shared.db.Local_preferences
import com.appfood.shared.util.AppResult

/**
 * Combines remote API calls with local SQLDelight cache.
 * Remote is the source of truth; local is a cache for offline access.
 */
class UserRepositoryImpl(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val localUserDataSource: LocalUserDataSource,
) : UserRepository {

    override suspend fun register(request: RegisterRequest): AppResult<AuthResponse> {
        return try {
            val response = authApi.register(request)
            cacheUser(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Registration failed", cause = e)
        }
    }

    override suspend fun login(request: LoginRequest): AppResult<AuthResponse> {
        return try {
            val response = authApi.login(request)
            cacheUser(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Login failed", cause = e)
        }
    }

    override suspend fun getCurrentUser(): AppResult<UserProfileResponse> {
        return try {
            val response = userApi.getMe()
            cacheFullProfile(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Failed to get current user", cause = e)
        }
    }

    override suspend fun createProfile(request: CreateProfileRequest): AppResult<ProfileResponse> {
        return try {
            val response = userApi.createProfile(request)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Failed to create profile", cause = e)
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): AppResult<ProfileResponse> {
        return try {
            val response = userApi.updateProfile(request)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Failed to update profile", cause = e)
        }
    }

    override suspend fun updatePreferences(request: UpdatePreferencesRequest): AppResult<PreferencesResponse> {
        return try {
            val response = userApi.updatePreferences(request)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Failed to update preferences", cause = e)
        }
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        return try {
            authApi.deleteAccount()
            localUserDataSource.deleteAll()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Failed to delete account", cause = e)
        }
    }

    override suspend fun exportData(): AppResult<UserExportResponse> {
        return try {
            val response = userApi.exportData()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(message = e.message ?: "Failed to export data", cause = e)
        }
    }

    private fun cacheUser(authResponse: AuthResponse) {
        val user = authResponse.user
        localUserDataSource.insertOrReplace(
            Local_user(
                id = user.id,
                email = user.email,
                nom = user.nom,
                prenom = user.prenom,
                role = "USER",
                created_at = 0L,
                updated_at = 0L,
            )
        )
    }

    private fun cacheFullProfile(response: UserProfileResponse) {
        val user = response.user
        localUserDataSource.insertOrReplace(
            Local_user(
                id = user.id,
                email = user.email,
                nom = user.nom,
                prenom = user.prenom,
                role = "USER",
                created_at = 0L,
                updated_at = 0L,
            )
        )

        response.profile?.let { profile ->
            localUserDataSource.insertOrReplaceProfile(
                Local_user_profile(
                    user_id = user.id,
                    sexe = profile.sexe,
                    age = profile.age.toLong(),
                    poids_kg = profile.poidsKg,
                    taille_cm = profile.tailleCm.toLong(),
                    regime_alimentaire = profile.regimeAlimentaire,
                    niveau_activite = profile.niveauActivite,
                    onboarding_complete = if (profile.onboardingComplete) 1L else 0L,
                    objectif_poids = profile.objectifPoids,
                    updated_at = 0L,
                )
            )
        }

        response.preferences?.let { prefs ->
            localUserDataSource.insertOrReplacePreferences(
                Local_preferences(
                    user_id = user.id,
                    allergies = prefs.allergies.joinToString(","),
                    aliments_exclus = prefs.alimentsExclus.joinToString(","),
                    aliments_favoris = prefs.alimentsFavoris.joinToString(","),
                    notifications_actives = 1L,
                    rappel_hydratation = 1L,
                    heure_rappel_matin = "08:00",
                    heure_rappel_soir = "20:00",
                    updated_at = 0L,
                )
            )
        }
    }
}
