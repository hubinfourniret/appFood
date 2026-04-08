package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class QuotaJournalier(
    val userId: String,
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
    @Contextual
    val updatedAt: Instant,
)

// Used for dashboard display — combines quota target with consumed value
@Serializable
data class QuotaStatus(
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,
    val unite: String,
)
