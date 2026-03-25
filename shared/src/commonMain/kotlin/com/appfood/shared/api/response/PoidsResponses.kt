package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PoidsResponse(
    val id: String,
    val date: String,
    val poidsKg: Double,
    val estReference: Boolean,
    val createdAt: String,
)
