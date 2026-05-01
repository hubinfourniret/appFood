package com.appfood.shared.data.impl

import com.appfood.shared.api.request.CreateRecetteRequest
import com.appfood.shared.api.request.UpdateRecetteRequest
import com.appfood.shared.api.response.NutrimentValuesResponse
import com.appfood.shared.api.response.RecetteDetailResponse
import com.appfood.shared.api.response.RecetteSummaryResponse
import com.appfood.shared.data.local.LocalRecetteDataSource
import com.appfood.shared.data.remote.RecetteApi
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.db.Local_recette
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceRecette
import com.appfood.shared.util.AppResult
import kotlin.time.Clock

/**
 * Combines remote API calls with local cache for recipe browsing.
 * Cache LRU limite a 100 recettes.
 */
class RecetteRepositoryImpl(
    private val recetteApi: RecetteApi,
    private val localRecetteDataSource: LocalRecetteDataSource,
) : RecetteRepository {

    companion object {
        const val MAX_CACHED_RECETTES = 100
    }

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
            // Cache chaque recette retournee
            response.data.forEach { cacheRecette(it) }
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
            cacheRecette(response)
            AppResult.Success(response.toDomain())
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch recipe",
                cause = e,
            )
        }
    }

    override suspend fun createRecette(request: CreateRecetteRequest): AppResult<Recette> {
        return try {
            val response = recetteApi.createRecette(request)
            cacheRecette(response)
            AppResult.Success(response.toDomain())
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to create recipe",
                cause = e,
            )
        }
    }

    override suspend fun listMyRecettes(): AppResult<List<Recette>> {
        return try {
            val response = recetteApi.listMyRecettes()
            // RecetteSummaryResponse → on a juste le summary, mais ca suffit pour la liste
            val recettes = response.data.map { summaryToDomain(it) }
            response.data.forEach { cacheRecette(it) }
            AppResult.Success(recettes)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch personal recipes",
                cause = e,
            )
        }
    }

    override suspend fun updateRecette(id: String, request: UpdateRecetteRequest): AppResult<Recette> {
        return try {
            val response = recetteApi.updateRecette(id, request)
            cacheRecette(response)
            AppResult.Success(response.toDomain())
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to update recipe",
                cause = e,
            )
        }
    }

    override suspend fun deleteRecette(id: String): AppResult<Unit> {
        return try {
            recetteApi.deleteRecette(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to delete recipe",
                cause = e,
            )
        }
    }

    private fun summaryToDomain(summary: RecetteSummaryResponse): Recette = Recette(
        id = summary.id,
        nom = summary.nom,
        description = summary.description,
        tempsPreparationMin = summary.tempsPreparationMin,
        tempsCuissonMin = summary.tempsCuissonMin,
        nbPortions = summary.nbPortions,
        regimesCompatibles = summary.regimesCompatibles.mapNotNull {
            runCatching { RegimeAlimentaire.valueOf(it) }.getOrNull()
        },
        source = runCatching { SourceRecette.valueOf(summary.source) }.getOrDefault(SourceRecette.MANUELLE),
        typeRepas = summary.typeRepas.mapNotNull {
            runCatching { MealType.valueOf(it) }.getOrNull()
        },
        ingredients = emptyList(),
        etapes = emptyList(),
        nutrimentsTotaux = summary.nutrimentsParPortion.toDomain().scale(summary.nbPortions.toDouble()),
        imageUrl = summary.imageUrl,
        publie = true,
        estPersonnelle = summary.estPersonnelle,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
    )

    private fun NutrimentValues.scale(factor: Double): NutrimentValues = NutrimentValues(
        calories = calories * factor,
        proteines = proteines * factor,
        glucides = glucides * factor,
        lipides = lipides * factor,
        fibres = fibres * factor,
        sel = sel * factor,
        sucres = sucres * factor,
        fer = fer * factor,
        calcium = calcium * factor,
        zinc = zinc * factor,
        magnesium = magnesium * factor,
        vitamineB12 = vitamineB12 * factor,
        vitamineD = vitamineD * factor,
        vitamineC = vitamineC * factor,
        omega3 = omega3 * factor,
        omega6 = omega6 * factor,
    )

    private fun cacheRecette(response: RecetteSummaryResponse) {
        val now = Clock.System.now().toEpochMilliseconds()
        val local = Local_recette(
            id = response.id,
            nom = response.nom,
            description = response.description,
            temps_preparation_min = response.tempsPreparationMin.toLong(),
            temps_cuisson_min = response.tempsCuissonMin.toLong(),
            nb_portions = response.nbPortions.toLong(),
            regimes_compatibles = response.regimesCompatibles.joinToString(","),
            source = response.source,
            type_repas = response.typeRepas.joinToString(","),
            image_url = response.imageUrl,
            calories = response.nutrimentsParPortion.calories,
            proteines = response.nutrimentsParPortion.proteines,
            glucides = response.nutrimentsParPortion.glucides,
            lipides = response.nutrimentsParPortion.lipides,
            fibres = response.nutrimentsParPortion.fibres,
            access_date = now,
            created_at = now,
        )
        localRecetteDataSource.insertOrReplace(local)
        localRecetteDataSource.evictOldEntries(MAX_CACHED_RECETTES)
    }

    private fun cacheRecette(response: RecetteDetailResponse) {
        val now = Clock.System.now().toEpochMilliseconds()
        val local = Local_recette(
            id = response.id,
            nom = response.nom,
            description = response.description,
            temps_preparation_min = response.tempsPreparationMin.toLong(),
            temps_cuisson_min = response.tempsCuissonMin.toLong(),
            nb_portions = response.nbPortions.toLong(),
            regimes_compatibles = response.regimesCompatibles.joinToString(","),
            source = response.source,
            type_repas = response.typeRepas.joinToString(","),
            image_url = response.imageUrl,
            calories = response.nutrimentsParPortion.calories,
            proteines = response.nutrimentsParPortion.proteines,
            glucides = response.nutrimentsParPortion.glucides,
            lipides = response.nutrimentsParPortion.lipides,
            fibres = response.nutrimentsParPortion.fibres,
            access_date = now,
            created_at = now,
        )
        localRecetteDataSource.insertOrReplace(local)
        localRecetteDataSource.evictOldEntries(MAX_CACHED_RECETTES)
    }

    private fun RecetteDetailResponse.toDomain(): Recette {
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
            source = runCatching { SourceRecette.valueOf(source) }.getOrDefault(SourceRecette.COMMUNAUTAIRE),
            typeRepas = typeRepas.mapNotNull {
                runCatching { MealType.valueOf(it) }.getOrNull()
            },
            ingredients = ingredients.map {
                com.appfood.shared.model.IngredientRecette(
                    id = it.id,
                    alimentId = it.alimentId,
                    alimentNom = it.alimentNom,
                    quantiteGrammes = it.quantiteGrammes,
                    nutrimentsPour100g = it.nutrimentsPour100g?.toDomain(),
                )
            },
            etapes = etapes,
            nutrimentsTotaux = nutrimentsTotaux.toDomain(),
            imageUrl = imageUrl,
            publie = true,
            estPersonnelle = estPersonnelle,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
    }

    private fun NutrimentValuesResponse.toDomain(): NutrimentValues = NutrimentValues(
        calories = calories,
        proteines = proteines,
        glucides = glucides,
        lipides = lipides,
        fibres = fibres,
        sel = sel,
        sucres = sucres,
        fer = fer,
        calcium = calcium,
        zinc = zinc,
        magnesium = magnesium,
        vitamineB12 = vitamineB12,
        vitamineD = vitamineD,
        vitamineC = vitamineC,
        omega3 = omega3,
        omega6 = omega6,
    )

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
