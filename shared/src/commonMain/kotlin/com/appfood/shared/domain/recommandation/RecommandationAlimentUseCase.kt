package com.appfood.shared.domain.recommandation

import com.appfood.shared.model.Aliment
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.QuotaStatus
import com.appfood.shared.model.RecommandationAliment
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.UserPreferences
import com.appfood.shared.util.AppResult
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Recommends foods to fill nutritional deficits for the day.
 * Uses a scoring algorithm that considers deficit severity,
 * dietary regime critical nutrients, and food diversity.
 *
 * Pure function — no side effects. Depends only on input data.
 */
class RecommandationAlimentUseCase {

    /**
     * @param quotaStatuses Current day's quota statuses (consumed vs target)
     * @param aliments Available foods (already filtered by regime compatibility from repository)
     * @param regime User's dietary regime
     * @param preferences User's food preferences (exclusions, allergies)
     * @param limit Max number of recommendations to return (default 10)
     */
    fun execute(
        quotaStatuses: List<QuotaStatus>,
        aliments: List<Aliment>,
        regime: RegimeAlimentaire,
        preferences: UserPreferences,
        limit: Int = 10,
    ): AppResult<List<RecommandationAliment>> {
        return try {
            val recommandations = calculerRecommandations(
                quotaStatuses = quotaStatuses,
                aliments = aliments,
                regime = regime,
                preferences = preferences,
                limit = limit,
            )
            AppResult.Success(recommandations)
        } catch (e: Exception) {
            AppResult.Error(
                code = "RECOMMANDATION_ERROR",
                message = "Failed to compute food recommendations: ${e.message}",
                cause = e,
            )
        }
    }

    /**
     * Core recommendation algorithm. Visible for testing.
     */
    internal fun calculerRecommandations(
        quotaStatuses: List<QuotaStatus>,
        aliments: List<Aliment>,
        regime: RegimeAlimentaire,
        preferences: UserPreferences,
        limit: Int,
    ): List<RecommandationAliment> {
        // 1. Identify deficits
        val deficits = identifierDeficits(quotaStatuses)
        if (deficits.isEmpty()) return emptyList()

        // 2. Get critical nutrients for the regime
        val nutrimentsCritiques = getNutrimentsCritiques(regime)

        // 3. Calculate weights for each deficit
        val poidsDeficits = calculerPoidsDeficits(deficits, nutrimentsCritiques)

        // 4. Filter foods
        val alimentsFiltres = filtrerAliments(aliments, regime, preferences)

        // 5. Score each food
        val scoredAliments = alimentsFiltres.mapNotNull { aliment ->
            scoreAliment(aliment, deficits, poidsDeficits, quotaStatuses)
        }

        // 6. Sort by score descending
        val sorted = scoredAliments.sortedByDescending { it.second }

        // 7. Apply diversity constraint: max 2 foods per category
        val diversified = appliquerDiversite(sorted)

        // 8. Take top N
        return diversified.take(limit).map { it.first }
    }

    // --- Deficit identification ---

    internal data class Deficit(
        val nutriment: NutrimentType,
        val niveau: NiveauDeficit,
        val manque: Double,
        val pourcentageAtteint: Double,
    )

    internal enum class NiveauDeficit {
        FORT,   // < 70%
        MODERE, // 70-90%
    }

    internal fun identifierDeficits(quotaStatuses: List<QuotaStatus>): List<Deficit> {
        return quotaStatuses.mapNotNull { status ->
            if (status.valeurCible <= 0) return@mapNotNull null
            val pourcentage = status.pourcentage
            val manque = status.valeurCible - status.valeurConsommee

            when {
                pourcentage < RecommandationConstants.SEUIL_DEFICIT_FORT -> Deficit(
                    nutriment = status.nutriment,
                    niveau = NiveauDeficit.FORT,
                    manque = manque,
                    pourcentageAtteint = pourcentage,
                )
                pourcentage < RecommandationConstants.SEUIL_DEFICIT_MODERE -> Deficit(
                    nutriment = status.nutriment,
                    niveau = NiveauDeficit.MODERE,
                    manque = manque,
                    pourcentageAtteint = pourcentage,
                )
                else -> null // >= 90%, no deficit
            }
        }
    }

    // --- Critical nutrients per regime ---

    internal fun getNutrimentsCritiques(regime: RegimeAlimentaire): Set<NutrimentType> {
        return RecommandationConstants.getNutrimentsCritiques(regime)
    }

    // --- Deficit weight calculation ---

    internal fun calculerPoidsDeficits(
        deficits: List<Deficit>,
        nutrimentsCritiques: Set<NutrimentType>,
    ): Map<NutrimentType, Double> {
        return deficits.associate { deficit ->
            val poids = when {
                deficit.niveau == NiveauDeficit.FORT && deficit.nutriment in nutrimentsCritiques -> RecommandationConstants.POIDS_FORT_CRITIQUE
                deficit.niveau == NiveauDeficit.FORT -> RecommandationConstants.POIDS_FORT_NORMAL
                else -> RecommandationConstants.POIDS_MODERE
            }
            deficit.nutriment to poids
        }
    }

    // --- Food filtering ---

    internal fun filtrerAliments(
        aliments: List<Aliment>,
        regime: RegimeAlimentaire,
        preferences: UserPreferences,
    ): List<Aliment> {
        val exclus = preferences.alimentsExclus.toSet()
        val allergies = preferences.allergies.map { it.lowercase() }

        return aliments.filter { aliment ->
            // 1. Regime compatible (a VEGAN food is also compatible with VEGETARIEN, FLEXITARIEN, OMNIVORE)
            val regimeCompatible = aliment.regimesCompatibles.contains(regime) ||
                isRegimeInferieurCompatible(aliment.regimesCompatibles, regime)

            // 2. Not excluded
            val nonExclu = aliment.id !in exclus

            // 3. No allergen match
            val sansAllergene = !hasAllergene(aliment, allergies)

            regimeCompatible && nonExclu && sansAllergene
        }
    }

    /**
     * A food compatible with a stricter regime is also compatible with less strict ones.
     * Hierarchy: VEGAN < VEGETARIEN < FLEXITARIEN < OMNIVORE
     */
    private fun isRegimeInferieurCompatible(
        alimentRegimes: List<RegimeAlimentaire>,
        userRegime: RegimeAlimentaire,
    ): Boolean {
        val hierarchy = listOf(
            RegimeAlimentaire.VEGAN,
            RegimeAlimentaire.VEGETARIEN,
            RegimeAlimentaire.FLEXITARIEN,
            RegimeAlimentaire.OMNIVORE,
        )
        val userIndex = hierarchy.indexOf(userRegime)
        return alimentRegimes.any { hierarchy.indexOf(it) <= userIndex }
    }

    /**
     * Check if a food matches any of the user's declared allergies
     * based on category pattern matching.
     */
    internal fun hasAllergene(aliment: Aliment, allergies: List<String>): Boolean {
        val categorieLower = aliment.categorie.lowercase()
        val nomLower = aliment.nom.lowercase()

        for (allergie in allergies) {
            val patterns = getAllergyPatterns(allergie)
            val isMatch = patterns.any { pattern -> categorieLower.contains(pattern) }

            // Exception: "sans gluten" in food name exempts it from gluten allergy
            if (allergie == "gluten" && nomLower.contains("sans gluten")) continue

            if (isMatch) return true
        }
        return false
    }

    private fun getAllergyPatterns(allergie: String): List<String> {
        return RecommandationConstants.ALLERGEN_PATTERNS[allergie] ?: listOf(allergie)
    }

    // --- Scoring ---

    /**
     * Scores a food based on how well it fills the deficits.
     * Returns null if the food doesn't contribute to any deficit.
     */
    internal fun scoreAliment(
        aliment: Aliment,
        deficits: List<Deficit>,
        poidsDeficits: Map<NutrimentType, Double>,
        quotaStatuses: List<QuotaStatus>,
    ): Pair<RecommandationAliment, Double>? {
        val nutriments = aliment.nutrimentsPour100g

        // Calculate suggested quantity based on the primary nutrient
        val quantiteSuggeree = calculerQuantiteSuggeree(aliment, deficits)
        if (quantiteSuggeree <= 0) return null

        // Calculate score
        var score = 0.0
        val nutrimentsCibles = mutableListOf<NutrimentType>()
        val pourcentageCouverture = mutableMapOf<NutrimentType, Double>()

        for (deficit in deficits) {
            val apportPour100g = nutriments.getByType(deficit.nutriment)
            if (apportPour100g <= 0) continue

            val apport = apportPour100g * (quantiteSuggeree / 100.0)
            val contribution = min(apport / deficit.manque, 1.0)
            val poids = (poidsDeficits[deficit.nutriment] ?: 1.0)

            score += contribution * poids

            if (contribution > 0.0) {
                nutrimentsCibles.add(deficit.nutriment)
                pourcentageCouverture[deficit.nutriment] = min(
                    (apport / deficit.manque) * 100.0,
                    100.0,
                )
            }
        }

        if (score <= 0 || nutrimentsCibles.isEmpty()) return null

        val recommandation = RecommandationAliment(
            aliment = aliment,
            nutrimentsCibles = nutrimentsCibles,
            quantiteSuggereGrammes = quantiteSuggeree,
            pourcentageCouverture = pourcentageCouverture,
        )

        return recommandation to score
    }

    /**
     * Calculate the suggested quantity (in grams) to fill the main deficit.
     * Capped at 300g, rounded to nearest 10g.
     */
    internal fun calculerQuantiteSuggeree(aliment: Aliment, deficits: List<Deficit>): Double {
        val nutriments = aliment.nutrimentsPour100g

        // Find the nutrient in deficit for which this food has the highest density
        var bestQuantite = 300.0 // cap
        var hasDensite = false

        for (deficit in deficits) {
            val densitePour100g = nutriments.getByType(deficit.nutriment)
            if (densitePour100g <= 0) continue

            val quantitePourCombler = (deficit.manque / densitePour100g) * 100.0
            val quantiteCappee = min(quantitePourCombler, 300.0)

            if (!hasDensite || quantiteCappee < bestQuantite) {
                bestQuantite = quantiteCappee
                hasDensite = true
            }
        }

        if (!hasDensite) return 0.0

        // Round to nearest 10g, minimum 10g
        val arrondi = ((bestQuantite / 10.0).roundToInt() * 10).toDouble()
        return if (arrondi < 10.0) 10.0 else arrondi
    }

    // --- Diversity ---

    /**
     * Ensures no more than 2 foods from the same category in the results.
     */
    internal fun appliquerDiversite(
        scored: List<Pair<RecommandationAliment, Double>>,
    ): List<Pair<RecommandationAliment, Double>> {
        val categoryCounts = mutableMapOf<String, Int>()
        return scored.filter { (reco, _) ->
            val cat = reco.aliment.categorie
            val count = (categoryCounts[cat] ?: 0)
            if (count < 2) {
                categoryCounts[cat] = count + 1
                true
            } else {
                false
            }
        }
    }
}
