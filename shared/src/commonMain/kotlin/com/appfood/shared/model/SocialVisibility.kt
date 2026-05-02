package com.appfood.shared.model

import kotlinx.serialization.Serializable

/**
 * TACHE-600 : visibilite du profil social.
 * - PRIVATE : seul l'utilisateur voit son contenu
 * - FRIENDS : visible par les utilisateurs avec un follow mutuel
 * - PUBLIC : visible par tout utilisateur 16+
 */
@Serializable
enum class SocialVisibility {
    PRIVATE,
    FRIENDS,
    PUBLIC,
}
