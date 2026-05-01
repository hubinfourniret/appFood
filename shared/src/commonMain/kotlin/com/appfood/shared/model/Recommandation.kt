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

/**
 * Resultat des recommandations recettes : permet de distinguer le cas
 * "pas de deficit" (manquesIdentifies vide) du cas "aucune recette ne correspond
 * aux deficits identifies" (manquesIdentifies non vide mais recettes vide). TACHE-512.
 */
@Serializable
data class RecommandationRecetteList(
    val manquesIdentifies: List<NutrimentType>,
    val recettes: List<RecommandationRecette>,
)
