package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

/**
 * TACHE-600 : reponses profil social.
 */
@Serializable
data class HandleAvailabilityResponse(
    val handle: String,
    val available: Boolean,
)
