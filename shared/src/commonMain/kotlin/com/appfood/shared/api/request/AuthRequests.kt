package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val firebaseToken: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
)

@Serializable
data class LoginRequest(
    val firebaseToken: String,
)
