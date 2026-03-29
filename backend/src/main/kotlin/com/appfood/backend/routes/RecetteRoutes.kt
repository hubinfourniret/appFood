package com.appfood.backend.routes

import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.CreateRecetteRequest
import com.appfood.backend.routes.dto.IngredientResponse
import com.appfood.backend.routes.dto.NutrimentValuesResponse
import com.appfood.backend.routes.dto.RecetteDetailResponse
import com.appfood.backend.routes.dto.RecetteListResponse
import com.appfood.backend.routes.dto.RecetteSummaryResponse
import com.appfood.backend.routes.dto.UpdateRecetteRequest
import com.appfood.backend.service.RecetteService
import com.appfood.backend.service.RecetteWithIngredients
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.recetteRoutes() {
    val recetteService by inject<RecetteService>()

    authenticate("auth-jwt") {
        route("/api/v1/recettes") {

            // GET /api/v1/recettes — liste avec filtres, pagination, search
            get {
                val regime = call.request.queryParameters["regime"]
                val typeRepas = call.request.queryParameters["typeRepas"]
                val sort = call.request.queryParameters["sort"]
                val query = call.request.queryParameters["q"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                val (recettes, total) = recetteService.listRecettes(
                    regime = regime,
                    typeRepas = typeRepas,
                    sort = sort,
                    query = query,
                    page = page,
                    size = size,
                )

                call.respond(
                    HttpStatusCode.OK,
                    RecetteListResponse(
                        data = recettes.map { it.toSummaryResponse() },
                        total = total,
                    ),
                )
            }

            // GET /api/v1/recettes/{id} — detail
            get("/{id}") {
                val id = call.parameters["id"]!!
                val detail = recetteService.getRecetteDetail(id)
                call.respond(HttpStatusCode.OK, detail.toDetailResponse())
            }

            // POST /api/v1/recettes — creation (ADMIN)
            post {
                val userId = call.userId()
                val request = call.receive<CreateRecetteRequest>()
                val created = recetteService.createRecette(userId, request)
                call.respond(HttpStatusCode.Created, created.toDetailResponse())
            }

            // PUT /api/v1/recettes/{id} — mise a jour (ADMIN)
            put("/{id}") {
                val userId = call.userId()
                val recetteId = call.parameters["id"]!!
                val request = call.receive<UpdateRecetteRequest>()
                val updated = recetteService.updateRecette(userId, recetteId, request)
                call.respond(HttpStatusCode.OK, updated.toDetailResponse())
            }

            // DELETE /api/v1/recettes/{id} — suppression (ADMIN)
            delete("/{id}") {
                val userId = call.userId()
                val recetteId = call.parameters["id"]!!
                recetteService.deleteRecette(userId, recetteId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun RecetteWithIngredients.toSummaryResponse(): RecetteSummaryResponse {
    val portions = recette.nbPortions.coerceAtLeast(1)
    return RecetteSummaryResponse(
        id = recette.id,
        nom = recette.nom,
        description = recette.description,
        tempsPreparationMin = recette.tempsPreparationMin,
        tempsCuissonMin = recette.tempsCuissonMin,
        nbPortions = recette.nbPortions,
        regimesCompatibles = recette.regimesCompatibles.split(",").filter { it.isNotBlank() },
        source = recette.source.name,
        typeRepas = recette.typeRepas.split(",").filter { it.isNotBlank() },
        imageUrl = recette.imageUrl,
        nutrimentsParPortion = nutrientsPerPortion(portions),
    )
}

private fun RecetteWithIngredients.toDetailResponse(): RecetteDetailResponse {
    val portions = recette.nbPortions.coerceAtLeast(1)
    return RecetteDetailResponse(
        id = recette.id,
        nom = recette.nom,
        description = recette.description,
        tempsPreparationMin = recette.tempsPreparationMin,
        tempsCuissonMin = recette.tempsCuissonMin,
        nbPortions = recette.nbPortions,
        regimesCompatibles = recette.regimesCompatibles.split(",").filter { it.isNotBlank() },
        typeRepas = recette.typeRepas.split(",").filter { it.isNotBlank() },
        ingredients = ingredients.map {
            IngredientResponse(
                alimentId = it.alimentId,
                alimentNom = it.alimentNom,
                quantiteGrammes = it.quantiteGrammes,
            )
        },
        etapes = recette.etapes.split("|||").filter { it.isNotBlank() },
        nutrimentsTotaux = NutrimentValuesResponse(
            calories = recette.calories,
            proteines = recette.proteines,
            glucides = recette.glucides,
            lipides = recette.lipides,
            fibres = recette.fibres,
            sel = recette.sel,
            sucres = recette.sucres,
            fer = recette.fer,
            calcium = recette.calcium,
            zinc = recette.zinc,
            magnesium = recette.magnesium,
            vitamineB12 = recette.vitamineB12,
            vitamineD = recette.vitamineD,
            vitamineC = recette.vitamineC,
            omega3 = recette.omega3,
            omega6 = recette.omega6,
        ),
        nutrimentsParPortion = nutrientsPerPortion(portions),
        source = recette.source.name,
        imageUrl = recette.imageUrl,
        createdAt = recette.createdAt.toString(),
        updatedAt = recette.updatedAt.toString(),
    )
}

private fun RecetteWithIngredients.nutrientsPerPortion(portions: Int): NutrimentValuesResponse {
    val p = portions.toDouble()
    return NutrimentValuesResponse(
        calories = recette.calories / p,
        proteines = recette.proteines / p,
        glucides = recette.glucides / p,
        lipides = recette.lipides / p,
        fibres = recette.fibres / p,
        sel = recette.sel / p,
        sucres = recette.sucres / p,
        fer = recette.fer / p,
        calcium = recette.calcium / p,
        zinc = recette.zinc / p,
        magnesium = recette.magnesium / p,
        vitamineB12 = recette.vitamineB12 / p,
        vitamineD = recette.vitamineD / p,
        vitamineC = recette.vitamineC / p,
        omega3 = recette.omega3 / p,
        omega6 = recette.omega6 / p,
    )
}
