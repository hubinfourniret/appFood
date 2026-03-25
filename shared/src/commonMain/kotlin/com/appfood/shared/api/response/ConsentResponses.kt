package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ConsentResponse(
    val type: String,
    val accepte: Boolean,
    val dateConsentement: String,
    val versionPolitique: String,
)
