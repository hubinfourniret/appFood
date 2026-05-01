package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class RecetteListResponse(
    val data: List<RecetteSummaryResponse>,
    val total: Int,
)

@Serializable
data class RecetteSummaryResponse(
    val id: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: List<String>,
    val source: String,
    val typeRepas: List<String>,
    val imageUrl: String?,
    val nutrimentsParPortion: NutrimentValuesResponse,
)

@Serializable
data class RecetteDetailResponse(
    val id: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: List<String>,
    val typeRepas: List<String>,
    val ingredients: List<IngredientResponse>,
    val etapes: List<String>,
    val nutrimentsTotaux: NutrimentValuesResponse,
    val nutrimentsParPortion: NutrimentValuesResponse,
    val source: String,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class IngredientResponse(
    val id: String = "",
    val alimentId: String,
    val alimentNom: String,
    val quantiteGrammes: Double,
)
