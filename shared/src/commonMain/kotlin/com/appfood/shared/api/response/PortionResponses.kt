package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PortionResponse(
    val id: String,
    val alimentId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val estGenerique: Boolean,
    val estPersonnalise: Boolean,
)

@Serializable
data class PortionListResponse(
    val data: List<PortionResponse>,
    val total: Int,
)
