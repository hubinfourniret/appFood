package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class HydratationResponse(
    val id: String,
    val date: String,
    val quantiteMl: Int,
    val objectifMl: Int,
    val estObjectifPersonnalise: Boolean,
    val pourcentage: Double,
    val entrees: List<HydratationEntryResponse>,
)

@Serializable
data class HydratationEntryResponse(
    val id: String,
    val heure: String,
    val quantiteMl: Int,
)
