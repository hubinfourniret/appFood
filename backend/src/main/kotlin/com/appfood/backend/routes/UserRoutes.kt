package com.appfood.backend.routes

import com.appfood.backend.database.dao.UserPreferencesRow
import com.appfood.backend.database.dao.UserProfileRow
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.ConsentExportResponse
import com.appfood.backend.routes.dto.CreateProfileRequest
import com.appfood.backend.routes.dto.HydratationExportResponse
import com.appfood.backend.routes.dto.JournalEntryExportResponse
import com.appfood.backend.routes.dto.NutrimentValuesResponse
import com.appfood.backend.routes.dto.PoidsExportResponse
import com.appfood.backend.routes.dto.PreferencesResponse
import com.appfood.backend.routes.dto.ProfileResponse
import com.appfood.backend.routes.dto.QuotaExportResponse
import com.appfood.backend.routes.dto.UpdatePreferencesRequest
import com.appfood.backend.routes.dto.UpdateProfileRequest
import com.appfood.backend.routes.dto.UserExportResponse
import com.appfood.backend.routes.dto.UserProfileResponse
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
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val profileService by inject<ProfileService>()

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
                val exportData = profileService.exportUserData(userId)
                val data = exportData.userProfileData

                val export = UserExportResponse(
                    user = data.user.toUserResponse(
                        onboardingComplete = data.profile?.onboardingComplete ?: false,
                    ),
                    profile = data.profile?.toProfileResponse(),
                    preferences = data.preferences?.toPreferencesResponse(),
                    journalEntries = exportData.journalEntries.map { entry ->
                        JournalEntryExportResponse(
                            id = entry.id,
                            date = entry.date.toString(),
                            mealType = entry.mealType.name,
                            alimentId = entry.alimentId,
                            recetteId = entry.recetteId,
                            nom = entry.nom,
                            quantiteGrammes = entry.quantiteGrammes,
                            nbPortions = entry.nbPortions,
                            nutrimentsCalcules = NutrimentValuesResponse(
                                calories = entry.calories,
                                proteines = entry.proteines,
                                glucides = entry.glucides,
                                lipides = entry.lipides,
                                fibres = entry.fibres,
                                sel = entry.sel,
                                sucres = entry.sucres,
                                fer = entry.fer,
                                calcium = entry.calcium,
                                zinc = entry.zinc,
                                magnesium = entry.magnesium,
                                vitamineB12 = entry.vitamineB12,
                                vitamineD = entry.vitamineD,
                                vitamineC = entry.vitamineC,
                                omega3 = entry.omega3,
                                omega6 = entry.omega6,
                            ),
                            createdAt = entry.createdAt.toString(),
                            updatedAt = entry.updatedAt.toString(),
                        )
                    },
                    quotas = exportData.quotas.map { quota ->
                        QuotaExportResponse(
                            nutriment = quota.nutriment.name,
                            valeurCible = quota.valeurCible,
                            estPersonnalise = quota.estPersonnalise,
                            valeurCalculee = quota.valeurCalculee,
                            unite = quota.unite,
                        )
                    },
                    poidsHistory = exportData.poidsHistory.map { poids ->
                        PoidsExportResponse(
                            id = poids.id,
                            date = poids.date.toString(),
                            poidsKg = poids.poidsKg,
                            estReference = poids.estReference,
                            createdAt = poids.createdAt.toString(),
                        )
                    },
                    hydratation = exportData.hydratation.map { h ->
                        HydratationExportResponse(
                            id = h.id,
                            date = h.date.toString(),
                            quantiteMl = h.quantiteMl,
                            objectifMl = h.objectifMl,
                            estObjectifPersonnalise = h.estObjectifPersonnalise,
                            pourcentage = if (h.objectifMl > 0) h.quantiteMl.toDouble() / h.objectifMl * 100.0 else 0.0,
                        )
                    },
                    consentements = exportData.consentements.map { c ->
                        ConsentExportResponse(
                            type = c.type.name,
                            accepte = c.accepte,
                            dateConsentement = c.dateConsentement.toString(),
                            versionPolitique = c.versionPolitique,
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
