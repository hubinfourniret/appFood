package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchAlimentResponse(
    val data: List<AlimentResponse>,
    val total: Int,
    val query: String,
)

@Serializable
data class AlimentResponse(
    val id: String,
    val nom: String,
    val marque: String?,
    val source: String,
    val sourceId: String?,
    val codeBarres: String?,
    val categorie: String,
    val regimesCompatibles: List<String>,
    val nutrimentsPour100g: NutrimentValuesResponse,
    val portionsStandard: List<PortionResponse>,
)

// NutrimentValuesResponse is defined in JournalResponses.kt
