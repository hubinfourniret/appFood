package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

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

@Serializable
data class SearchAlimentResponse(
    val data: List<AlimentResponse>,
    val total: Int,
    val query: String,
)

@Serializable
data class PortionResponse(
    val id: String,
    val alimentId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val estGenerique: Boolean,
    val estPersonnalise: Boolean,
)

@Serializable
data class PortionListResponse(
    val data: List<PortionResponse>,
    val total: Int,
)

@Serializable
data class CreatePortionRequest(
    val alimentId: String? = null,
    val nom: String,
    val quantiteGrammes: Double,
)

@Serializable
data class UpdatePortionRequest(
    val nom: String? = null,
    val quantiteGrammes: Double? = null,
)
