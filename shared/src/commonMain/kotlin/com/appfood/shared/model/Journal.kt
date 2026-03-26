package com.appfood.shared.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class JournalEntry(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val mealType: MealType,
    val alimentId: String?,
    val recetteId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val nbPortions: Double?,
    val nutrimentsCalcules: NutrimentValues,
    @Transient
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    @Contextual
    val createdAt: Instant,
    @Contextual
    val updatedAt: Instant,
)
