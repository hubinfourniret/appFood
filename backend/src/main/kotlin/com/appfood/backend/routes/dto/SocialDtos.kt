package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

/**
 * TACHE-600 : DTOs profil social.
 */

@Serializable
data class HandleAvailabilityResponse(
    val handle: String,
    val available: Boolean,
)

@Serializable
data class UpdateSocialProfileRequest(
    val handle: String,
    val bio: String? = null,
    val dateNaissance: String? = null, // ISO YYYY-MM-DD ; ignore si deja set
    val socialVisibility: String, // PRIVATE | FRIENDS | PUBLIC
)
