package com.appfood.shared.data.impl

import com.appfood.shared.api.response.RecommandationAlimentResponse
import com.appfood.shared.api.response.RecommandationRecetteResponse
import com.appfood.shared.api.response.RecetteSummaryResponse
import com.appfood.shared.data.remote.RecommandationApi
import com.appfood.shared.data.repository.RecommandationRepository
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.IngredientRecette
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.PortionStandard
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RecommandationAliment
import com.appfood.shared.model.RecommandationRecette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import com.appfood.shared.model.SourceRecette
import com.appfood.shared.util.AppResult
import kotlin.time.Clock
import kotlinx.datetime.Instant

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
            aliment = aliment.let { a ->
                Aliment(
                    id = a.id,
                    nom = a.nom,
                    marque = a.marque,
                    source = SourceAliment.valueOf(a.source),
                    sourceId = a.sourceId,
                    codeBarres = a.codeBarres,
                    categorie = a.categorie,
                    regimesCompatibles = a.regimesCompatibles.map { RegimeAlimentaire.valueOf(it) },
                    nutrimentsPour100g = a.nutrimentsPour100g.let { n ->
                        NutrimentValues(
                            calories = n.calories,
                            proteines = n.proteines,
                            glucides = n.glucides,
                            lipides = n.lipides,
                            fibres = n.fibres,
                            sel = n.sel,
                            sucres = n.sucres,
                            fer = n.fer,
                            calcium = n.calcium,
                            zinc = n.zinc,
                            magnesium = n.magnesium,
                            vitamineB12 = n.vitamineB12,
                            vitamineD = n.vitamineD,
                            vitamineC = n.vitamineC,
                            omega3 = n.omega3,
                            omega6 = n.omega6,
                        )
                    },
                    portionsStandard = a.portionsStandard.map { p ->
                        PortionStandard(
                            id = p.id,
                            alimentId = p.alimentId,
                            nom = p.nom,
                            quantiteGrammes = p.quantiteGrammes,
                            estGenerique = p.estGenerique,
                            estPersonnalise = p.estPersonnalise,
                            userId = null,
                        )
                    },
                )
            },
            nutrimentsCibles = nutrimentsCibles.map { NutrimentType.valueOf(it) },
            quantiteSuggereGrammes = quantiteSuggereGrammes,
            pourcentageCouverture = pourcentageCouverture.mapKeys { (key, _) ->
                NutrimentType.valueOf(key)
            },
        )
    }

    private fun RecommandationRecetteResponse.toDomain(): RecommandationRecette {
        return RecommandationRecette(
            recette = recette.toDomainRecette(),
            nutrimentsCibles = nutrimentsCibles.map { NutrimentType.valueOf(it) },
            pourcentageCouvertureGlobal = pourcentageCouvertureGlobal,
            pourcentageCouverture = pourcentageCouverture.mapKeys { (key, _) ->
                NutrimentType.valueOf(key)
            },
        )
    }

    private fun RecetteSummaryResponse.toDomainRecette(): Recette {
        val now = Clock.System.now()
        return Recette(
            id = id,
            nom = nom,
            description = description,
            tempsPreparationMin = tempsPreparationMin,
            tempsCuissonMin = tempsCuissonMin,
            nbPortions = nbPortions,
            regimesCompatibles = regimesCompatibles.map { RegimeAlimentaire.valueOf(it) },
            source = SourceRecette.valueOf(source),
            typeRepas = typeRepas.map { MealType.valueOf(it) },
            ingredients = emptyList(), // Summary response doesn't include full ingredients
            etapes = emptyList(), // Summary response doesn't include steps
            nutrimentsTotaux = nutrimentsParPortion.let { n ->
                // nutrimentsParPortion is per-portion; multiply by nbPortions to get totaux
                NutrimentValues(
                    calories = n.calories * nbPortions,
                    proteines = n.proteines * nbPortions,
                    glucides = n.glucides * nbPortions,
                    lipides = n.lipides * nbPortions,
                    fibres = n.fibres * nbPortions,
                    sel = n.sel * nbPortions,
                    sucres = n.sucres * nbPortions,
                    fer = n.fer * nbPortions,
                    calcium = n.calcium * nbPortions,
                    zinc = n.zinc * nbPortions,
                    magnesium = n.magnesium * nbPortions,
                    vitamineB12 = n.vitamineB12 * nbPortions,
                    vitamineD = n.vitamineD * nbPortions,
                    vitamineC = n.vitamineC * nbPortions,
                    omega3 = n.omega3 * nbPortions,
                    omega6 = n.omega6 * nbPortions,
                )
            },
            imageUrl = imageUrl,
            publie = true, // Listed recipes are always published
            createdAt = now,
            updatedAt = now,
        )
    }
}
