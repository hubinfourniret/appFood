package com.appfood.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class RecommandationAliment(
    val aliment: Aliment,
    val nutrimentsCibles: List<NutrimentType>,
    val quantiteSuggereGrammes: Double,
    val pourcentageCouverture: Map<NutrimentType, Double>,
)

@Serializable
data class RecommandationRecette(
    val recette: Recette,
    val nutrimentsCibles: List<NutrimentType>,
    val pourcentageCouvertureGlobal: Double,
    val pourcentageCouverture: Map<NutrimentType, Double>,
)
