package com.appfood.shared.domain.recette

import com.appfood.shared.model.Aliment
import com.appfood.shared.model.IngredientRecette
import com.appfood.shared.model.NutrimentValues

/**
 * Calcule les nutriments totaux et par portion d'une recette
 * a partir de ses ingredients et de la table d'aliments.
 *
 * Formule : pour chaque ingredient, nutriments = nutrimentsPour100g * quantiteGrammes / 100
 * Total = somme de tous les ingredients
 * Par portion = total / nbPortions (nbPortions coerce a 1 minimum)
 */
object RecetteNutrimentsCalculator {

    /**
     * Calcule les nutriments totaux d'une recette.
     * @param ingredients liste des ingredients de la recette
     * @param alimentsById map alimentId -> Aliment pour lookup des nutriments
     * @return NutrimentValues totaux (somme des contributions de chaque ingredient)
     */
    fun calculerNutrimentsTotaux(
        ingredients: List<IngredientRecette>,
        alimentsById: Map<String, Aliment>,
    ): NutrimentValues {
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

        for (ingredient in ingredients) {
            val aliment = alimentsById[ingredient.alimentId] ?: continue
            val ratio = ingredient.quantiteGrammes / 100.0
            val n = aliment.nutrimentsPour100g

            calories += n.calories * ratio
            proteines += n.proteines * ratio
            glucides += n.glucides * ratio
            lipides += n.lipides * ratio
            fibres += n.fibres * ratio
            sel += n.sel * ratio
            sucres += n.sucres * ratio
            fer += n.fer * ratio
            calcium += n.calcium * ratio
            zinc += n.zinc * ratio
            magnesium += n.magnesium * ratio
            vitamineB12 += n.vitamineB12 * ratio
            vitamineD += n.vitamineD * ratio
            vitamineC += n.vitamineC * ratio
            omega3 += n.omega3 * ratio
            omega6 += n.omega6 * ratio
        }

        return NutrimentValues(
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
    }

    /**
     * Calcule les nutriments par portion.
     * @param nutrimentsTotaux nutriments totaux de la recette
     * @param nbPortions nombre de portions (coerce a 1 si <= 0)
     * @return NutrimentValues par portion
     */
    fun calculerNutrimentsParPortion(
        nutrimentsTotaux: NutrimentValues,
        nbPortions: Int,
    ): NutrimentValues {
        val divisor = nbPortions.coerceAtLeast(1).toDouble()
        return NutrimentValues(
            calories = nutrimentsTotaux.calories / divisor,
            proteines = nutrimentsTotaux.proteines / divisor,
            glucides = nutrimentsTotaux.glucides / divisor,
            lipides = nutrimentsTotaux.lipides / divisor,
            fibres = nutrimentsTotaux.fibres / divisor,
            sel = nutrimentsTotaux.sel / divisor,
            sucres = nutrimentsTotaux.sucres / divisor,
            fer = nutrimentsTotaux.fer / divisor,
            calcium = nutrimentsTotaux.calcium / divisor,
            zinc = nutrimentsTotaux.zinc / divisor,
            magnesium = nutrimentsTotaux.magnesium / divisor,
            vitamineB12 = nutrimentsTotaux.vitamineB12 / divisor,
            vitamineD = nutrimentsTotaux.vitamineD / divisor,
            vitamineC = nutrimentsTotaux.vitamineC / divisor,
            omega3 = nutrimentsTotaux.omega3 / divisor,
            omega6 = nutrimentsTotaux.omega6 / divisor,
        )
    }
}
