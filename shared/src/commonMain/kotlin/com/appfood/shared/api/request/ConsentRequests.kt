package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConsentRequest(
    val accepte: Boolean,
    val versionPolitique: String,
)

@Serializable
data class InitialConsentRequest(
    val analytics: Boolean,
    val publicite: Boolean,
    val ameliorationService: Boolean,
    val versionPolitique: String,
)
