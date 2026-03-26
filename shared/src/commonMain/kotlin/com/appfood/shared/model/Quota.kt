package com.appfood.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class QuotaJournalier(
    val userId: String,
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
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
