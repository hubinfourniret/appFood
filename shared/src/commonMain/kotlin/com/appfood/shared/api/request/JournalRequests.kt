package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

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

// V1.1 — JOURNAL-05
@Serializable
data class CopyJournalDayRequest(
    val sourceDate: String,
    val targetDate: String,
    val mealType: String? = null,
)
