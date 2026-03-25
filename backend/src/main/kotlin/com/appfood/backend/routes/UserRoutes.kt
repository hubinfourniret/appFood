package com.appfood.backend.routes

import com.appfood.backend.database.dao.ConsentDao
import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.UserPreferencesRow
import com.appfood.backend.database.dao.UserProfileRow
import com.appfood.backend.plugins.userId
import com.appfood.backend.service.ProfileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

// --- Request DTOs ---

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

// --- Response DTOs ---

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
    val journalEntries: List<JournalEntryExportItem>,
    val quotas: List<QuotaExportItem>,
    val poidsHistory: List<PoidsExportItem>,
    val hydratation: List<HydratationExportItem>,
    val consentements: List<ConsentExportItem>,
    val exportedAt: String,
)

@Serializable
data class JournalEntryExportItem(
    val id: String,
    val date: String,
    val mealType: String,
    val nom: String,
    val quantiteGrammes: Double,
    val calories: Double,
)

@Serializable
data class QuotaExportItem(
    val nutriment: String,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
)

@Serializable
data class PoidsExportItem(
    val date: String,
    val poidsKg: Double,
)

@Serializable
data class HydratationExportItem(
    val date: String,
    val quantiteMl: Int,
    val objectifMl: Int,
)

@Serializable
data class ConsentExportItem(
    val type: String,
    val accepte: Boolean,
    val dateConsentement: String,
)

fun Route.userRoutes() {
    val profileService by inject<ProfileService>()
    val journalEntryDao by inject<JournalEntryDao>()
    val quotaDao by inject<QuotaDao>()
    val poidsHistoryDao by inject<PoidsHistoryDao>()
    val hydratationDao by inject<HydratationDao>()
    val consentDao by inject<ConsentDao>()

    authenticate("auth-jwt") {
        route("/api/v1/users") {
            get("/me") {
                val userId = call.userId()
                val data = profileService.getUserProfile(userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        data = UserProfileResponse(
                            user = data.user.toUserResponse(
                                onboardingComplete = data.profile?.onboardingComplete ?: false,
                            ),
                            profile = data.profile?.toProfileResponse(),
                            preferences = data.preferences?.toPreferencesResponse(),
                        ),
                    ),
                )
            }

            post("/me/profile") {
                val userId = call.userId()
                val request = call.receive<CreateProfileRequest>()
                val profile = profileService.createProfile(
                    userId = userId,
                    sexeStr = request.sexe,
                    age = request.age,
                    poidsKg = request.poidsKg,
                    tailleCm = request.tailleCm,
                    regimeAlimentaireStr = request.regimeAlimentaire,
                    niveauActiviteStr = request.niveauActivite,
                )
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(data = profile.toProfileResponse()),
                )
            }

            put("/me/profile") {
                val userId = call.userId()
                val request = call.receive<UpdateProfileRequest>()
                val profile = profileService.updateProfile(
                    userId = userId,
                    sexeStr = request.sexe,
                    age = request.age,
                    poidsKg = request.poidsKg,
                    tailleCm = request.tailleCm,
                    regimeAlimentaireStr = request.regimeAlimentaire,
                    niveauActiviteStr = request.niveauActivite,
                    objectifPoidsStr = request.objectifPoids,
                )
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(data = profile.toProfileResponse()),
                )
            }

            put("/me/preferences") {
                val userId = call.userId()
                val request = call.receive<UpdatePreferencesRequest>()
                val preferences = profileService.updatePreferences(
                    userId = userId,
                    alimentsExclus = request.alimentsExclus,
                    allergies = request.allergies,
                    alimentsFavoris = request.alimentsFavoris,
                )
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(data = preferences.toPreferencesResponse()),
                )
            }

            get("/me/export") {
                val userId = call.userId()
                val data = profileService.getUserProfile(userId)
                val journalEntries = journalEntryDao.findByUserAll(userId)
                val quotas = quotaDao.findByUserId(userId)
                val poidsHistory = poidsHistoryDao.findByUserId(userId)
                val hydratation = hydratationDao.findByUserId(userId)
                val consentements = consentDao.findByUserId(userId)

                val export = UserExportResponse(
                    user = data.user.toUserResponse(
                        onboardingComplete = data.profile?.onboardingComplete ?: false,
                    ),
                    profile = data.profile?.toProfileResponse(),
                    preferences = data.preferences?.toPreferencesResponse(),
                    journalEntries = journalEntries.map { entry ->
                        JournalEntryExportItem(
                            id = entry.id,
                            date = entry.date.toString(),
                            mealType = entry.mealType.name,
                            nom = entry.nom,
                            quantiteGrammes = entry.quantiteGrammes,
                            calories = entry.calories,
                        )
                    },
                    quotas = quotas.map { quota ->
                        QuotaExportItem(
                            nutriment = quota.nutriment.name,
                            valeurCible = quota.valeurCible,
                            estPersonnalise = quota.estPersonnalise,
                        )
                    },
                    poidsHistory = poidsHistory.map { poids ->
                        PoidsExportItem(
                            date = poids.date.toString(),
                            poidsKg = poids.poidsKg,
                        )
                    },
                    hydratation = hydratation.map { h ->
                        HydratationExportItem(
                            date = h.date.toString(),
                            quantiteMl = h.quantiteMl,
                            objectifMl = h.objectifMl,
                        )
                    },
                    consentements = consentements.map { c ->
                        ConsentExportItem(
                            type = c.type.name,
                            accepte = c.accepte,
                            dateConsentement = c.dateConsentement.toString(),
                        )
                    },
                    exportedAt = Clock.System.now().toString(),
                )

                call.respond(HttpStatusCode.OK, ApiResponse(data = export))
            }
        }
    }
}

// --- Mapping extensions ---

internal fun UserProfileRow.toProfileResponse() = ProfileResponse(
    sexe = sexe.name,
    age = age,
    poidsKg = poidsKg,
    tailleCm = tailleCm,
    regimeAlimentaire = regimeAlimentaire.name,
    niveauActivite = niveauActivite.name,
    onboardingComplete = onboardingComplete,
    objectifPoids = objectifPoids?.name,
    updatedAt = updatedAt.toString(),
)

internal fun UserPreferencesRow.toPreferencesResponse() = PreferencesResponse(
    alimentsExclus = ProfileService.deserializeList(alimentsExclus),
    allergies = ProfileService.deserializeList(allergies),
    alimentsFavoris = ProfileService.deserializeList(alimentsFavoris),
    updatedAt = updatedAt.toString(),
)
