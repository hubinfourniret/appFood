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

    // Journal flow
    @Serializable data object AddEntry : Screen()
    @Serializable data object SearchAliment : Screen()
    @Serializable data object PortionSelector : Screen()

    // Quota management (QUOTAS-02)
    @Serializable data object QuotaManagement : Screen()

    // Poids (POIDS-01)
    @Serializable data object Poids : Screen()

    // Hydratation (HYDRA-01)
    @Serializable data object Hydratation : Screen()

    // Weekly dashboard (DASHBOARD-02)
    @Serializable data object WeeklyDashboard : Screen()

    // Recette detail (RECETTES-02)
    @Serializable data class RecetteDetail(val recetteId: String) : Screen()

    // Create recette — admin only (RECETTES-03)
    @Serializable data object CreateRecette : Screen()

    // Keep Auth as alias for Login (backward compatibility)
    @Serializable data object Auth : Screen()
}
