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
    @Serializable data object SearchRecette : Screen()
    @Serializable data object PortionSelector : Screen()

    // Quota management (QUOTAS-02)
    @Serializable data object QuotaManagement : Screen()

    // Poids (POIDS-01)
    @Serializable data object Poids : Screen()

    // Hydratation (HYDRA-01)
    @Serializable data object Hydratation : Screen()

    // Weekly dashboard (DASHBOARD-02)
    @Serializable data object WeeklyDashboard : Screen()

    // Recette detail (RECETTES-02). prefilledMealType permet de skip le dialog
    // de selection repas quand on vient de AddEntry → SearchRecette (TACHE-510 v3).
    // editJournalEntryId : si fourni, le bouton "Ajouter au journal" devient
    // "Enregistrer" et appelle updateEntry au lieu de create (TACHE-518).
    @Serializable data class RecetteDetail(
        val recetteId: String,
        val prefilledMealType: String? = null,
        val editJournalEntryId: String? = null,
        val prefilledPortions: Int? = null,
        /** JSON encode du Map<ingredientId, grammes> pour restaurer les ajustements en mode edit. */
        val prefilledOverridesJson: String? = null,
    ) : Screen()

    // Create recette — admin only (RECETTES-03)
    @Serializable data object CreateRecette : Screen()

    // About (SUPPORT-01)
    @Serializable data object About : Screen()

    // FAQ (SUPPORT-02)
    @Serializable data object Faq : Screen()

    // Legal disclaimer (UX-05)
    @Serializable data object Disclaimer : Screen()

    // Consent management (LEGAL-03)
    @Serializable data object Consent : Screen()
    @Serializable data object ConsentSettings : Screen()

    // Legal screens (LEGAL-01 / LEGAL-02)
    @Serializable data object PrivacyPolicy : Screen()
    @Serializable data object TermsOfService : Screen()

    // Keep Auth as alias for Login (backward compatibility)
    @Serializable data object Auth : Screen()
}
