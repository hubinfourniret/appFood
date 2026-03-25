package com.appfood.shared.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object Login : Screen()
    @Serializable data object Register : Screen()
    @Serializable data object ForgotPassword : Screen()
    @Serializable data object Onboarding : Screen()
    @Serializable data object Dashboard : Screen()
    @Serializable data object Journal : Screen()
    @Serializable data object Recommandations : Screen()
    @Serializable data object Recettes : Screen()
    @Serializable data object Profil : Screen()
    @Serializable data object EditProfil : Screen()
    @Serializable data object PreferencesAlimentaires : Screen()
    @Serializable data object Settings : Screen()

    // Keep Auth as alias for Login (backward compatibility)
    @Serializable data object Auth : Screen()
}
