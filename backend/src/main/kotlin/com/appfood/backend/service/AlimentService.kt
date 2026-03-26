package com.appfood.backend.service

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.dao.PortionDao
import com.appfood.backend.database.dao.PortionRow
import com.appfood.backend.external.OpenFoodFactsClient
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.AlimentResponse
import com.appfood.backend.routes.dto.NutrimentValuesResponse
import com.appfood.backend.routes.dto.PortionResponse
import com.appfood.backend.routes.dto.SearchAlimentResponse
import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.search.SearchHit
import com.appfood.backend.search.SearchQuery
import org.slf4j.LoggerFactory

class AlimentService(
    private val alimentDao: AlimentDao,
    private val portionDao: PortionDao,
    private val meilisearchClient: MeilisearchClient,
    private val openFoodFactsClient: OpenFoodFactsClient,
) {
    private val logger = LoggerFactory.getLogger("AlimentService")

    /**
     * Search aliments via Meilisearch.
     * Validates inputs and builds proper Meilisearch filters.
     */
    suspend fun search(
        query: String,
        regime: String?,
        categorie: String?,
        page: Int,
        size: Int,
    ): SearchAlimentResponse {
        if (query.length < 2) {
            throw ValidationException("Le parametre 'q' doit contenir au moins 2 caracteres")
        }

        val validatedSize = size.coerceIn(1, 100)
        val validatedPage = page.coerceAtLeast(1)

        val filters = buildList {
            if (regime != null) add("regimesCompatibles = \"$regime\"")
            if (categorie != null) add("categorie = \"$categorie\"")
        }

        val searchQuery = SearchQuery(
            q = query,
            filter = filters.ifEmpty { null },
            sort = null,
            limit = validatedSize,
            offset = (validatedPage - 1) * validatedSize,
        )

        val result = meilisearchClient.search(AlimentIndexer.INDEX_NAME, searchQuery)
        logger.info("Search: query='$query', results=${result.hits.size}, total=${result.estimatedTotalHits}")

        val alimentResponses = result.hits.map { hit -> hit.toAlimentResponse() }

        return SearchAlimentResponse(
            data = alimentResponses,
            total = result.estimatedTotalHits,
            query = query,
        )
    }

    /**
     * Find an aliment by ID from the database.
     */
    suspend fun findById(id: String): AlimentResponse {
        val aliment = alimentDao.findById(id)
            ?: throw NotFoundException("Aliment non trouve: $id")

        val portions = portionDao.findByAlimentId(id, userId = null)
        return aliment.toAlimentResponse(portions)
    }

    /**
     * Find an aliment by barcode.
     * Checks the local database first, then falls back to Open Food Facts.
     */
    suspend fun findByBarcode(code: String): AlimentResponse {
        // Check local database first
        val cached = alimentDao.findByCodeBarres(code)
        if (cached != null) {
            logger.info("Barcode $code found in local database")
            val portions = portionDao.findByAlimentId(cached.id, userId = null)
            return cached.toAlimentResponse(portions)
        }

        // Fallback to Open Food Facts
        logger.info("Barcode $code not found locally, querying Open Food Facts")
        val offResult = openFoodFactsClient.searchByBarcode(code)
            ?: throw NotFoundException("Produit non trouve pour le code-barres: $code")

        val portions = portionDao.findByAlimentId(offResult.id, userId = null)
        return offResult.toAlimentResponse(portions)
    }

    // --- Mapping helpers ---

    private fun SearchHit.toAlimentResponse() = AlimentResponse(
        id = id,
        nom = nom,
        marque = marque,
        source = source,
        sourceId = sourceId,
        codeBarres = null, // Not available from Meilisearch index
        categorie = categorie,
        regimesCompatibles = regimesCompatibles,
        nutrimentsPour100g = NutrimentValuesResponse(
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
        ),
        portionsStandard = emptyList(), // Not loaded from search results for performance
    )

    private fun AlimentRow.toAlimentResponse(
        portions: List<PortionRow> = emptyList(),
    ) = AlimentResponse(
        id = id,
        nom = nom,
        marque = marque,
        source = source.name,
        sourceId = sourceId,
        codeBarres = codeBarres,
        categorie = categorie,
        regimesCompatibles = parseRegimesCompatibles(regimesCompatibles),
        nutrimentsPour100g = NutrimentValuesResponse(
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
        ),
        portionsStandard = portions.map { it.toPortionResponse() },
    )

    companion object {
        fun PortionRow.toPortionResponse() = PortionResponse(
            id = id,
            alimentId = alimentId,
            nom = nom,
            quantiteGrammes = quantiteGrammes,
            estGenerique = estGenerique,
            estPersonnalise = estPersonnalise,
        )

        /**
         * Parses the regimesCompatibles JSON array string into a list of strings.
         */
        fun parseRegimesCompatibles(json: String): List<String> {
            return try {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(json)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
