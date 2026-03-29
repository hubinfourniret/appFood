package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Response DTOs ---

@Serializable
data class AlimentSummaryResponse(
    val id: String,
    val nom: String,
    val categorie: String,
    val caloriesPour100g: Double,
    val regimesCompatibles: List<String>,
)

@Serializable
data class RecommandationAlimentResponse(
    val aliment: AlimentSummaryResponse,
    val nutrimentsCibles: List<String>,
    val quantiteSuggereGrammes: Double,
    val pourcentageCouverture: Map<String, Double>,
)

@Serializable
data class RecommandationAlimentListResponse(
    val date: String,
    val manquesIdentifies: List<String>,
    val data: List<RecommandationAlimentResponse>,
)

@Serializable
data class RecommandationRecetteResponse(
    val recetteId: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val nutrimentsCibles: List<String>,
    val pourcentageCouvertureGlobal: Double,
    val pourcentageCouverture: Map<String, Double>,
)

@Serializable
data class RecommandationRecetteListResponse(
    val date: String,
    val manquesIdentifies: List<String>,
    val data: List<RecommandationRecetteResponse>,
)
