package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val firebaseToken: String,
    val email: String,
    val nom: String? = null,
    val prenom: String? = null,
)

@Serializable
data class LoginRequest(
    val firebaseToken: String,
)

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
    val dateNaissance: String? = null, // ISO YYYY-MM-DD
    val socialVisibility: String = "PRIVATE",
    val socialEnabled: Boolean = false, // age >= 16 ET handle non null
    val socialOnboardingComplete: Boolean = false, // handle ET dateNaissance presents
)
