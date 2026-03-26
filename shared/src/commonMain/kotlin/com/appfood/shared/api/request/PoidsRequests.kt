package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class AddPoidsRequest(
    val id: String? = null,
    val date: String,
    val poidsKg: Double,
)
