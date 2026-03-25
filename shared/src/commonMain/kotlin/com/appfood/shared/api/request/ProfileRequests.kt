package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateProfileRequest(
    val sexe: String,
    val age: Int,
    val poidsKg: Double,
    val tailleCm: Int,
    val regimeAlimentaire: String,
    val niveauActivite: String,
)

@Serializable
data class UpdateProfileRequest(
    val sexe: String? = null,
    val age: Int? = null,
    val poidsKg: Double? = null,
    val tailleCm: Int? = null,
    val regimeAlimentaire: String? = null,
    val niveauActivite: String? = null,
    val objectifPoids: String? = null,
)

@Serializable
data class UpdatePreferencesRequest(
    val alimentsExclus: List<String>? = null,
    val allergies: List<String>? = null,
    val alimentsFavoris: List<String>? = null,
)
