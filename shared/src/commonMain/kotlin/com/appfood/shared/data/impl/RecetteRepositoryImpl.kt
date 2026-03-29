package com.appfood.shared.data.impl

import com.appfood.shared.api.response.RecetteSummaryResponse
import com.appfood.shared.data.remote.RecetteApi
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceRecette
import com.appfood.shared.util.AppResult
import kotlin.time.Clock

/**
 * Combines remote API calls with local cache for recipe browsing.
 */
class RecetteRepositoryImpl(
    private val recetteApi: RecetteApi,
) : RecetteRepository {

    override suspend fun listRecettes(
        regime: String?,
        typeRepas: String?,
        sort: String?,
        query: String?,
        page: Int,
        limit: Int,
    ): AppResult<List<Recette>> {
        return try {
            val response = recetteApi.listRecettes(regime, typeRepas, sort, query, page, limit)
            val recettes = response.data.map { it.toDomain() }
            AppResult.Success(recettes)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch recipes",
                cause = e,
            )
        }
    }

    override suspend fun getRecette(id: String): AppResult<Recette> {
        return try {
            val response = recetteApi.getRecette(id)
            AppResult.Success(response.toDomain())
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch recipe",
                cause = e,
            )
        }
    }

    private fun RecetteSummaryResponse.toDomain(): Recette {
        return Recette(
            id = id,
            nom = nom,
            description = description,
            tempsPreparationMin = tempsPreparationMin,
            tempsCuissonMin = tempsCuissonMin,
            nbPortions = nbPortions,
            regimesCompatibles = regimesCompatibles.mapNotNull {
                runCatching { RegimeAlimentaire.valueOf(it) }.getOrNull()
            },
            source = SourceRecette.COMMUNAUTAIRE,
            typeRepas = typeRepas.mapNotNull {
                runCatching { MealType.valueOf(it) }.getOrNull()
            },
            ingredients = emptyList(),
            etapes = emptyList(),
            nutrimentsTotaux = NutrimentValues(),
            imageUrl = imageUrl,
            publie = true,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
    }
}
