package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val titre: String,
    val contenu: String,
    @Contextual val dateEnvoi: Instant,
    val lue: Boolean,
)
