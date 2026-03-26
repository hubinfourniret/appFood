package com.appfood.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val titre: String,
    val contenu: String,
    val dateEnvoi: Instant,
    val lue: Boolean,
)
