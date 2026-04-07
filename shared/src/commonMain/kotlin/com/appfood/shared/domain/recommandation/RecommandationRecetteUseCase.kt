package com.appfood.shared.domain.recommandation

import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.QuotaStatus
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RecommandationRecette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.util.AppResult
import kotlin.math.min

/**
 * Recommends recipes to fill nutritional deficits for the day (RECO-02).
 *
 * Algorithm:
 * 1. Load nutritional deficits (quotas - consumed via QuotaStatus)
 * 2. Filter recipes by dietary regime
 * 3. Score each recipe: mean coverage of deficit nutrients per portion
 * 4. Return top N recipes with their scores
 *
 * Pure function — no side effects. Depends only on input data.
 */
class RecommandationRecetteUseCase {

    /**
     * @param quotaStatuses Current day's quota statuses (consumed vs target)
     * @param recettes Available recipes
     * @param regime User's dietary regime
     * @param limit Max number of recommendations to return (default 5)
     */
    fun execute(
        quotaStatuses: List<QuotaStatus>,
        recettes: List<Recette>,
        regime: RegimeAlimentaire,
        limit: Int = 5,
    ): AppResult<List<RecommandationRecette>> {
        return try {
            val recommandations = calculerRecommandations(
                quotaStatuses = quotaStatuses,
                recettes = recettes,
                regime = regime,
                limit = limit,
            )
            AppResult.Success(recommandations)
        } catch (e: Exception) {
            AppResult.Error(
                code = "RECOMMANDATION_RECETTE_ERROR",
                message = "Failed to compute recipe recommendations: ${e.message}",
                cause = e,
            )
        }
    }

    /**
     * Core recommendation algorithm. Visible for testing.
     */
    internal fun calculerRecommandations(
        quotaStatuses: List<QuotaStatus>,
        recettes: List<Recette>,
        regime: RegimeAlimentaire,
        limit: Int,
    ): List<RecommandationRecette> {
        // 1. Identify deficits (nutrients < 90% of target)
        val deficits = identifierDeficits(quotaStatuses)
        if (deficits.isEmpty()) return emptyList()

        // 2. Filter recipes by regime compatibility
        val recettesFiltrees = filtrerParRegime(recettes, regime)

        // 3. Score each recipe
        val scoredRecettes = recettesFiltrees.mapNotNull { recette ->
            scoreRecette(recette, deficits, quotaStatuses)
        }

        // 4. Sort by global coverage descending and take top N
        return scoredRecettes
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    // --- Deficit identification ---

    internal data class Deficit(
        val nutriment: NutrimentType,
        val manque: Double,
        val pourcentageAtteint: Double,
    )

    internal fun identifierDeficits(quotaStatuses: List<QuotaStatus>): List<Deficit> {
        return quotaStatuses.mapNotNull { status ->
            if (status.valeurCible <= 0) return@mapNotNull null
            val manque = status.valeurCible - status.valeurConsommee
            if (status.pourcentage < 90.0 && manque > 0) {
                Deficit(
                    nutriment = status.nutriment,
                    manque = manque,
                    pourcentageAtteint = status.pourcentage,
                )
            } else {
                null
            }
        }
    }

    // --- Regime filtering ---

    /**
     * Filter recipes compatible with the user's regime.
     * A recipe compatible with VEGAN is also compatible with VEGETARIEN, FLEXITARIEN, OMNIVORE.
     */
    internal fun filtrerParRegime(
        recettes: List<Recette>,
        regime: RegimeAlimentaire,
    ): List<Recette> {
        val hierarchy = listOf(
            RegimeAlimentaire.VEGAN,
            RegimeAlimentaire.VEGETARIEN,
            RegimeAlimentaire.FLEXITARIEN,
            RegimeAlimentaire.OMNIVORE,
        )
        val userIndex = hierarchy.indexOf(regime)

        return recettes.filter { recette ->
            recette.regimesCompatibles.contains(regime) ||
                recette.regimesCompatibles.any { hierarchy.indexOf(it) <= userIndex }
        }
    }

    // --- Scoring ---

    /**
     * Scores a recipe based on mean coverage of deficit nutrients per portion.
     * Returns null if the recipe doesn't contribute to any deficit.
     */
    internal fun scoreRecette(
        recette: Recette,
        deficits: List<Deficit>,
        quotaStatuses: List<QuotaStatus>,
    ): Pair<RecommandationRecette, Double>? {
        // Calculate nutriments per portion
        val nbPortions = recette.nbPortions.coerceAtLeast(1)
        val nutrimentsParPortion = recette.nutrimentsTotaux.divideBy(nbPortions)

        val nutrimentsCibles = mutableListOf<NutrimentType>()
        val pourcentageCouverture = mutableMapOf<NutrimentType, Double>()
        var totalCoverage = 0.0

        for (deficit in deficits) {
            val apportParPortion = nutrimentsParPortion.getByType(deficit.nutriment)
            if (apportParPortion <= 0) continue

            val coverage = min((apportParPortion / deficit.manque) * 100.0, 100.0)
            nutrimentsCibles.add(deficit.nutriment)
            pourcentageCouverture[deficit.nutriment] = coverage
            totalCoverage += coverage
        }

        if (nutrimentsCibles.isEmpty()) return null

        val pourcentageCouvertureGlobal = totalCoverage / deficits.size

        val recommandation = RecommandationRecette(
            recette = recette,
            nutrimentsCibles = nutrimentsCibles,
            pourcentageCouvertureGlobal = pourcentageCouvertureGlobal,
            pourcentageCouverture = pourcentageCouverture,
        )

        return recommandation to pourcentageCouvertureGlobal
    }

    /**
     * Divides all nutriment values by a divisor (for per-portion calculation).
     */
    private fun NutrimentValues.divideBy(divisor: Int): NutrimentValues {
        val d = divisor.toDouble()
        return NutrimentValues(
            calories = calories / d,
            proteines = proteines / d,
            glucides = glucides / d,
            lipides = lipides / d,
            fibres = fibres / d,
            sel = sel / d,
            sucres = sucres / d,
            fer = fer / d,
            calcium = calcium / d,
            zinc = zinc / d,
            magnesium = magnesium / d,
            vitamineB12 = vitamineB12 / d,
            vitamineD = vitamineD / d,
            vitamineC = vitamineC / d,
            omega3 = omega3 / d,
            omega6 = omega6 / d,
        )
    }
}
