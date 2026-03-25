package com.appfood.backend.routes

import kotlinx.serialization.Serializable

/**
 * Wrapper generique pour les reponses API.
 * Encapsule les donnees dans un champ `data`.
 */
@Serializable
data class ApiResponse<T>(
    val data: T,
)
