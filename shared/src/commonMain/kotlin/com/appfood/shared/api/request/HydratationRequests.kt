package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class AddHydratationRequest(
    val id: String? = null,
    val date: String,
    val quantiteMl: Int,
)

@Serializable
data class UpdateHydratationObjectifRequest(
    val objectifMl: Int,
)
