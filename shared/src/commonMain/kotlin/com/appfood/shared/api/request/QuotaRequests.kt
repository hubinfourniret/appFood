package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateQuotaRequest(
    val valeurCible: Double,
)
