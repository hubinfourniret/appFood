package com.appfood.backend.database.dao

import com.appfood.backend.database.tables.NutrimentType

/**
 * Extensions pour acceder aux valeurs nutritionnelles par NutrimentType.
 * Elimine la duplication des when-statements a 16 cas dans les services.
 */

fun AlimentRow.getNutrientValue(type: NutrimentType): Double = when (type) {
    NutrimentType.CALORIES -> calories
    NutrimentType.PROTEINES -> proteines
    NutrimentType.GLUCIDES -> glucides
    NutrimentType.LIPIDES -> lipides
    NutrimentType.FIBRES -> fibres
    NutrimentType.SEL -> sel
    NutrimentType.SUCRES -> sucres
    NutrimentType.FER -> fer
    NutrimentType.CALCIUM -> calcium
    NutrimentType.ZINC -> zinc
    NutrimentType.MAGNESIUM -> magnesium
    NutrimentType.VITAMINE_B12 -> vitamineB12
    NutrimentType.VITAMINE_D -> vitamineD
    NutrimentType.VITAMINE_C -> vitamineC
    NutrimentType.OMEGA_3 -> omega3
    NutrimentType.OMEGA_6 -> omega6
}

fun com.appfood.backend.service.NutrientSums.getByType(type: NutrimentType): Double = when (type) {
    NutrimentType.CALORIES -> calories
    NutrimentType.PROTEINES -> proteines
    NutrimentType.GLUCIDES -> glucides
    NutrimentType.LIPIDES -> lipides
    NutrimentType.FIBRES -> fibres
    NutrimentType.SEL -> sel
    NutrimentType.SUCRES -> sucres
    NutrimentType.FER -> fer
    NutrimentType.CALCIUM -> calcium
    NutrimentType.ZINC -> zinc
    NutrimentType.MAGNESIUM -> magnesium
    NutrimentType.VITAMINE_B12 -> vitamineB12
    NutrimentType.VITAMINE_D -> vitamineD
    NutrimentType.VITAMINE_C -> vitamineC
    NutrimentType.OMEGA_3 -> omega3
    NutrimentType.OMEGA_6 -> omega6
}

fun RecetteRow.getNutrientValue(type: NutrimentType): Double = when (type) {
    NutrimentType.CALORIES -> calories
    NutrimentType.PROTEINES -> proteines
    NutrimentType.GLUCIDES -> glucides
    NutrimentType.LIPIDES -> lipides
    NutrimentType.FIBRES -> fibres
    NutrimentType.SEL -> sel
    NutrimentType.SUCRES -> sucres
    NutrimentType.FER -> fer
    NutrimentType.CALCIUM -> calcium
    NutrimentType.ZINC -> zinc
    NutrimentType.MAGNESIUM -> magnesium
    NutrimentType.VITAMINE_B12 -> vitamineB12
    NutrimentType.VITAMINE_D -> vitamineD
    NutrimentType.VITAMINE_C -> vitamineC
    NutrimentType.OMEGA_3 -> omega3
    NutrimentType.OMEGA_6 -> omega6
}
