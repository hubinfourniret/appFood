package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val onboardingComplete: Boolean,
    val createdAt: String,
    // TACHE-600 : profil social
    val handle: String? = null,
    val bio: String? = null,
    val dateNaissance: String? = null,
    val socialVisibility: String = "PRIVATE",
    val socialEnabled: Boolean = false,
    val socialOnboardingComplete: Boolean = false,
)
