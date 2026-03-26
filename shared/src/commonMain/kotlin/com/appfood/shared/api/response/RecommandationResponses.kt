package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class RecommandationAlimentListResponse(
    val date: String,
    val manquesIdentifies: List<String>,
    val data: List<RecommandationAlimentResponse>,
)

@Serializable
data class RecommandationAlimentResponse(
    val aliment: AlimentResponse,
    val nutrimentsCibles: List<String>,
    val quantiteSuggereGrammes: Double,
    val pourcentageCouverture: Map<String, Double>,
)

@Serializable
data class RecommandationRecetteListResponse(
    val date: String,
    val manquesIdentifies: List<String>,
    val data: List<RecommandationRecetteResponse>,
)

@Serializable
data class RecommandationRecetteResponse(
    val recette: RecetteSummaryResponse,
    val nutrimentsCibles: List<String>,
    val pourcentageCouvertureGlobal: Double,
    val pourcentageCouverture: Map<String, Double>,
)
