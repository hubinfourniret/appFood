package com.appfood.shared.ui

object Strings {
    const val APP_NAME = "appFood"

    // Navigation — barre inferieure
    const val NAV_DASHBOARD = "Dashboard"
    const val NAV_ADD = "Ajouter"
    const val NAV_RECIPES = "Recettes"
    const val NAV_PROFILE = "Profil"

    // Aliases (backward compatibility)
    const val TAB_DASHBOARD = "Dashboard"
    const val TAB_ADD = "Ajouter"
    const val TAB_RECETTES = "Recettes"
    const val TAB_PROFIL = "Profil"

    // Ecrans
    const val SCREEN_AUTH = "Connexion"
    const val SCREEN_REGISTER = "Inscription"
    const val SCREEN_ONBOARDING = "Bienvenue"
    const val SCREEN_DASHBOARD = "Tableau de bord"
    const val SCREEN_JOURNAL = "Journal alimentaire"
    const val SCREEN_RECOMMANDATIONS = "Recommandations"
    const val SCREEN_RECETTES = "Recettes"
    const val SCREEN_PROFIL = "Mon profil"
    const val SCREEN_SETTINGS = "Parametres"
    const val SCREEN_FORGOT_PASSWORD = "Mot de passe oublie"
    const val SCREEN_EDIT_PROFIL = "Modifier le profil"
    const val SCREEN_PREFERENCES_ALIMENTAIRES = "Preferences alimentaires"

    // Etats
    const val EMPTY_STATE_DEFAULT = "Aucun element a afficher"
    const val LOADING = "Chargement..."
    const val ERROR_DEFAULT = "Une erreur est survenue"
    const val RETRY = "Reessayer"

    // Auth — Login
    const val LOGIN_TITLE = "Se connecter"
    const val LOGIN_EMAIL_LABEL = "Adresse e-mail"
    const val LOGIN_PASSWORD_LABEL = "Mot de passe"
    const val LOGIN_BUTTON = "Se connecter"
    const val LOGIN_FORGOT_PASSWORD = "Mot de passe oublie ?"
    const val LOGIN_NO_ACCOUNT = "Pas de compte ?"
    const val LOGIN_SIGN_UP = "S'inscrire"
    const val LOGIN_CONTINUE_WITH_GOOGLE = "Continuer avec Google"
    const val LOGIN_CONTINUE_WITH_APPLE = "Continuer avec Apple"
    const val LOGIN_ERROR_INVALID_CREDENTIALS = "Identifiants invalides"
    const val LOGIN_OR_SEPARATOR = "ou"

    // Auth — Register
    const val REGISTER_TITLE = "Creer un compte"
    const val REGISTER_EMAIL_LABEL = "Adresse e-mail"
    const val REGISTER_PASSWORD_LABEL = "Mot de passe"
    const val REGISTER_CONFIRM_PASSWORD_LABEL = "Confirmer le mot de passe"
    const val REGISTER_BUTTON = "S'inscrire"
    const val REGISTER_ALREADY_ACCOUNT = "Deja un compte ?"
    const val REGISTER_SIGN_IN = "Se connecter"
    const val REGISTER_ERROR_EMAIL_TAKEN = "Cette adresse e-mail est deja utilisee"
    const val REGISTER_ERROR_PASSWORDS_MISMATCH = "Les mots de passe ne correspondent pas"

    // Auth — Validation
    const val VALIDATION_EMAIL_INVALID = "Adresse e-mail invalide"
    const val VALIDATION_PASSWORD_TOO_SHORT = "Le mot de passe doit contenir au moins 8 caracteres"
    const val VALIDATION_FIELD_REQUIRED = "Ce champ est requis"

    // Auth — Forgot password
    const val FORGOT_PASSWORD_TITLE = "Reinitialiser le mot de passe"
    const val FORGOT_PASSWORD_DESCRIPTION = "Entrez votre adresse e-mail pour recevoir un lien de reinitialisation."
    const val FORGOT_PASSWORD_EMAIL_LABEL = "Adresse e-mail"
    const val FORGOT_PASSWORD_SEND_BUTTON = "Envoyer le lien"
    const val FORGOT_PASSWORD_SUCCESS = "Un e-mail de reinitialisation a ete envoye a votre adresse."
    const val FORGOT_PASSWORD_BACK_TO_LOGIN = "Retour a la connexion"

    // Onboarding
    const val ONBOARDING_WELCOME = "Bienvenue sur appFood !"
    const val ONBOARDING_SUBTITLE = "Configurons votre profil nutritionnel"
    const val ONBOARDING_CONTINUE = "Continuer"
    const val ONBOARDING_SKIP = "Passer"
    const val ONBOARDING_FINISH = "Terminer"
    // ONBOARDING_STEP_INDICATOR removed — use onboardingStepIndicator() instead
    fun onboardingStepIndicator(current: Int, total: Int) = "Étape $current sur $total"

    // Onboarding — Step 1 : Body metrics
    const val ONBOARDING_STEP1_TITLE = "Vos informations"
    const val ONBOARDING_SEXE_LABEL = "Sexe"
    const val ONBOARDING_SEXE_HOMME = "Homme"
    const val ONBOARDING_SEXE_FEMME = "Femme"
    const val ONBOARDING_AGE_LABEL = "Age"
    const val ONBOARDING_AGE_UNIT = "ans"
    const val ONBOARDING_POIDS_LABEL = "Poids"
    const val ONBOARDING_POIDS_UNIT = "kg"
    const val ONBOARDING_TAILLE_LABEL = "Taille"
    const val ONBOARDING_TAILLE_UNIT = "cm"

    // Onboarding — Step 2 : Diet type
    const val ONBOARDING_STEP2_TITLE = "Votre regime alimentaire"
    const val ONBOARDING_REGIME_VEGAN = "Vegan"
    const val ONBOARDING_REGIME_VEGAN_DESC = "Aucun produit d'origine animale"
    const val ONBOARDING_REGIME_VEGETARIEN = "Vegetarien"
    const val ONBOARDING_REGIME_VEGETARIEN_DESC = "Pas de viande ni poisson"
    const val ONBOARDING_REGIME_FLEXITARIEN = "Flexitarien"
    const val ONBOARDING_REGIME_FLEXITARIEN_DESC = "Principalement vegetal, viande occasionnelle"
    const val ONBOARDING_REGIME_OMNIVORE = "Omnivore"
    const val ONBOARDING_REGIME_OMNIVORE_DESC = "Aucune restriction"

    // Onboarding — Step 3 : Activity level
    const val ONBOARDING_STEP3_TITLE = "Votre activite physique"
    const val ONBOARDING_ACTIVITE_SEDENTAIRE = "Sedentaire"
    const val ONBOARDING_ACTIVITE_SEDENTAIRE_DESC = "Peu ou pas d'exercice"
    const val ONBOARDING_ACTIVITE_LEGER = "Legerement actif"
    const val ONBOARDING_ACTIVITE_LEGER_DESC = "Exercice leger 1-3 jours/semaine"
    const val ONBOARDING_ACTIVITE_MODERE = "Moderement actif"
    const val ONBOARDING_ACTIVITE_MODERE_DESC = "Exercice modere 3-5 jours/semaine"
    const val ONBOARDING_ACTIVITE_ACTIF = "Actif"
    const val ONBOARDING_ACTIVITE_ACTIF_DESC = "Exercice intense 6-7 jours/semaine"
    const val ONBOARDING_ACTIVITE_TRES_ACTIF = "Tres actif"
    const val ONBOARDING_ACTIVITE_TRES_ACTIF_DESC = "Exercice tres intense ou travail physique"

    // Onboarding — Step 4 : Exclusions/allergies (optional)
    const val ONBOARDING_STEP4_TITLE = "Allergies et exclusions"
    const val ONBOARDING_STEP4_SUBTITLE = "Facultatif — vous pourrez modifier ces choix plus tard"
    const val ONBOARDING_ALLERGIES_LABEL = "Allergies courantes"
    const val ONBOARDING_EXCLUSIONS_SEARCH_LABEL = "Rechercher un aliment a exclure"

    // Allergies predefined
    const val ALLERGIE_GLUTEN = "Gluten"
    const val ALLERGIE_SOJA = "Soja"
    const val ALLERGIE_FRUITS_A_COQUE = "Fruits a coque"
    const val ALLERGIE_ARACHIDES = "Arachides"
    const val ALLERGIE_LAIT = "Lait"
    const val ALLERGIE_OEUFS = "Oeufs"
    const val ALLERGIE_SESAME = "Sesame"
    const val ALLERGIE_LUPIN = "Lupin"
    const val ALLERGIE_CELERI = "Celeri"
    const val ALLERGIE_MOUTARDE = "Moutarde"
    const val ALLERGIE_SULFITES = "Sulfites"
    const val ALLERGIE_CRUSTACES = "Crustaces"
    const val ALLERGIE_MOLLUSQUES = "Mollusques"
    const val ALLERGIE_POISSON = "Poisson"

    // Onboarding — Validation
    const val VALIDATION_AGE_RANGE = "L'age doit etre entre 1 et 120 ans"
    const val VALIDATION_POIDS_RANGE = "Le poids doit etre entre 20 et 500 kg"
    const val VALIDATION_TAILLE_RANGE = "La taille doit etre entre 50 et 300 cm"

    // Profil — Edit
    const val PROFIL_SAVE_BUTTON = "Sauvegarder"
    const val PROFIL_SAVE_SUCCESS = "Profil mis a jour avec succes"
    const val PROFIL_SECTION_BODY = "Informations corporelles"
    const val PROFIL_SECTION_DIET = "Regime alimentaire"
    const val PROFIL_SECTION_ACTIVITY = "Activite physique"

    // Profil — Preferences alimentaires
    const val PREFERENCES_TITLE = "Preferences alimentaires"
    const val PREFERENCES_EXCLUDED_SECTION = "Aliments exclus"
    const val PREFERENCES_ALLERGIES_SECTION = "Allergies"
    const val PREFERENCES_SEARCH_PLACEHOLDER = "Rechercher un aliment..."
    const val PREFERENCES_NO_EXCLUSIONS = "Aucun aliment exclu"
    const val PREFERENCES_REMOVE = "Retirer"
    const val PREFERENCES_SAVE_SUCCESS = "Preferences mises a jour"

    // Profil — Delete account
    const val DELETE_ACCOUNT_BUTTON = "Supprimer mon compte"
    const val DELETE_ACCOUNT_DIALOG_TITLE = "Supprimer votre compte ?"
    const val DELETE_ACCOUNT_DIALOG_MESSAGE = "Cette action est irreversible. Toutes vos donnees seront definitivement supprimees."
    const val DELETE_ACCOUNT_CONFIRM = "Oui, supprimer mon compte"
    const val DELETE_ACCOUNT_CANCEL = "Annuler"
    const val DELETE_ACCOUNT_CONFIRM_AGAIN = "Je confirme vouloir supprimer mon compte"

    // Profil — Menu
    const val PROFIL_EDIT = "Modifier le profil"
    const val PROFIL_PREFERENCES = "Preferences alimentaires"
    const val PROFIL_SETTINGS = "Parametres"
    const val PROFIL_LOGOUT = "Se deconnecter"
}
