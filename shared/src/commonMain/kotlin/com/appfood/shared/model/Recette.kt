package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Recette(
    val id: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: List<RegimeAlimentaire>,
    val source: SourceRecette,
    val typeRepas: List<MealType>,
    val ingredients: List<IngredientRecette>,
    val etapes: List<String>,
    val nutrimentsTotaux: NutrimentValues,
    val imageUrl: String?,
    val publie: Boolean,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
)

@Serializable
data class IngredientRecette(
    val alimentId: String,
    val alimentNom: String,
    val quantiteGrammes: Double,
)
