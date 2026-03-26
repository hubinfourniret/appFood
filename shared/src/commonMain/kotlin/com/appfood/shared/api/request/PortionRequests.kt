package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

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
