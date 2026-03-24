package com.appfood.shared.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object Auth : Screen()
    @Serializable data object Register : Screen()
    @Serializable data object Onboarding : Screen()
    @Serializable data object Dashboard : Screen()
    @Serializable data object Journal : Screen()
    @Serializable data object Recommandations : Screen()
    @Serializable data object Recettes : Screen()
    @Serializable data object Profil : Screen()
    @Serializable data object Settings : Screen()
}
