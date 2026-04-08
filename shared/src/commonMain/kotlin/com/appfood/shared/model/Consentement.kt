package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Consentement(
    val id: String,
    val userId: String,
    val type: ConsentType,
    val accepte: Boolean,
    @Contextual
    val dateConsentement: Instant,
    val versionPolitique: String,
)
