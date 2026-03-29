package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class AddJournalEntryRequest(
    val id: String? = null,
    val date: String,
    val mealType: String,
    val alimentId: String? = null,
    val recetteId: String? = null,
    val quantiteGrammes: Double? = null,
    val nbPortions: Double? = null,
)

@Serializable
data class UpdateJournalEntryRequest(
    val quantiteGrammes: Double? = null,
    val nbPortions: Double? = null,
    val mealType: String? = null,
)

// --- Response DTOs ---

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

@Serializable
data class RecentAlimentResponse(
    val id: String,
    val nom: String,
    val categorie: String,
)
