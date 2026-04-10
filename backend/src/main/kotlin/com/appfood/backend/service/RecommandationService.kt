package com.appfood.backend.service

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.database.dao.RecetteRow
import com.appfood.backend.database.dao.UserPreferencesDao
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.plugins.NotFoundException
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import kotlin.math.min
import kotlin.math.roundToInt

class RecommandationService(
    private val quotaService: QuotaService,
    private val alimentDao: AlimentDao,
    private val recetteDao: RecetteDao,
    private val userProfileDao: UserProfileDao,
    private val userPreferencesDao: UserPreferencesDao,
) {
    private val logger = LoggerFactory.getLogger("RecommandationService")

    /**
     * Returns recommended aliments to fill nutritional gaps for the given date.
     */
    suspend fun getRecommandationsAliments(
        userId: String,
        date: LocalDate,
        limit: Int = 10,
    ): RecommandationAlimentResult {
        val profile =
            userProfileDao.findByUserId(userId)
                ?: throw NotFoundException("Profil non trouve. Creez d'abord un profil via POST /users/me/profile.")

        val quotaStatuses = quotaService.getQuotaStatus(userId, date)
        val deficits = identifyDeficits(quotaStatuses, profile.regimeAlimentaire)

        if (deficits.isEmpty()) {
            return RecommandationAlimentResult(
                manquesIdentifies = emptyList(),
                recommandations = emptyList(),
            )
        }

        val manquesIdentifies = deficits.map { it.nutriment.name }

        // Load preferences for filtering
        val preferences = userPreferencesDao.findByUserId(userId)
        val alimentsExclus = preferences?.let { deserializeList(it.alimentsExclus) } ?: emptyList()
        val allergies = preferences?.let { deserializeList(it.allergies) } ?: emptyList()

        // PERF-01: SQL pre-filter - load at most ~200 candidates per deficit nutrient,
        // pre-sorted DESC by that nutrient. Fusion deduplique par id, cap a 400 total.
        val t0 = System.currentTimeMillis()
        val regimeFilter = profile.regimeAlimentaire.name
        val candidatesById = linkedMapOf<String, AlimentRow>()
        for (deficit in deficits) {
            val nutrientKey = nutrimentTypeToKey(deficit.nutriment) ?: continue
            val chunk =
                try {
                    alimentDao.findCandidatesByNutrientDeficit(
                        nutrient = nutrientKey,
                        regime = regimeFilter,
                        limit = CANDIDATE_LIMIT_PER_DEFICIT,
                    )
                } catch (e: Exception) {
                    logger.warn("findCandidatesByNutrientDeficit failed for $nutrientKey: ${e.message}")
                    emptyList()
                }
            for (row in chunk) {
                if (candidatesById.size >= CANDIDATE_POOL_MAX) break
                candidatesById.putIfAbsent(row.id, row)
            }
            if (candidatesById.size >= CANDIDATE_POOL_MAX) break
        }
        val candidates = candidatesById.values.toList()
        logger.info(
            "RecommandationService/aliments candidates_loaded=${candidates.size} " +
                "deficits=${deficits.size} in=${System.currentTimeMillis() - t0}ms",
        )

        // Apply user preferences + regime + allergen filters on the reduced pool
        val compatibleAliments =
            candidates.filter { aliment ->
                isRegimeCompatible(aliment.regimesCompatibles, profile.regimeAlimentaire) &&
                    aliment.id !in alimentsExclus &&
                    !isAllergenExcluded(aliment.categorie, allergies)
            }

        // Score each aliment
        val scored =
            compatibleAliments.mapNotNull { aliment ->
                scoreAliment(aliment, deficits)
            }.sortedByDescending { it.score }

        // Apply diversity: max 2 per category in top results
        val diversified = applyDiversity(scored, maxPerCategorie = 2, targetCount = limit)

        return RecommandationAlimentResult(
            manquesIdentifies = manquesIdentifies,
            recommandations = diversified,
        )
    }

    /**
     * Returns recommended recettes to fill nutritional gaps for the given date.
     */
    suspend fun getRecommandationsRecettes(
        userId: String,
        date: LocalDate,
        limit: Int = 5,
    ): RecommandationRecetteResult {
        val profile =
            userProfileDao.findByUserId(userId)
                ?: throw NotFoundException("Profil non trouve. Creez d'abord un profil via POST /users/me/profile.")

        val quotaStatuses = quotaService.getQuotaStatus(userId, date)
        val deficits = identifyDeficits(quotaStatuses, profile.regimeAlimentaire)

        if (deficits.isEmpty()) {
            return RecommandationRecetteResult(
                manquesIdentifies = emptyList(),
                recommandations = emptyList(),
            )
        }

        val manquesIdentifies = deficits.map { it.nutriment.name }

        // PERF-01: SQL pre-filter for recettes - ~50 candidates per deficit, dedup, cap 150.
        val t0 = System.currentTimeMillis()
        val regimeFilter = profile.regimeAlimentaire.name
        val candidatesById = linkedMapOf<String, RecetteRow>()
        for (deficit in deficits) {
            val nutrientKey = nutrimentTypeToKey(deficit.nutriment) ?: continue
            val chunk =
                try {
                    recetteDao.findRecetteCandidatesByNutrientDeficit(
                        nutrient = nutrientKey,
                        regime = regimeFilter,
                        limit = RECETTE_CANDIDATE_LIMIT_PER_DEFICIT,
                    )
                } catch (e: Exception) {
                    logger.warn("findRecetteCandidatesByNutrientDeficit failed for $nutrientKey: ${e.message}")
                    emptyList()
                }
            for (row in chunk) {
                if (candidatesById.size >= RECETTE_CANDIDATE_POOL_MAX) break
                candidatesById.putIfAbsent(row.id, row)
            }
            if (candidatesById.size >= RECETTE_CANDIDATE_POOL_MAX) break
        }
        val candidates = candidatesById.values.toList()
        logger.info(
            "RecommandationService/recettes candidates_loaded=${candidates.size} " +
                "deficits=${deficits.size} in=${System.currentTimeMillis() - t0}ms",
        )

        val compatibleRecettes =
            candidates.filter { recette ->
                isRegimeCompatible(recette.regimesCompatibles, profile.regimeAlimentaire)
            }

        // Score each recette
        val scored =
            compatibleRecettes.mapNotNull { recette ->
                scoreRecette(recette, deficits)
            }.sortedByDescending { it.score }

        return RecommandationRecetteResult(
            manquesIdentifies = manquesIdentifies,
            recommandations = scored.take(limit),
        )
    }

    private fun nutrimentTypeToKey(type: NutrimentType): String? {
        return when (type) {
            NutrimentType.PROTEINES -> "proteines"
            NutrimentType.FER -> "fer"
            NutrimentType.CALCIUM -> "calcium"
            NutrimentType.VITAMINE_B12 -> "vitamineB12"
            NutrimentType.FIBRES -> "fibres"
            NutrimentType.ZINC -> "zinc"
            NutrimentType.MAGNESIUM -> "magnesium"
            NutrimentType.VITAMINE_D -> "vitamineD"
            NutrimentType.OMEGA_3 -> "omega3"
            NutrimentType.VITAMINE_C -> "vitamineC"
            // Macros not targetted by candidate pre-filtering
            NutrimentType.CALORIES,
            NutrimentType.GLUCIDES,
            NutrimentType.LIPIDES,
            NutrimentType.SEL,
            NutrimentType.SUCRES,
            NutrimentType.OMEGA_6,
            -> null
        }
    }

    // --- Deficit identification ---

    private fun identifyDeficits(
        quotaStatuses: List<QuotaStatusResult>,
        regime: RegimeAlimentaire,
    ): List<NutrientDeficit> {
        val critiques = getCriticalNutrients(regime)

        return quotaStatuses.mapNotNull { status ->
            val pourcentage = status.pourcentage
            val isCritique = status.nutriment in critiques

            when {
                pourcentage < 70.0 -> {
                    val poids = if (isCritique) 3.0 else 2.0
                    NutrientDeficit(
                        nutriment = status.nutriment,
                        manque = status.valeurCible - status.valeurConsommee,
                        quota = status.valeurCible,
                        pourcentageAtteint = pourcentage,
                        poids = poids,
                    )
                }
                pourcentage < 90.0 -> {
                    NutrientDeficit(
                        nutriment = status.nutriment,
                        manque = status.valeurCible - status.valeurConsommee,
                        quota = status.valeurCible,
                        pourcentageAtteint = pourcentage,
                        poids = 1.0,
                    )
                }
                else -> null
            }
        }
    }

    private fun getCriticalNutrients(regime: RegimeAlimentaire): Set<NutrimentType> {
        return when (regime) {
            RegimeAlimentaire.VEGAN ->
                setOf(
                    NutrimentType.VITAMINE_B12,
                    NutrimentType.FER,
                    NutrimentType.ZINC,
                    NutrimentType.OMEGA_3,
                    NutrimentType.CALCIUM,
                    NutrimentType.PROTEINES,
                )
            RegimeAlimentaire.VEGETARIEN ->
                setOf(
                    NutrimentType.VITAMINE_B12,
                    NutrimentType.FER,
                    NutrimentType.ZINC,
                    NutrimentType.OMEGA_3,
                )
            RegimeAlimentaire.FLEXITARIEN -> emptySet()
            RegimeAlimentaire.OMNIVORE -> emptySet()
        }
    }

    // --- Aliment scoring ---

    private fun scoreAliment(
        aliment: AlimentRow,
        deficits: List<NutrientDeficit>,
    ): ScoredAliment? {
        var totalScore = 0.0
        val nutrimentsCibles = mutableListOf<String>()
        val pourcentageCouverture = mutableMapOf<String, Double>()

        // Find the deficit for which this aliment has the highest density (for quantity suggestion)
        var bestDensityNutriment: NutrientDeficit? = null
        var bestDensity = 0.0

        for (deficit in deficits) {
            val densitePour100g = getAlimentNutrientValue(aliment, deficit.nutriment)
            if (densitePour100g <= 0.0 || deficit.manque <= 0.0) continue

            val density = densitePour100g / deficit.manque
            if (density > bestDensity) {
                bestDensity = density
                bestDensityNutriment = deficit
            }
        }

        if (bestDensityNutriment == null) return null

        // Calculate suggested quantity based on the principal nutrient
        val principalDensity = getAlimentNutrientValue(aliment, bestDensityNutriment.nutriment)
        val rawQuantite =
            if (principalDensity > 0.0) {
                (bestDensityNutriment.manque / principalDensity) * 100.0
            } else {
                100.0
            }
        val quantiteSuggereGrammes = roundToTen(min(rawQuantite, 300.0))

        // Now score with the suggested quantity
        for (deficit in deficits) {
            val densitePour100g = getAlimentNutrientValue(aliment, deficit.nutriment)
            if (densitePour100g <= 0.0 || deficit.manque <= 0.0) continue

            val apport = densitePour100g * (quantiteSuggereGrammes / 100.0)
            val contribution = min(apport / deficit.manque, 1.0)
            totalScore += contribution * deficit.poids
            nutrimentsCibles.add(deficit.nutriment.name)

            val couverturePct = min((apport / deficit.manque) * 100.0, 100.0)
            pourcentageCouverture[deficit.nutriment.name] = (couverturePct * 10).roundToInt() / 10.0
        }

        if (totalScore <= 0.0) return null

        return ScoredAliment(
            aliment = aliment,
            score = totalScore,
            nutrimentsCibles = nutrimentsCibles,
            quantiteSuggereGrammes = quantiteSuggereGrammes,
            pourcentageCouverture = pourcentageCouverture,
        )
    }

    // --- Recette scoring ---

    private fun scoreRecette(
        recette: RecetteRow,
        deficits: List<NutrientDeficit>,
    ): ScoredRecette? {
        if (recette.nbPortions <= 0) return null

        var totalScore = 0.0
        val nutrimentsCibles = mutableListOf<String>()
        val pourcentageCouverture = mutableMapOf<String, Double>()
        var totalCouverture = 0.0
        var countDeficitsCouverts = 0

        for (deficit in deficits) {
            val nutrimentParPortion = getRecetteNutrientValue(recette, deficit.nutriment) / recette.nbPortions
            if (nutrimentParPortion <= 0.0 || deficit.manque <= 0.0) continue

            val contribution = min(nutrimentParPortion / deficit.manque, 1.0)
            totalScore += contribution * deficit.poids
            nutrimentsCibles.add(deficit.nutriment.name)

            val couverturePct = min((nutrimentParPortion / deficit.manque) * 100.0, 100.0)
            pourcentageCouverture[deficit.nutriment.name] = (couverturePct * 10).roundToInt() / 10.0
            totalCouverture += couverturePct
            countDeficitsCouverts++
        }

        if (totalScore <= 0.0) return null

        val pourcentageCouvertureGlobal =
            if (countDeficitsCouverts > 0) {
                (totalCouverture / countDeficitsCouverts * 10).roundToInt() / 10.0
            } else {
                0.0
            }

        return ScoredRecette(
            recette = recette,
            score = totalScore,
            nutrimentsCibles = nutrimentsCibles,
            pourcentageCouvertureGlobal = pourcentageCouvertureGlobal,
            pourcentageCouverture = pourcentageCouverture,
        )
    }

    // --- Filtering helpers ---

    private fun isRegimeCompatible(
        regimesCompatiblesJson: String,
        userRegime: RegimeAlimentaire,
    ): Boolean {
        // regimesCompatibles is stored as a JSON array string e.g. ["VEGAN","VEGETARIEN"]
        // A VEGAN aliment is compatible with all regimes
        val regimesStr = regimesCompatiblesJson.uppercase()
        return when (userRegime) {
            RegimeAlimentaire.VEGAN -> regimesStr.contains("VEGAN")
            RegimeAlimentaire.VEGETARIEN -> regimesStr.contains("VEGAN") || regimesStr.contains("VEGETARIEN")
            RegimeAlimentaire.FLEXITARIEN -> regimesStr.contains("VEGAN") || regimesStr.contains("VEGETARIEN") || regimesStr.contains("FLEXITARIEN")
            RegimeAlimentaire.OMNIVORE -> true // everything is compatible for omnivore
        }
    }

    private fun isAllergenExcluded(
        categorie: String,
        allergies: List<String>,
    ): Boolean {
        val catLower = categorie.lowercase()
        for (allergie in allergies) {
            val patterns = ALLERGEN_PATTERNS[allergie.lowercase()] ?: continue
            for (pattern in patterns) {
                if (catLower.contains(pattern)) {
                    // Special case: "sans gluten" in name should not be excluded
                    if (allergie.lowercase() == "gluten" && catLower.contains("sans gluten")) continue
                    return true
                }
            }
        }
        return false
    }

    // --- Diversity filter ---

    private fun applyDiversity(
        scored: List<ScoredAliment>,
        maxPerCategorie: Int,
        targetCount: Int,
    ): List<ScoredAliment> {
        val result = mutableListOf<ScoredAliment>()
        val categoryCounts = mutableMapOf<String, Int>()

        for (item in scored) {
            if (result.size >= targetCount) break
            val cat = item.aliment.categorie
            val currentCount = categoryCounts.getOrDefault(cat, 0)
            if (currentCount < maxPerCategorie) {
                result.add(item)
                categoryCounts[cat] = currentCount + 1
            }
        }
        return result
    }

    // --- Nutrient value accessors ---

    private fun getAlimentNutrientValue(
        aliment: AlimentRow,
        nutriment: NutrimentType,
    ): Double {
        return when (nutriment) {
            NutrimentType.CALORIES -> aliment.calories
            NutrimentType.PROTEINES -> aliment.proteines
            NutrimentType.GLUCIDES -> aliment.glucides
            NutrimentType.LIPIDES -> aliment.lipides
            NutrimentType.FIBRES -> aliment.fibres
            NutrimentType.SEL -> aliment.sel
            NutrimentType.SUCRES -> aliment.sucres
            NutrimentType.FER -> aliment.fer
            NutrimentType.CALCIUM -> aliment.calcium
            NutrimentType.ZINC -> aliment.zinc
            NutrimentType.MAGNESIUM -> aliment.magnesium
            NutrimentType.VITAMINE_B12 -> aliment.vitamineB12
            NutrimentType.VITAMINE_D -> aliment.vitamineD
            NutrimentType.VITAMINE_C -> aliment.vitamineC
            NutrimentType.OMEGA_3 -> aliment.omega3
            NutrimentType.OMEGA_6 -> aliment.omega6
        }
    }

    private fun getRecetteNutrientValue(
        recette: RecetteRow,
        nutriment: NutrimentType,
    ): Double {
        return when (nutriment) {
            NutrimentType.CALORIES -> recette.calories
            NutrimentType.PROTEINES -> recette.proteines
            NutrimentType.GLUCIDES -> recette.glucides
            NutrimentType.LIPIDES -> recette.lipides
            NutrimentType.FIBRES -> recette.fibres
            NutrimentType.SEL -> recette.sel
            NutrimentType.SUCRES -> recette.sucres
            NutrimentType.FER -> recette.fer
            NutrimentType.CALCIUM -> recette.calcium
            NutrimentType.ZINC -> recette.zinc
            NutrimentType.MAGNESIUM -> recette.magnesium
            NutrimentType.VITAMINE_B12 -> recette.vitamineB12
            NutrimentType.VITAMINE_D -> recette.vitamineD
            NutrimentType.VITAMINE_C -> recette.vitamineC
            NutrimentType.OMEGA_3 -> recette.omega3
            NutrimentType.OMEGA_6 -> recette.omega6
        }
    }

    private fun roundToTen(value: Double): Double {
        return ((value / 10.0).roundToInt() * 10).toDouble()
    }

    private fun deserializeList(jsonStr: String): List<String> {
        return try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        // PERF-01 candidate pool sizing
        private const val CANDIDATE_LIMIT_PER_DEFICIT = 200
        private const val CANDIDATE_POOL_MAX = 400
        private const val RECETTE_CANDIDATE_LIMIT_PER_DEFICIT = 50
        private const val RECETTE_CANDIDATE_POOL_MAX = 150

        private val ALLERGEN_PATTERNS =
            mapOf(
                "gluten" to listOf("ble", "seigle", "orge", "avoine"),
                "soja" to listOf("soja"),
                "arachides" to listOf("arachide", "cacahuete"),
                "fruits_a_coque" to listOf("noix", "amande", "noisette", "cajou", "pistache", "pecan", "macadamia"),
                "lait" to listOf("lait", "fromage", "yaourt", "beurre", "creme"),
                "oeufs" to listOf("oeuf", "egg"),
            )
    }
}

// --- Internal data classes ---

data class NutrientDeficit(
    val nutriment: NutrimentType,
    val manque: Double,
    val quota: Double,
    val pourcentageAtteint: Double,
    val poids: Double,
)

data class ScoredAliment(
    val aliment: AlimentRow,
    val score: Double,
    val nutrimentsCibles: List<String>,
    val quantiteSuggereGrammes: Double,
    val pourcentageCouverture: Map<String, Double>,
)

data class ScoredRecette(
    val recette: RecetteRow,
    val score: Double,
    val nutrimentsCibles: List<String>,
    val pourcentageCouvertureGlobal: Double,
    val pourcentageCouverture: Map<String, Double>,
)

data class RecommandationAlimentResult(
    val manquesIdentifies: List<String>,
    val recommandations: List<ScoredAliment>,
)

data class RecommandationRecetteResult(
    val manquesIdentifies: List<String>,
    val recommandations: List<ScoredRecette>,
)
