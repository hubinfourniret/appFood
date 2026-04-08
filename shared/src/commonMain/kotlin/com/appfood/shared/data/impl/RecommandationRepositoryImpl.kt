package com.appfood.shared.data.impl

import com.appfood.shared.api.response.AlimentSummaryResponse
import com.appfood.shared.api.response.RecommandationAlimentResponse
import com.appfood.shared.api.response.RecommandationRecetteResponse
import com.appfood.shared.data.remote.RecommandationApi
import com.appfood.shared.data.repository.RecommandationRepository
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RecommandationAliment
import com.appfood.shared.model.RecommandationRecette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import com.appfood.shared.model.SourceRecette
import com.appfood.shared.util.AppResult
import kotlin.time.Clock

/**
 * Fetches recommendations from the remote API.
 * Recommendations are computed server-side; this repository
 * handles the network call and response mapping.
 */
class RecommandationRepositoryImpl(
    private val recommandationApi: RecommandationApi,
) : RecommandationRepository {

    override suspend fun getAlimentRecommandations(
        userId: String,
        date: String,
        limit: Int,
    ): AppResult<List<RecommandationAliment>> {
        return try {
            val response = recommandationApi.getAlimentRecommandations(date, limit)
            val recommandations = response.data.map { it.toDomain() }
            AppResult.Success(recommandations)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch food recommendations",
                cause = e,
            )
        }
    }

    override suspend fun getRecetteRecommandations(
        userId: String,
        date: String?,
        limit: Int?,
    ): AppResult<List<RecommandationRecette>> {
        return try {
            val response = recommandationApi.getRecetteRecommandations(date, limit)
            val recommandations = response.data.map { it.toDomain() }
            AppResult.Success(recommandations)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch recipe recommendations",
                cause = e,
            )
        }
    }

    // --- Mapping ---

    private fun RecommandationAlimentResponse.toDomain(): RecommandationAliment {
        return RecommandationAliment(
            aliment = aliment.toDomainAliment(),
            nutrimentsCibles = nutrimentsCibles.map { NutrimentType.valueOf(it) },
            quantiteSuggereGrammes = quantiteSuggereGrammes,
            pourcentageCouverture = pourcentageCouverture.mapKeys { (key, _) ->
                NutrimentType.valueOf(key)
            },
        )
    }

    private fun AlimentSummaryResponse.toDomainAliment(): Aliment {
        return Aliment(
            id = id,
            nom = nom,
            marque = null,
            source = SourceAliment.CIQUAL,
            sourceId = null,
            codeBarres = null,
            categorie = categorie,
            regimesCompatibles = regimesCompatibles.map { RegimeAlimentaire.valueOf(it) },
            nutrimentsPour100g = NutrimentValues(
                calories = caloriesPour100g,
            ),
        )
    }

    private fun RecommandationRecetteResponse.toDomain(): RecommandationRecette {
        val now = Clock.System.now()
        return RecommandationRecette(
            recette = Recette(
                id = recetteId,
                nom = nom,
                description = description,
                tempsPreparationMin = tempsPreparationMin,
                tempsCuissonMin = tempsCuissonMin,
                nbPortions = nbPortions,
                regimesCompatibles = emptyList(),
                source = SourceRecette.COMMUNAUTAIRE,
                typeRepas = emptyList(),
                ingredients = emptyList(),
                etapes = emptyList(),
                nutrimentsTotaux = NutrimentValues(),
                imageUrl = null,
                publie = true,
                createdAt = now,
                updatedAt = now,
            ),
            nutrimentsCibles = nutrimentsCibles.map { NutrimentType.valueOf(it) },
            pourcentageCouvertureGlobal = pourcentageCouvertureGlobal,
            pourcentageCouverture = pourcentageCouverture.mapKeys { (key, _) ->
                NutrimentType.valueOf(key)
            },
        )
    }
}
