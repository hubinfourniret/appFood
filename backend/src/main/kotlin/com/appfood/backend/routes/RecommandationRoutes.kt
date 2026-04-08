package com.appfood.backend.routes

import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.AlimentSummaryResponse
import com.appfood.backend.routes.dto.RecommandationAlimentListResponse
import com.appfood.backend.routes.dto.RecommandationAlimentResponse
import com.appfood.backend.routes.dto.RecommandationRecetteListResponse
import com.appfood.backend.routes.dto.RecommandationRecetteResponse
import com.appfood.backend.service.RecommandationService
import com.appfood.backend.service.ScoredAliment
import com.appfood.backend.service.ScoredRecette
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.koin.ktor.ext.inject

fun Route.recommandationRoutes() {
    val recommandationService by inject<RecommandationService>()

    authenticate("auth-jwt") {
        route("/api/v1/recommandations") {
            get("/aliments") {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val date =
                    if (dateStr != null) {
                        parseRecoDate(dateStr)
                    } else {
                        todayRecoDate()
                    }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

                val result = recommandationService.getRecommandationsAliments(userId, date, limit)

                call.respond(
                    HttpStatusCode.OK,
                    RecommandationAlimentListResponse(
                        date = date.toString(),
                        manquesIdentifies = result.manquesIdentifies,
                        data = result.recommandations.map { it.toResponse() },
                    ),
                )
            }

            get("/recettes") {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val date =
                    if (dateStr != null) {
                        parseRecoDate(dateStr)
                    } else {
                        todayRecoDate()
                    }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5

                val result = recommandationService.getRecommandationsRecettes(userId, date, limit)

                call.respond(
                    HttpStatusCode.OK,
                    RecommandationRecetteListResponse(
                        date = date.toString(),
                        manquesIdentifies = result.manquesIdentifies,
                        data = result.recommandations.map { it.toResponse() },
                    ),
                )
            }
        }
    }
}

// --- Mapping helpers ---

internal fun ScoredAliment.toResponse() =
    RecommandationAlimentResponse(
        aliment = aliment.toSummaryResponse(),
        nutrimentsCibles = nutrimentsCibles,
        quantiteSuggereGrammes = quantiteSuggereGrammes,
        pourcentageCouverture = pourcentageCouverture,
    )

internal fun AlimentRow.toSummaryResponse(): AlimentSummaryResponse {
    val regimes =
        try {
            Json.parseToJsonElement(regimesCompatibles).jsonArray.map { it.jsonPrimitive.content }
        } catch (e: Exception) {
            emptyList()
        }
    return AlimentSummaryResponse(
        id = id,
        nom = nom,
        categorie = categorie,
        caloriesPour100g = calories,
        regimesCompatibles = regimes,
    )
}

internal fun ScoredRecette.toResponse() =
    RecommandationRecetteResponse(
        recetteId = recette.id,
        nom = recette.nom,
        description = recette.description,
        tempsPreparationMin = recette.tempsPreparationMin,
        tempsCuissonMin = recette.tempsCuissonMin,
        nbPortions = recette.nbPortions,
        nutrimentsCibles = nutrimentsCibles,
        pourcentageCouvertureGlobal = pourcentageCouvertureGlobal,
        pourcentageCouverture = pourcentageCouverture,
    )

private fun parseRecoDate(dateStr: String): LocalDate {
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        throw com.appfood.backend.plugins.ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
    }
}

private fun todayRecoDate(): LocalDate {
    val todayStr = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
    return LocalDate.parse(todayStr)
}
