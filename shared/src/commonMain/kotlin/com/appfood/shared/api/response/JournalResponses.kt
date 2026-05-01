package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class NutrimentValuesResponse(
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val fibres: Double,
    val sel: Double,
    val sucres: Double,
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double,
)

@Serializable
data class JournalEntryResponse(
    val id: String,
    val date: String,
    val mealType: String,
    val alimentId: String?,
    val recetteId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val nbPortions: Double?,
    val nutrimentsCalcules: NutrimentValuesResponse,
    val ingredientOverrides: Map<String, Double>? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class JournalListResponse(
    val data: List<JournalEntryResponse>,
    val total: Int,
)

@Serializable
data class DailySummaryResponse(
    val date: String,
    val totalNutriments: NutrimentValuesResponse,
    val parRepas: Map<String, NutrimentValuesResponse>,
    val nbEntrees: Int,
)

@Serializable
data class WeeklySummaryResponse(
    val dateFrom: String,
    val dateTo: String,
    val moyenneJournaliere: NutrimentValuesResponse,
    val parJour: Map<String, NutrimentValuesResponse>,
    val joursAvecSaisie: Int,
)
