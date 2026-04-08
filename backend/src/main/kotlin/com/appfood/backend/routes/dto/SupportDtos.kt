package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Response DTOs ---

@Serializable
data class FaqListResponse(
    val data: List<FaqResponse>,
)

@Serializable
data class FaqResponse(
    val id: String,
    val theme: String,
    val question: String,
    val reponse: String,
    val ordre: Int,
)
