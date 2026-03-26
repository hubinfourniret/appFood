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
)
