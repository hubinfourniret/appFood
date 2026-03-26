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
    val portionsStandard: List<PortionStandard>,
)

@Serializable
data class NutrimentValues(
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val fibres: Double,
    val sel: Double,
    val sucres: Double,
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double,
)
