package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class RegisterFcmTokenRequest(
    val token: String,
    val platform: String,
)

// --- Response DTOs ---

@Serializable
data class NotificationListResponse(
    val data: List<NotificationResponse>,
    val total: Int,
    val nonLues: Int,
)

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,
    val titre: String,
    val contenu: String,
    val dateEnvoi: String,
    val lue: Boolean,
)

@Serializable
data class ReadAllResponse(
    val marqueesCommeLues: Int,
)

@Serializable
data class RegisterTokenResponse(
    val registered: Boolean,
)
