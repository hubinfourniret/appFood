package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterFcmTokenRequest(
    val token: String,
    val platform: String,
)
