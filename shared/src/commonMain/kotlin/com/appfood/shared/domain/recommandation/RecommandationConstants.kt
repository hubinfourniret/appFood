package com.appfood.shared.domain.recommandation

import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.RegimeAlimentaire

/**
 * Constantes partagees pour les algorithmes de recommandation.
 * Utilisees par shared (RecommandationAlimentUseCase) et backend (RecommandationService)
 * pour garantir la coherence des seuils, nutriments critiques et patterns d'allergenes.
 */
object RecommandationConstants {

    /** Seuil en dessous duquel un deficit est considere FORT (< 70%) */
    const val SEUIL_DEFICIT_FORT = 70.0

    /** Seuil en dessous duquel un deficit est considere MODERE (70-90%) */
    const val SEUIL_DEFICIT_MODERE = 90.0

    /** Poids d'un deficit FORT sur un nutriment critique */
    const val POIDS_FORT_CRITIQUE = 3.0

    /** Poids d'un deficit FORT sur un nutriment non-critique */
    const val POIDS_FORT_NORMAL = 2.0

    /** Poids d'un deficit MODERE */
    const val POIDS_MODERE = 1.0

    /** Quantite max suggeree en grammes */
    const val QUANTITE_MAX_GRAMMES = 300.0

    /** Quantite min suggeree en grammes */
    const val QUANTITE_MIN_GRAMMES = 10.0

    /**
     * Nutriments critiques par regime alimentaire.
     * Un deficit sur un nutriment critique a un poids plus eleve dans le scoring.
     */
    fun getNutrimentsCritiques(regime: RegimeAlimentaire): Set<NutrimentType> {
        return when (regime) {
            RegimeAlimentaire.VEGAN -> setOf(
                NutrimentType.VITAMINE_B12,
                NutrimentType.FER,
                NutrimentType.ZINC,
                NutrimentType.OMEGA_3,
                NutrimentType.CALCIUM,
                NutrimentType.PROTEINES,
            )
            RegimeAlimentaire.VEGETARIEN -> setOf(
                NutrimentType.VITAMINE_B12,
                NutrimentType.FER,
                NutrimentType.ZINC,
                NutrimentType.OMEGA_3,
            )
            RegimeAlimentaire.FLEXITARIEN,
            RegimeAlimentaire.OMNIVORE -> emptySet()
        }
    }

    /**
     * Patterns de mots-cles associes a chaque allergie.
     * Utilises pour filtrer les aliments par categorie.
     */
    val ALLERGEN_PATTERNS: Map<String, List<String>> = mapOf(
        "gluten" to listOf("ble", "seigle", "orge", "avoine"),
        "soja" to listOf("soja"),
        "arachides" to listOf("arachide", "cacahuete"),
        "fruits_a_coque" to listOf("noix", "amande", "noisette", "cajou", "pistache", "pecan", "macadamia"),
        "lait" to listOf("lait", "fromage", "yaourt", "beurre", "creme"),
        "oeufs" to listOf("oeuf", "egg"),
    )

    /**
     * Arrondi a la dizaine la plus proche, avec un minimum.
     */
    fun arrondirQuantite(value: Double): Double {
        val arrondi = ((value / 10.0).toInt() * 10).toDouble()
        return if (arrondi < QUANTITE_MIN_GRAMMES) QUANTITE_MIN_GRAMMES else arrondi
    }
}
