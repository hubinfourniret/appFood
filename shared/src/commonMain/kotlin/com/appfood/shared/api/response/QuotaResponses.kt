package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class QuotaListResponse(
    val data: List<QuotaResponse>,
)

@Serializable
data class QuotaResponse(
    val nutriment: String,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
)

@Serializable
data class QuotaStatusListResponse(
    val date: String,
    val data: List<QuotaStatusResponse>,
)

@Serializable
data class QuotaStatusResponse(
    val nutriment: String,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,
    val unite: String,
)
