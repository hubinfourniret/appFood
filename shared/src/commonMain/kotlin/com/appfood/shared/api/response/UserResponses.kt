package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val user: UserResponse,
    val profile: ProfileResponse?,
    val preferences: PreferencesResponse?,
)

@Serializable
data class ProfileResponse(
    val sexe: String,
    val age: Int,
    val poidsKg: Double,
    val tailleCm: Int,
    val regimeAlimentaire: String,
    val niveauActivite: String,
    val onboardingComplete: Boolean,
    val objectifPoids: String?,
    val updatedAt: String,
)

@Serializable
data class PreferencesResponse(
    val alimentsExclus: List<String>,
    val allergies: List<String>,
    val alimentsFavoris: List<String>,
    val updatedAt: String,
)

@Serializable
data class UserExportResponse(
    val user: UserResponse,
    val profile: ProfileResponse?,
    val preferences: PreferencesResponse?,
    val journalEntries: List<JournalEntryResponse>,
    val quotas: List<QuotaResponse>,
    val poidsHistory: List<PoidsResponse>,
    val hydratation: List<HydratationResponse>,
    val consentements: List<ConsentResponse>,
    val exportedAt: String,
)
