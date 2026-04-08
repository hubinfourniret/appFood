package com.appfood.backend.external

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.search.AlimentIndexer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Client for the Open Food Facts API (https://world.openfoodfacts.org/api/v2/).
 *
 * Searches products by name or barcode, maps to the appFood Aliment schema,
 * caches results in PostgreSQL, and indexes them in Meilisearch.
 */
class OpenFoodFactsClient(
    private val httpClient: HttpClient,
    private val alimentDao: AlimentDao,
    private val alimentIndexer: AlimentIndexer,
) {
    private val logger = LoggerFactory.getLogger(OpenFoodFactsClient::class.java)

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org"
        private const val SEARCH_URL = "$BASE_URL/cgi/search.pl"
        private const val PRODUCT_URL = "$BASE_URL/api/v2/product"
        private const val USER_AGENT = "appFood/1.0 (contact@appfood.com)"
        private const val DEFAULT_PAGE_SIZE = 20
    }

    // -- Public API --

    /**
     * Search products by name.
     * Caches found products in PostgreSQL and indexes them in Meilisearch.
     *
     * @param query Search terms
     * @param page Page number (1-indexed)
     * @param pageSize Number of results per page (max 100)
     * @return List of AlimentRow mapped from OFF products
     */
    suspend fun searchByName(
        query: String,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): List<AlimentRow> {
        logger.info("Open Food Facts search: query='$query', page=$page, pageSize=$pageSize")

        val response: HttpResponse =
            httpClient.get(SEARCH_URL) {
                url {
                    parameters.append("search_terms", query)
                    parameters.append("search_simple", "1")
                    parameters.append("action", "process")
                    parameters.append("json", "1")
                    parameters.append("page", page.toString())
                    parameters.append("page_size", pageSize.toString())
                }
                header("User-Agent", USER_AGENT)
            }

        if (!response.status.isSuccess()) {
            logger.error("Open Food Facts search failed: ${response.status.value}")
            return emptyList()
        }

        val searchResponse: OffSearchResponse = response.body()
        val rows =
            searchResponse.products.mapNotNull { product ->
                mapProductToAlimentRow(product)
            }

        logger.info("Open Food Facts search: ${rows.size} products mapped from ${searchResponse.products.size} results")

        if (rows.isNotEmpty()) {
            cacheAndIndex(rows)
        }

        return rows
    }

    /**
     * Search a product by barcode.
     * Checks the local cache first, falls back to the OFF API.
     *
     * @param barcode EAN-13 or other barcode
     * @return AlimentRow if found, null otherwise
     */
    suspend fun searchByBarcode(barcode: String): AlimentRow? {
        logger.info("Open Food Facts barcode lookup: $barcode")

        // Check local cache first
        val cached = alimentDao.findByCodeBarres(barcode)
        if (cached != null) {
            logger.info("Barcode $barcode found in local cache (id=${cached.id})")
            return cached
        }

        // Fetch from Open Food Facts API
        val response: HttpResponse =
            httpClient.get("$PRODUCT_URL/$barcode") {
                header("User-Agent", USER_AGENT)
            }

        if (!response.status.isSuccess()) {
            logger.warn("Open Food Facts barcode lookup failed: ${response.status.value}")
            return null
        }

        val productResponse: OffProductResponse = response.body()

        if (productResponse.status != 1 || productResponse.product == null) {
            logger.info("Product not found for barcode $barcode")
            return null
        }

        val row = mapProductToAlimentRow(productResponse.product)
        if (row != null) {
            cacheAndIndex(listOf(row))
            logger.info("Barcode $barcode: product '${row.nom}' cached and indexed")
        }

        return row
    }

    // -- Mapping --

    private fun mapProductToAlimentRow(product: OffProduct): AlimentRow? {
        val nom = product.productName
        if (nom.isNullOrBlank()) {
            logger.debug("Skipping OFF product without name (code=${product.code})")
            return null
        }

        val nutriments = product.nutriments ?: return null

        val categorie =
            product.categoriesHierarchy?.firstOrNull()
                ?.removePrefix("en:")
                ?.replace("-", " ")
                ?: product.categories?.split(",")?.firstOrNull()?.trim()
                ?: "Autres"

        val regimesCompatibles = detectRegimes(product)
        val regimesJson = JsonArray(regimesCompatibles.map { JsonPrimitive(it.name) }).toString()

        return AlimentRow(
            id = UUID.randomUUID().toString(),
            nom = nom,
            marque = product.brands?.split(",")?.firstOrNull()?.trim(),
            source = SourceAliment.OPEN_FOOD_FACTS,
            sourceId = product.code,
            codeBarres = product.code,
            categorie = categorie,
            regimesCompatibles = regimesJson,
            calories = nutriments.energyKcal100g ?: 0.0,
            proteines = nutriments.proteins100g ?: 0.0,
            glucides = nutriments.carbohydrates100g ?: 0.0,
            lipides = nutriments.fat100g ?: 0.0,
            fibres = nutriments.fiber100g ?: 0.0,
            sel = nutriments.salt100g ?: 0.0,
            sucres = nutriments.sugars100g ?: 0.0,
            fer = nutriments.iron100g ?: 0.0, // OFF returns mg directly
            calcium = nutriments.calcium100g ?: 0.0, // OFF returns mg directly
            zinc = nutriments.zinc100g ?: 0.0, // OFF returns mg directly
            magnesium = nutriments.magnesium100g ?: 0.0, // OFF returns mg directly
            vitamineB12 = nutriments.vitaminB12100g ?: 0.0, // OFF returns µg directly
            vitamineD = nutriments.vitaminD100g ?: 0.0, // OFF returns µg directly
            vitamineC = nutriments.vitaminC100g ?: 0.0, // OFF returns mg directly
            omega3 = nutriments.omega3Fat100g ?: 0.0,
            omega6 = nutriments.omega6Fat100g ?: 0.0,
        )
    }

    /**
     * Detect compatible diets using OFF tags when available, fallback to category heuristic.
     */
    private fun detectRegimes(product: OffProduct): List<RegimeAlimentaire> {
        // Use OFF ingredient analysis if available
        val veganTag = product.ingredientsAnalysisTags?.find { it.contains("vegan") }
        val vegetarianTag = product.ingredientsAnalysisTags?.find { it.contains("vegetarian") }

        val isVegan = veganTag?.contains("en:vegan") == true
        val isNonVegan = veganTag?.contains("en:non-vegan") == true
        val isVegetarian = vegetarianTag?.contains("en:vegetarian") == true
        val isNonVegetarian = vegetarianTag?.contains("en:non-vegetarian") == true

        return when {
            isVegan ->
                listOf(
                    RegimeAlimentaire.VEGAN,
                    RegimeAlimentaire.VEGETARIEN,
                    RegimeAlimentaire.FLEXITARIEN,
                    RegimeAlimentaire.OMNIVORE,
                )
            isNonVegetarian || isNonVegan ->
                listOf(
                    RegimeAlimentaire.OMNIVORE,
                    RegimeAlimentaire.FLEXITARIEN,
                )
            isVegetarian ->
                listOf(
                    RegimeAlimentaire.VEGETARIEN,
                    RegimeAlimentaire.FLEXITARIEN,
                    RegimeAlimentaire.OMNIVORE,
                )
            else -> {
                // Fallback conservateur : sans tags, on ne peut pas garantir vegan/vegetarien
                listOf(
                    RegimeAlimentaire.OMNIVORE,
                    RegimeAlimentaire.FLEXITARIEN,
                )
            }
        }
    }

    // -- Cache & Index --

    private suspend fun cacheAndIndex(rows: List<AlimentRow>) {
        try {
            alimentDao.upsertBatch(rows)
            logger.info("Cached ${rows.size} OFF products in PostgreSQL")
        } catch (e: Exception) {
            logger.error("Failed to cache OFF products in PostgreSQL: ${e.message}", e)
        }

        try {
            alimentIndexer.indexBatch(rows)
            logger.info("Indexed ${rows.size} OFF products in Meilisearch")
        } catch (e: Exception) {
            logger.error("Failed to index OFF products in Meilisearch: ${e.message}", e)
        }
    }
}

// -- Open Food Facts API response models --

@Serializable
data class OffSearchResponse(
    val count: Int = 0,
    val page: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 20,
    val products: List<OffProduct> = emptyList(),
)

@Serializable
data class OffProductResponse(
    val status: Int = 0,
    @SerialName("status_verbose")
    val statusVerbose: String? = null,
    val product: OffProduct? = null,
)

@Serializable
data class OffProduct(
    val code: String? = null,
    @SerialName("product_name")
    val productName: String? = null,
    val brands: String? = null,
    val categories: String? = null,
    @SerialName("categories_hierarchy")
    val categoriesHierarchy: List<String>? = null,
    val nutriments: OffNutriments? = null,
    @SerialName("ingredients_analysis_tags")
    val ingredientsAnalysisTags: List<String>? = null,
)

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g")
    val energyKcal100g: Double? = null,
    @SerialName("proteins_100g")
    val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g")
    val carbohydrates100g: Double? = null,
    @SerialName("fat_100g")
    val fat100g: Double? = null,
    @SerialName("fiber_100g")
    val fiber100g: Double? = null,
    @SerialName("salt_100g")
    val salt100g: Double? = null,
    @SerialName("sugars_100g")
    val sugars100g: Double? = null,
    @SerialName("iron_100g")
    val iron100g: Double? = null,
    @SerialName("calcium_100g")
    val calcium100g: Double? = null,
    @SerialName("zinc_100g")
    val zinc100g: Double? = null,
    @SerialName("magnesium_100g")
    val magnesium100g: Double? = null,
    @SerialName("vitamin-b12_100g")
    val vitaminB12100g: Double? = null,
    @SerialName("vitamin-d_100g")
    val vitaminD100g: Double? = null,
    @SerialName("vitamin-c_100g")
    val vitaminC100g: Double? = null,
    @SerialName("omega-3-fat_100g")
    val omega3Fat100g: Double? = null,
    @SerialName("omega-6-fat_100g")
    val omega6Fat100g: Double? = null,
)
