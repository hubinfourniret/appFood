package com.appfood.shared.data.impl

import com.appfood.shared.api.response.RecommandationAlimentResponse
import com.appfood.shared.data.remote.RecommandationApi
import com.appfood.shared.data.repository.RecommandationRepository
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.PortionStandard
import com.appfood.shared.model.RecommandationAliment
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import com.appfood.shared.util.AppResult

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
}
