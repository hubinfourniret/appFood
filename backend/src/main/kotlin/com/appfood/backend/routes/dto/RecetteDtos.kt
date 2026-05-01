package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

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
data class IngredientRequest(
    val alimentId: String,
    val quantiteGrammes: Double,
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

// --- Response DTOs ---

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
    /** TACHE-516 : true si la recette appartient a l'utilisateur courant. */
    val estPersonnelle: Boolean = false,
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
    val estPersonnelle: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class IngredientResponse(
    val id: String,
    val alimentId: String,
    val alimentNom: String,
    val quantiteGrammes: Double,
    /** Valeurs nutritionnelles pour 100g de cet ingredient (utile pour preview live cote client). */
    val nutrimentsPour100g: NutrimentValuesResponse? = null,
)
