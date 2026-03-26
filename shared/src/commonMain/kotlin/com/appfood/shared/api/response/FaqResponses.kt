package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

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
