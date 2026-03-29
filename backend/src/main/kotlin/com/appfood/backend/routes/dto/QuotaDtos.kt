package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class UpdateQuotaRequest(
    val valeurCible: Double,
)

// --- Response DTOs ---

@Serializable
data class QuotaResponse(
    val nutriment: String,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
    val updatedAt: String,
)

@Serializable
data class QuotaListResponse(
    val data: List<QuotaResponse>,
    val total: Int,
)

@Serializable
data class QuotaStatusResponse(
    val nutriment: String,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,
    val unite: String,
)

@Serializable
data class QuotaStatusListResponse(
    val date: String,
    val data: List<QuotaStatusResponse>,
)
