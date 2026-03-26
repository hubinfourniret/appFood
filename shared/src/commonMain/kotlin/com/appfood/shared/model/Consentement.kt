package com.appfood.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Consentement(
    val id: String,
    val userId: String,
    val type: ConsentType,
    val accepte: Boolean,
    val dateConsentement: Instant,
    val versionPolitique: String,
)
