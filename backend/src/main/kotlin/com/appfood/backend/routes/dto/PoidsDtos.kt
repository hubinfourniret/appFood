package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class AddPoidsRequest(
    val id: String? = null,
    val date: String,
    val poidsKg: Double,
)

// --- Response DTOs ---

@Serializable
data class PoidsListResponse(
    val data: List<PoidsResponse>,
    val total: Int,
    val poidsCourant: Double?,
    val poidsMin: Double?,
    val poidsMax: Double?,
)

@Serializable
data class PoidsResponse(
    val id: String,
    val date: String,
    val poidsKg: Double,
    val estReference: Boolean,
    val createdAt: String,
)

@Serializable
data class AddPoidsResponse(
    val poids: PoidsResponse,
    val changementSignificatif: Boolean,
    val messageRecalcul: String?,
)
