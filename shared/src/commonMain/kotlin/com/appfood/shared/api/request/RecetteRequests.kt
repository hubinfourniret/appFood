package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRecetteRequest(
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: List<String>,
    val typeRepas: List<String>,
    val ingredients: List<IngredientRequest>,
    val etapes: List<String>,
    val imageUrl: String? = null,
    val publie: Boolean,
)

@Serializable
data class UpdateRecetteRequest(
    val nom: String? = null,
    val description: String? = null,
    val tempsPreparationMin: Int? = null,
    val tempsCuissonMin: Int? = null,
    val nbPortions: Int? = null,
    val regimesCompatibles: List<String>? = null,
    val typeRepas: List<String>? = null,
    val ingredients: List<IngredientRequest>? = null,
    val etapes: List<String>? = null,
    val imageUrl: String? = null,
    val publie: Boolean? = null,
)

@Serializable
data class IngredientRequest(
    val alimentId: String,
    val quantiteGrammes: Double,
)
