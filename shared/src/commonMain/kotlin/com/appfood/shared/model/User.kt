package com.appfood.shared.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val role: Role = Role.USER,
    @Contextual
    val createdAt: Instant,
    @Contextual
    val updatedAt: Instant,
)

@Serializable
data class UserProfile(
    val userId: String,
    val sexe: Sexe,
    val age: Int,
    val poidsKg: Double,
    val tailleCm: Int,
    val regimeAlimentaire: RegimeAlimentaire,
    val niveauActivite: NiveauActivite,
    val onboardingComplete: Boolean,
    val objectifPoids: ObjectifPoids?,
    @Contextual
    val updatedAt: Instant,
)

@Serializable
data class UserPreferences(
    val userId: String,
    val alimentsExclus: List<String>,
    val allergies: List<String>,
    val alimentsFavoris: List<String>,
    @Contextual
    val updatedAt: Instant,
)
