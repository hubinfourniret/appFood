package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

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
