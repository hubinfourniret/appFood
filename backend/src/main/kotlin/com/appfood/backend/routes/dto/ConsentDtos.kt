package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class UpdateConsentRequest(
    val accepte: Boolean,
    val versionPolitique: String,
)

@Serializable
data class InitialConsentRequest(
    val analytics: Boolean,
    val publicite: Boolean,
    val ameliorationService: Boolean,
    val versionPolitique: String,
)

// --- Response DTOs ---

@Serializable
data class ConsentListResponse(
    val data: List<ConsentResponse>,
)

@Serializable
data class ConsentResponse(
    val type: String,
    val accepte: Boolean,
    val dateConsentement: String,
    val versionPolitique: String,
)
