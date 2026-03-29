package com.appfood.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Aliment(
    val id: String,
    val nom: String,
    val marque: String?,
    val source: SourceAliment,
    val sourceId: String?,
    val codeBarres: String?,
    val categorie: String,
    val regimesCompatibles: List<RegimeAlimentaire>,
    val nutrimentsPour100g: NutrimentValues,
    val portionsStandard: List<PortionStandard> = emptyList(),
)

@Serializable
data class NutrimentValues(
    val calories: Double = 0.0,
    val proteines: Double = 0.0,
    val glucides: Double = 0.0,
    val lipides: Double = 0.0,
    val fibres: Double = 0.0,
    val sel: Double = 0.0,
    val sucres: Double = 0.0,
    val fer: Double = 0.0,
    val calcium: Double = 0.0,
    val zinc: Double = 0.0,
    val magnesium: Double = 0.0,
    val vitamineB12: Double = 0.0,
    val vitamineD: Double = 0.0,
    val vitamineC: Double = 0.0,
    val omega3: Double = 0.0,
    val omega6: Double = 0.0,
) {
    /**
     * Returns the value of the given nutriment type.
     */
    fun getByType(type: NutrimentType): Double = when (type) {
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
}
