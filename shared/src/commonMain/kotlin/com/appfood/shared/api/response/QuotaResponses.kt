package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class QuotaResponse(
    val nutriment: String,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
)
