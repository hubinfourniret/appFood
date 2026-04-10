package com.appfood.backend.external

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.dao.IngredientRow
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.database.dao.RecetteRow
import com.appfood.backend.database.tables.SourceRecette
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.UUID

/**
 * Import des recettes curees depuis un fichier JSON embarque.
 *
 * Pour chaque recette :
 *  - Parse via kotlinx.serialization
 *  - Matche chaque ingredient (fuzzy, en memoire) contre la base Ciqual
 *  - Calcule les nutriments totaux a partir des aliments matches
 *  - Insere la recette (publie = true) et ses ingredients
 *
 * Idempotent : verifie par nom si la recette existe deja avant insertion.
 */
class RecetteImporter(
    private val alimentDao: AlimentDao,
    private val recetteDao: RecetteDao,
) {
    private val logger = LoggerFactory.getLogger(RecetteImporter::class.java)

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    companion object {
        private const val ALIMENT_LOAD_LIMIT = 5000
        private const val MIN_KEYWORD_LENGTH = 3

        private val STOPWORDS =
            setOf(
                "de", "du", "des", "le", "la", "les", "a", "au", "aux",
                "en", "et", "ou", "un", "une", "avec", "sans",
                "cru", "crue", "crus", "crues", "sec", "seche", "seches",
                "frais", "fraiche", "fraiches", "nature", "poudre",
                "entier", "entiere", "moulu", "moulue",
            )
    }

    @Serializable
    private data class RecetteJsonDto(
        val nom: String,
        val description: String,
        val tempsPreparationMin: Int,
        val tempsCuissonMin: Int,
        val nbPortions: Int,
        val regimes: List<String>,
        val typeRepas: List<String>,
        val imageUrl: String? = null,
        val ingredients: List<IngredientJsonDto>,
        val etapes: List<String>,
    )

    @Serializable
    private data class IngredientJsonDto(
        val nom: String,
        val quantiteGrammes: Double,
    )

    /**
     * Resultat d'un import.
     *
     * @param insertedCount Nombre de recettes nouvellement inserees
     * @param skippedCount Nombre de recettes ignorees (deja presentes)
     * @param warnings Avertissements (ingredients non matches, etc.)
     */
    data class ImportResult(
        val insertedCount: Int,
        val skippedCount: Int,
        val warnings: List<String>,
    )

    /**
     * Import complet depuis un InputStream JSON (tableau de recettes).
     */
    suspend fun importAll(jsonStream: InputStream): ImportResult {
        val jsonText = jsonStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        val recettesJson = json.decodeFromString<List<RecetteJsonDto>>(jsonText)
        logger.info("Parsing de ${recettesJson.size} recettes depuis le JSON")

        // Charge toute la base Ciqual en memoire (un seul round-trip)
        val allAliments = alimentDao.findAll(limit = ALIMENT_LOAD_LIMIT, offset = 0)
        if (allAliments.isEmpty()) {
            logger.warn("Aucun aliment en base — import recettes annule")
            return ImportResult(0, recettesJson.size, listOf("Base d'aliments vide"))
        }
        logger.info("${allAliments.size} aliments charges pour le matching fuzzy")

        // Pre-normalise les noms d'aliments pour le scoring
        val normalizedAliments: List<Pair<AlimentRow, String>> =
            allAliments.map { it to normalize(it.nom) }

        // Idempotence : collecte les noms de recettes deja publiees
        val existingNames = recetteDao.findAllPublished().map { it.nom }.toHashSet()

        val warnings = mutableListOf<String>()
        var insertedCount = 0
        var skippedCount = 0

        for (recetteJson in recettesJson) {
            if (existingNames.contains(recetteJson.nom)) {
                logger.info("Recette deja presente, skip : '${recetteJson.nom}'")
                skippedCount++
                continue
            }

            try {
                val inserted = insertRecette(recetteJson, normalizedAliments, warnings)
                if (inserted) insertedCount++
            } catch (e: Exception) {
                val msg = "Erreur lors de l'import de la recette '${recetteJson.nom}' : ${e.message}"
                logger.error(msg, e)
                warnings.add(msg)
            }
        }

        logger.info(
            "Import recettes termine : $insertedCount inserees, $skippedCount ignorees, " +
                "${warnings.size} warnings",
        )
        return ImportResult(insertedCount, skippedCount, warnings)
    }

    private suspend fun insertRecette(
        recetteJson: RecetteJsonDto,
        normalizedAliments: List<Pair<AlimentRow, String>>,
        warnings: MutableList<String>,
    ): Boolean {
        val now = Clock.System.now()
        val recetteId = UUID.randomUUID().toString()

        // Matche chaque ingredient et calcule les nutriments totaux
        val matchedIngredients = mutableListOf<Pair<AlimentRow, Double>>()
        for (ingredientJson in recetteJson.ingredients) {
            val match = findBestMatch(ingredientJson.nom, normalizedAliments)
            if (match == null) {
                val warn =
                    "Aucun match pour '${ingredientJson.nom}' dans recette " +
                        "'${recetteJson.nom}' — ingredient ignore (nutriments=0)"
                logger.warn(warn)
                warnings.add(warn)
                continue
            }
            matchedIngredients.add(match to ingredientJson.quantiteGrammes)
        }

        // TODO: remove debug logs after diagnosis — investigating bug 0 kcal en prod
        logger.info("DEBUG matchedIngredients pour '${recetteJson.nom}':")
        for ((aliment, grammes) in matchedIngredients) {
            logger.info(
                "  - '${aliment.nom}' (id=${aliment.id}) calories=${aliment.calories} kcal/100g, " +
                    "${grammes}g => ${aliment.calories * grammes / 100.0} kcal " +
                    "(proteines/100g=${aliment.proteines}, lipides/100g=${aliment.lipides})",
            )
        }

        // Agregation des nutriments : nutriment_total = sum(aliment.nutriment * g / 100)
        val totaux = computeTotaux(matchedIngredients)

        // TODO: remove debug logs after diagnosis
        logger.info(
            "DEBUG totaux pour '${recetteJson.nom}': calories=${totaux["calories"]}, " +
                "proteines=${totaux["proteines"]}, lipides=${totaux["lipides"]}",
        )

        val row =
            RecetteRow(
                id = recetteId,
                nom = recetteJson.nom,
                description = recetteJson.description,
                tempsPreparationMin = recetteJson.tempsPreparationMin,
                tempsCuissonMin = recetteJson.tempsCuissonMin,
                nbPortions = recetteJson.nbPortions,
                regimesCompatibles = recetteJson.regimes.joinToString(","),
                source = SourceRecette.MANUELLE,
                typeRepas = recetteJson.typeRepas.joinToString(","),
                etapes = recetteJson.etapes.joinToString("|||"),
                calories = totaux["calories"] ?: 0.0,
                proteines = totaux["proteines"] ?: 0.0,
                glucides = totaux["glucides"] ?: 0.0,
                lipides = totaux["lipides"] ?: 0.0,
                fibres = totaux["fibres"] ?: 0.0,
                sel = totaux["sel"] ?: 0.0,
                sucres = totaux["sucres"] ?: 0.0,
                fer = totaux["fer"] ?: 0.0,
                calcium = totaux["calcium"] ?: 0.0,
                zinc = totaux["zinc"] ?: 0.0,
                magnesium = totaux["magnesium"] ?: 0.0,
                vitamineB12 = totaux["vitamineB12"] ?: 0.0,
                vitamineD = totaux["vitamineD"] ?: 0.0,
                vitamineC = totaux["vitamineC"] ?: 0.0,
                omega3 = totaux["omega3"] ?: 0.0,
                omega6 = totaux["omega6"] ?: 0.0,
                imageUrl = recetteJson.imageUrl,
                publie = true,
                createdAt = now,
                updatedAt = now,
            )

        val ingredientRows =
            matchedIngredients.map { (aliment, quantite) ->
                IngredientRow(
                    id = UUID.randomUUID().toString(),
                    recetteId = recetteId,
                    alimentId = aliment.id,
                    alimentNom = aliment.nom,
                    quantiteGrammes = quantite,
                )
            }

        recetteDao.insertRecetteWithIngredients(row, ingredientRows)

        logger.info(
            "Recette inseree : '${recetteJson.nom}' avec ${matchedIngredients.size} ingredients, " +
                "${"%.0f".format(row.calories)} kcal totales",
        )
        return true
    }

    private fun computeTotaux(matched: List<Pair<AlimentRow, Double>>): Map<String, Double> {
        var calories = 0.0
        var proteines = 0.0
        var glucides = 0.0
        var lipides = 0.0
        var fibres = 0.0
        var sel = 0.0
        var sucres = 0.0
        var fer = 0.0
        var calcium = 0.0
        var zinc = 0.0
        var magnesium = 0.0
        var vitamineB12 = 0.0
        var vitamineD = 0.0
        var vitamineC = 0.0
        var omega3 = 0.0
        var omega6 = 0.0

        for ((aliment, grammes) in matched) {
            val ratio = grammes / 100.0
            calories += aliment.calories * ratio
            proteines += aliment.proteines * ratio
            glucides += aliment.glucides * ratio
            lipides += aliment.lipides * ratio
            fibres += aliment.fibres * ratio
            sel += aliment.sel * ratio
            sucres += aliment.sucres * ratio
            fer += aliment.fer * ratio
            calcium += aliment.calcium * ratio
            zinc += aliment.zinc * ratio
            magnesium += aliment.magnesium * ratio
            vitamineB12 += aliment.vitamineB12 * ratio
            vitamineD += aliment.vitamineD * ratio
            vitamineC += aliment.vitamineC * ratio
            omega3 += aliment.omega3 * ratio
            omega6 += aliment.omega6 * ratio
        }

        return mapOf(
            "calories" to calories,
            "proteines" to proteines,
            "glucides" to glucides,
            "lipides" to lipides,
            "fibres" to fibres,
            "sel" to sel,
            "sucres" to sucres,
            "fer" to fer,
            "calcium" to calcium,
            "zinc" to zinc,
            "magnesium" to magnesium,
            "vitamineB12" to vitamineB12,
            "vitamineD" to vitamineD,
            "vitamineC" to vitamineC,
            "omega3" to omega3,
            "omega6" to omega6,
        )
    }

    /**
     * Trouve le meilleur aliment en base pour un nom d'ingredient donne.
     *
     * Strategie : score = nombre de mots-cles de l'ingredient (>= 3 chars, hors stopwords)
     * presents dans le nom normalise de l'aliment. En cas d'egalite, on prend le nom le plus
     * court (plus generique). Retourne null si aucun aliment ne partage au moins un mot-cle.
     */
    internal fun findBestMatch(
        ingredientNom: String,
        normalizedAliments: List<Pair<AlimentRow, String>>,
    ): AlimentRow? {
        val keywords = extractKeywords(ingredientNom)
        if (keywords.isEmpty()) return null

        var bestAliment: AlimentRow? = null
        var bestScore = 0
        var bestLength = Int.MAX_VALUE

        for ((aliment, normName) in normalizedAliments) {
            val tokens = normName.split(Regex("\\s+|,|'|-"))
                .filter { it.isNotBlank() }
                .toHashSet()
            var score = 0
            for (kw in keywords) {
                if (tokens.contains(kw) || normName.contains(kw)) score++
            }
            if (score == 0) continue

            val length = aliment.nom.length
            if (score > bestScore || (score == bestScore && length < bestLength)) {
                bestScore = score
                bestLength = length
                bestAliment = aliment
            }
        }

        return if (bestScore > 0) bestAliment else null
    }

    private fun extractKeywords(raw: String): List<String> {
        val normalized = normalize(raw)
        return normalized
            .split(Regex("\\s+|,|'|-"))
            .map { it.trim() }
            .filter { it.length >= MIN_KEYWORD_LENGTH && it !in STOPWORDS }
    }

    /**
     * Lower-case + suppression des accents.
     */
    private fun normalize(input: String): String {
        val lowered = input.lowercase()
        val decomposed = Normalizer.normalize(lowered, Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
