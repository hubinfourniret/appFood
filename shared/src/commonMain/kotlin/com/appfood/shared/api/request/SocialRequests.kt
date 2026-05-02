package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

/**
 * TACHE-600 : requetes profil social.
 */
@Serializable
data class UpdateSocialProfileRequest(
    val handle: String,
    val bio: String? = null,
    val dateNaissance: String? = null, // ISO YYYY-MM-DD ; ignore si deja set cote backend
    val socialVisibility: String, // PRIVATE | FRIENDS | PUBLIC
)
