package com.appfood.shared.data.impl

import com.appfood.shared.api.response.AlimentResponse
import com.appfood.shared.api.response.PortionListResponse
import com.appfood.shared.api.response.SearchAlimentResponse
import com.appfood.shared.data.local.LocalAlimentDataSource
import com.appfood.shared.data.remote.AlimentApi
import com.appfood.shared.data.remote.PortionApi
import com.appfood.shared.data.repository.AlimentRepository
import com.appfood.shared.db.Local_aliment
import com.appfood.shared.util.AppResult
import kotlin.time.Clock

/**
 * Fetches aliment and portion data from the remote API.
 * Cache les aliments localement avec strategie LRU (max 500 entrees).
 */
class AlimentRepositoryImpl(
    private val alimentApi: AlimentApi,
    private val portionApi: PortionApi,
    private val localAlimentDataSource: LocalAlimentDataSource,
) : AlimentRepository {

    companion object {
        const val MAX_CACHED_ALIMENTS = 500
    }

    override suspend fun search(
        query: String,
        regime: String?,
        page: Int?,
        size: Int?,
    ): AppResult<SearchAlimentResponse> {
        return try {
            val response = alimentApi.search(query, regime, page, size)
            // Cache chaque aliment retourne par la recherche
            response.data.forEach { cacheAliment(it) }
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to search aliments",
                cause = e,
            )
        }
    }

    override suspend fun getById(id: String): AppResult<AlimentResponse> {
        return try {
            val response = alimentApi.getById(id)
            cacheAliment(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch aliment",
                cause = e,
            )
        }
    }

    override suspend fun getByBarcode(code: String): AppResult<AlimentResponse> {
        return try {
            val response = alimentApi.getByBarcode(code)
            cacheAliment(response)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch aliment by barcode",
                cause = e,
            )
        }
    }

    override suspend fun getPortions(alimentId: String?): AppResult<PortionListResponse> {
        return try {
            val response = portionApi.getPortions(alimentId)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch portions",
                cause = e,
            )
        }
    }

    private fun cacheAliment(response: AlimentResponse) {
        val now = Clock.System.now().toEpochMilliseconds()
        val localAliment = Local_aliment(
            id = response.id,
            nom = response.nom,
            marque = response.marque,
            categorie = response.categorie,
            source = response.source,
            source_id = response.sourceId,
            calories = response.nutrimentsPour100g.calories,
            proteines = response.nutrimentsPour100g.proteines,
            glucides = response.nutrimentsPour100g.glucides,
            lipides = response.nutrimentsPour100g.lipides,
            fibres = response.nutrimentsPour100g.fibres,
            sel = response.nutrimentsPour100g.sel,
            sucres = response.nutrimentsPour100g.sucres,
            fer = response.nutrimentsPour100g.fer,
            calcium = response.nutrimentsPour100g.calcium,
            zinc = response.nutrimentsPour100g.zinc,
            magnesium = response.nutrimentsPour100g.magnesium,
            vitamine_b12 = response.nutrimentsPour100g.vitamineB12,
            vitamine_d = response.nutrimentsPour100g.vitamineD,
            vitamine_c = response.nutrimentsPour100g.vitamineC,
            omega_3 = response.nutrimentsPour100g.omega3,
            omega_6 = response.nutrimentsPour100g.omega6,
            regimes_compatibles = response.regimesCompatibles.joinToString(","),
            last_accessed = now,
        )
        localAlimentDataSource.insertOrReplace(localAliment)
        localAlimentDataSource.evictOldEntries(MAX_CACHED_ALIMENTS)
    }
}
