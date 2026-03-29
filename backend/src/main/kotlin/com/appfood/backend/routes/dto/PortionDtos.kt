package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class CreatePortionRequest(
    val alimentId: String? = null,
    val nom: String,
    val quantiteGrammes: Double,
)

@Serializable
data class UpdatePortionRequest(
    val nom: String? = null,
    val quantiteGrammes: Double? = null,
)

// --- Response DTOs ---

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
