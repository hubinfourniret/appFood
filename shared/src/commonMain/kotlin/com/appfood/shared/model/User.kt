package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val role: Role = Role.USER,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    // TACHE-600 : profil social (nullable tant que l'utilisateur n'a pas complete l'onboarding social)
    val handle: String? = null,
    val bio: String? = null,
    val dateNaissance: String? = null, // ISO YYYY-MM-DD
    val socialVisibility: SocialVisibility = SocialVisibility.PRIVATE,
    val socialEnabled: Boolean = false,
    val socialOnboardingComplete: Boolean = false,
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
    @Contextual val updatedAt: Instant,
)

@Serializable
data class UserPreferences(
    val userId: String,
    val alimentsExclus: List<String>,
    val allergies: List<String>,
    val alimentsFavoris: List<String>,
    @Contextual val updatedAt: Instant,
)
