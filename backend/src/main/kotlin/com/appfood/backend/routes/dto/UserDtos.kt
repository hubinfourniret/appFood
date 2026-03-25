package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateProfileRequest(
    val sexe: String,
    val age: Int,
    val poidsKg: Double,
    val tailleCm: Int,
    val regimeAlimentaire: String,
    val niveauActivite: String,
)

@Serializable
data class UpdateProfileRequest(
    val sexe: String? = null,
    val age: Int? = null,
    val poidsKg: Double? = null,
    val tailleCm: Int? = null,
    val regimeAlimentaire: String? = null,
    val niveauActivite: String? = null,
    val objectifPoids: String? = null,
)

@Serializable
data class UpdatePreferencesRequest(
    val alimentsExclus: List<String>? = null,
    val allergies: List<String>? = null,
    val alimentsFavoris: List<String>? = null,
)

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
    val journalEntries: List<JournalEntryExportResponse>,
    val quotas: List<QuotaExportResponse>,
    val poidsHistory: List<PoidsExportResponse>,
    val hydratation: List<HydratationExportResponse>,
    val consentements: List<ConsentExportResponse>,
    val exportedAt: String,
)

@Serializable
data class NutrimentValuesResponse(
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val fibres: Double,
    val sel: Double,
    val sucres: Double,
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double,
)

@Serializable
data class JournalEntryExportResponse(
    val id: String,
    val date: String,
    val mealType: String,
    val alimentId: String?,
    val recetteId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val nbPortions: Double?,
    val nutrimentsCalcules: NutrimentValuesResponse,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class QuotaExportResponse(
    val nutriment: String,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
)

@Serializable
data class PoidsExportResponse(
    val id: String,
    val date: String,
    val poidsKg: Double,
    val estReference: Boolean,
    val createdAt: String,
)

@Serializable
data class HydratationExportResponse(
    val id: String,
    val date: String,
    val quantiteMl: Int,
    val objectifMl: Int,
    val estObjectifPersonnalise: Boolean,
    val pourcentage: Double,
)

@Serializable
data class ConsentExportResponse(
    val type: String,
    val accepte: Boolean,
    val dateConsentement: String,
    val versionPolitique: String,
)
