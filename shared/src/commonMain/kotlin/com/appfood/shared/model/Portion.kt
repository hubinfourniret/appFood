package com.appfood.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class PortionStandard(
    val id: String,
    val alimentId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val estGenerique: Boolean,
    val estPersonnalise: Boolean,
    val userId: String?,
)
