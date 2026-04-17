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

    // UX-02 — Empty states
    const val EMPTY_DASHBOARD_TITLE = "Bienvenue sur appFood !"
    const val EMPTY_DASHBOARD_MESSAGE = "Commence par ajouter ton premier repas pour suivre tes apports nutritionnels."
    const val EMPTY_DASHBOARD_CTA = "Ajouter mon premier repas"
    const val EMPTY_JOURNAL_TITLE = "Ton journal est vide"
    const val EMPTY_JOURNAL_MESSAGE = "Ajoute un aliment ou une recette pour commencer a suivre tes apports du jour."
    const val EMPTY_JOURNAL_CTA = "Ajouter un aliment"
    const val EMPTY_FAVORITES_TITLE = "Pas encore de favoris"
    const val EMPTY_FAVORITES_MESSAGE = "Ajoute des aliments en favoris pour les retrouver rapidement lors de tes prochaines saisies."

    // UX-03 — Loading states
    const val LOADING_DASHBOARD = "Chargement du tableau de bord..."
    const val LOADING_SEARCH = "Recherche en cours..."

    // UX-04 — Error states
    const val ERROR_NETWORK = "Pas de connexion \u2014 tes donnees seront synchronisees plus tard"
    const val ERROR_SERVER = "Oups, quelque chose a coince \u2014 reessaie dans quelques instants"
    const val ERROR_SEARCH_NO_RESULTS = "Aucun aliment trouve \u2014 essaie un autre mot"
    const val ERROR_SEARCH_FAILED = "La recherche a echoue \u2014 verifie ta connexion"
    const val ERROR_RETRY = "Reessayer"
    const val ERROR_MODIFY_SEARCH = "Modifier la recherche"

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

    // Auth — Erreurs Firebase
    const val AUTH_ERROR_INVALID_CREDENTIALS = "E-mail ou mot de passe incorrect"
    const val AUTH_ERROR_USER_NOT_FOUND = "Aucun compte trouve avec cette adresse e-mail"
    const val AUTH_ERROR_EMAIL_IN_USE = "Cette adresse e-mail est deja utilisee"
    const val AUTH_ERROR_WEAK_PASSWORD = "Le mot de passe est trop faible (minimum 6 caracteres)"
    const val AUTH_ERROR_NETWORK = "Erreur reseau, verifiez votre connexion"
    const val AUTH_ERROR_GENERIC = "Une erreur est survenue, veuillez reessayer"

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
    const val PREFERENCES_SEARCH_LOADING = "Recherche en cours..."
    const val PREFERENCES_SEARCH_NO_RESULTS = "Aucun resultat pour cette recherche"
    const val PREFERENCES_SEARCH_ERROR = "Erreur de recherche. Verifiez votre connexion."

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

    // Profil — Export data
    const val PROFIL_EXPORT_BUTTON = "Exporter mes donnees"
    const val PROFIL_EXPORT_LOADING = "Export en cours..."
    const val PROFIL_EXPORT_SUCCESS_TITLE = "Export termine"
    const val PROFIL_EXPORT_SUCCESS_MESSAGE = "Vos donnees ont ete exportees avec succes."
    const val PROFIL_EXPORT_ERROR = "Erreur lors de l'export"
    const val PROFIL_EXPORT_CLOSE = "Fermer"
    const val PROFIL_EXPORT_SUMMARY_JOURNAL = "Entrees journal : "
    const val PROFIL_EXPORT_SUMMARY_QUOTAS = "Quotas : "
    const val PROFIL_EXPORT_SUMMARY_POIDS = "Historique poids : "
    const val PROFIL_EXPORT_SUMMARY_HYDRATATION = "Hydratation : "
    const val PROFIL_EXPORT_SUMMARY_DATE = "Exporte le : "

    // Journal — General
    const val JOURNAL_ADD_ENTRY_TITLE = "Ajouter un aliment"
    const val JOURNAL_BACK = "Retour"
    const val JOURNAL_SELECT_MEAL_TYPE = "Quel repas ?"

    // Journal — Meal types
    const val JOURNAL_MEAL_BREAKFAST = "Petit-dejeuner"
    const val JOURNAL_MEAL_BREAKFAST_DESC = "Premier repas de la journee"
    const val JOURNAL_MEAL_LUNCH = "Dejeuner"
    const val JOURNAL_MEAL_LUNCH_DESC = "Repas du midi"
    const val JOURNAL_MEAL_DINNER = "Diner"
    const val JOURNAL_MEAL_DINNER_DESC = "Repas du soir"
    const val JOURNAL_MEAL_SNACK = "Collation"
    const val JOURNAL_MEAL_SNACK_DESC = "En-cas ou gouter"

    // Journal — Search (JOURNAL-01)
    const val JOURNAL_SEARCH_TITLE = "Rechercher un aliment"
    const val JOURNAL_SEARCH_PLACEHOLDER = "Rechercher un aliment..."
    const val JOURNAL_SEARCH_NO_RESULTS = "Aucun aliment trouve"
    const val JOURNAL_SEARCH_HINT = "Commencez a taper pour rechercher un aliment"
    fun journalCaloriesPer100g(calories: Double) = "${calories.toLong()} kcal / 100g"
    fun journalCaloriesShort(calories: Double) = "${calories.toLong()} kcal"

    // Journal — Favorites (JOURNAL-03)
    const val JOURNAL_FAVORITES_TITLE = "Favoris"
    const val JOURNAL_ADD_FAVORITE = "Ajouter aux favoris"
    const val JOURNAL_REMOVE_FAVORITE = "Retirer des favoris"

    // Journal — Recents (JOURNAL-04)
    const val JOURNAL_RECENTS_TITLE = "Recents"

    // Journal — Portion selector (PORTIONS-01)
    const val JOURNAL_PORTION_QUANTITY_LABEL = "Quantite"
    const val JOURNAL_PORTION_GRAMS = "Grammes"
    const val JOURNAL_PORTION_QUICK_SUGGESTIONS = "Suggestions rapides"
    const val JOURNAL_PORTION_STANDARD = "Portions standard"
    const val JOURNAL_PORTION_GENERIC = "Portions generiques"
    const val JOURNAL_PORTION_CUP = "Bol"
    const val JOURNAL_PORTION_TABLESPOON = "Cuillere a soupe"
    const val JOURNAL_PORTION_TEASPOON = "Cuillere a cafe"
    const val JOURNAL_PORTION_HANDFUL = "Poignee"
    const val JOURNAL_NUTRITION_SUMMARY = "Resume nutritionnel"
    const val JOURNAL_NUTRITION_PREVIEW_TITLE = "Apercu nutritionnel"
    const val JOURNAL_VALIDATE_ENTRY = "Valider"
    const val JOURNAL_SAVED_OFFLINE = "Enregistre hors ligne, sera synchronise automatiquement"
    fun journalPortionSummaryFor(grams: Double) = "Pour ${grams.toLong()}g :"

    // Journal — Nutriments
    const val JOURNAL_NUTRIMENT_CALORIES = "Calories"
    const val JOURNAL_NUTRIMENT_PROTEINES = "Proteines"
    const val JOURNAL_NUTRIMENT_GLUCIDES = "Glucides"
    const val JOURNAL_NUTRIMENT_LIPIDES = "Lipides"
    const val JOURNAL_NUTRIMENT_FIBRES = "Fibres"
    const val JOURNAL_UNIT_KCAL = "kcal"
    const val JOURNAL_UNIT_G = "g"

    // Journal — Edit/Delete (JOURNAL-06)
    const val JOURNAL_EDIT_QUANTITY = "Modifier la quantite"
    const val JOURNAL_DELETE_ENTRY = "Supprimer"
    const val JOURNAL_DELETE_CONFIRM_TITLE = "Supprimer cette entree ?"
    const val JOURNAL_DELETE_CONFIRM_MESSAGE = "Cette entree sera supprimee du journal."
    const val JOURNAL_DELETE_CONFIRM = "Supprimer"
    const val JOURNAL_DELETE_CANCEL = "Annuler"
    const val JOURNAL_ENTRY_UPDATED = "Entree mise a jour"
    const val JOURNAL_ENTRY_DELETED = "Entree supprimee"

    // Journal — Navigation
    const val SCREEN_ADD_ENTRY = "Ajouter un aliment"
    const val SCREEN_SEARCH_ALIMENT = "Rechercher un aliment"
    const val SCREEN_PORTION_SELECTOR = "Choisir la portion"

    // Dashboard (DASHBOARD-01)
    const val DASHBOARD_TITLE = "Tableau de bord"
    const val DASHBOARD_CALORIES_LABEL = "Calories"
    const val DASHBOARD_MACROS_TITLE = "Macronutriments"
    const val DASHBOARD_VITAMINES_TITLE = "Vitamines"
    const val DASHBOARD_MINERAUX_TITLE = "Mineraux"
    const val DASHBOARD_ACIDES_GRAS_TITLE = "Acides gras"
    const val DASHBOARD_REPAS_TITLE = "Repas du jour"
    const val DASHBOARD_ADD_MEAL = "Ajouter un repas"
    const val DASHBOARD_NO_ENTRY = "Aucune saisie"
    const val DASHBOARD_SEE_RECOMMENDATIONS = "Voir les suggestions"

    // PROFIL-01 — Bandeau rappel profil incomplet
    const val DASHBOARD_INCOMPLETE_PROFILE_MESSAGE = "Complete ton profil pour des recommandations personnalisees"
    const val DASHBOARD_INCOMPLETE_PROFILE_CTA = "Completer"

    // Quotas (QUOTAS-02)
    const val QUOTAS_TITLE = "Gestion des quotas"
    const val QUOTAS_CALCULATED = "Calcule"
    const val QUOTAS_CUSTOM = "Personnalise"
    const val QUOTAS_RESET = "Revenir au calcul automatique"
    const val QUOTAS_RESET_ALL = "Reinitialiser tous les quotas"
    const val QUOTAS_EDIT = "Modifier"
    const val QUOTAS_SAVE = "Enregistrer"

    // Recommandations (RECO-01)
    const val RECO_TITLE = "Suggestions"
    const val RECO_SUBTITLE = "Aliments pour combler vos manques"
    const val RECO_SUGGESTED_QUANTITY = "%s suggeres"
    const val RECO_COVERS = "Couvre"
    const val RECO_ATE_THIS = "J'ai mange ca"
    const val RECO_NO_DEFICIT = "Tous vos quotas sont atteints, bravo !"

    // Poids (POIDS-01)
    const val SCREEN_POIDS = "Mon poids"
    const val POIDS_SAISIE_TITLE = "Nouvelle pesee"
    const val POIDS_LABEL = "Poids"
    const val POIDS_UNIT = "kg"
    const val POIDS_SAVE_BUTTON = "Enregistrer"
    const val POIDS_SAVE_SUCCESS = "Poids enregistre avec succes"
    const val POIDS_SAVE_ERROR = "Erreur lors de l'enregistrement du poids"
    const val POIDS_NO_DATA = "Aucune pesee enregistree"
    const val POIDS_CURRENT = "Actuel"
    const val POIDS_MIN = "Min"
    const val POIDS_MAX = "Max"
    const val POIDS_PERIOD_WEEK = "Semaine"
    const val POIDS_PERIOD_MONTH = "Mois"
    const val POIDS_PERIOD_3MONTHS = "3 mois"
    const val POIDS_PERIOD_6MONTHS = "6 mois"
    const val POIDS_PERIOD_YEAR = "1 an"
    const val POIDS_SIGNIFICANT_CHANGE = "Votre poids a change significativement. Voulez-vous recalculer vos quotas ?"
    const val POIDS_RECALCULATE_YES = "Recalculer"
    const val POIDS_RECALCULATE_NO = "Plus tard"

    // Recettes (RECETTES-01)
    const val RECETTE_SEARCH_PLACEHOLDER = "Rechercher une recette..."
    const val RECETTE_EMPTY = "Aucune recette trouvee"
    const val RECETTE_FILTER_REGIME = "Regime"
    const val RECETTE_FILTER_TYPE_REPAS = "Type de repas"
    const val RECETTE_CLEAR_FILTERS = "Effacer les filtres"
    const val RECETTE_SORT_LABEL = "Trier par"
    const val RECETTE_SORT_PERTINENCE = "Pertinence"
    const val RECETTE_SORT_POPULARITE = "Popularite"
    const val RECETTE_SORT_TEMPS = "Temps de preparation"
    const val RECETTE_REGIME_VEGAN = "Vegan"
    const val RECETTE_REGIME_VEGETARIEN = "Vegetarien"
    const val RECETTE_REGIME_FLEXITARIEN = "Flexitarien"
    const val RECETTE_REGIME_OMNIVORE = "Omnivore"
    fun recetteTempsPrep(minutes: Int) = "${minutes} min"

    // Hydratation (HYDRA-01)
    const val HYDRA_TITLE = "Hydratation"
    const val HYDRA_OBJECTIVE = "Objectif"
    const val HYDRA_ADD_GLASS = "Verre (250 ml)"
    const val HYDRA_ADD_BOTTLE = "Bouteille (500 ml)"
    const val HYDRA_ADD_CUSTOM = "Autre quantite"
    const val HYDRA_CUSTOM_LABEL = "Quantite (ml)"
    const val HYDRA_WEEKLY_TITLE = "Cette semaine"
    const val HYDRA_MODIFY_OBJECTIVE = "Modifier l'objectif"
    const val HYDRA_RESET_OBJECTIVE = "Objectif automatique"
    fun hydraProgress(current: Int, target: Int) = "${current} / ${target} ml"

    // Dashboard hebdo (DASHBOARD-02)
    const val WEEKLY_TITLE = "Vue hebdomadaire"
    const val WEEKLY_AVERAGE = "Moyenne"
    const val WEEKLY_CRITICAL_NUTRIENTS = "Nutriments critiques"
    const val WEEKLY_IMPROVEMENTS = "Ameliorations"
    const val WEEKLY_DEGRADATIONS = "En baisse"
    const val WEEKLY_PREVIOUS = "Semaine precedente"
    const val WEEKLY_NEXT = "Semaine suivante"

    // Recette detail (RECETTES-02)
    const val RECETTE_DETAIL_TITLE = "Detail recette"
    const val RECETTE_INGREDIENTS_TITLE = "Ingredients"
    const val RECETTE_ETAPES_TITLE = "Preparation"
    const val RECETTE_NUTRITION_TITLE = "Valeurs nutritionnelles (par portion)"
    const val RECETTE_PORTIONS_LABEL = "Portions"
    const val RECETTE_ADD_TO_JOURNAL = "Ajouter a mon journal"
    const val RECETTE_ADDED_TO_JOURNAL = "Recette ajoutee au journal !"
    const val RECETTE_ADDED_TO_JOURNAL_OFFLINE = "Recette enregistree (hors-ligne)"
    const val RECETTE_ADD_CANCEL = "Annuler"
    const val RECETTE_TEMPS_PREP_LABEL = "Preparation"
    const val RECETTE_TEMPS_CUISSON_LABEL = "Cuisson"
    const val RECETTE_FAVORIS_ADD = "Ajouter aux favoris"
    const val RECETTE_FAVORIS_REMOVE = "Retirer des favoris"
    fun recetteTempsCuisson(minutes: Int) = "${minutes} min"
    fun recettePortionCount(count: Int) = "$count portion${if (count > 1) "s" else ""}"
    fun recetteEtapeNumber(number: Int) = "Etape $number"
    fun recetteIngredientQuantity(nom: String, grammes: Double) = "$nom — ${grammes.toLong()}g"

    // Create recette (RECETTES-03)
    const val RECETTE_CREATE_TITLE = "Creer une recette"
    const val RECETTE_CREATE_NOM_LABEL = "Nom de la recette"
    const val RECETTE_CREATE_DESCRIPTION_LABEL = "Description"
    const val RECETTE_CREATE_IMAGE_URL_LABEL = "URL de la photo (optionnel)"
    const val RECETTE_CREATE_TEMPS_PREP_LABEL = "Temps de preparation (min)"
    const val RECETTE_CREATE_TEMPS_CUISSON_LABEL = "Temps de cuisson (min)"
    const val RECETTE_CREATE_NB_PORTIONS_LABEL = "Nombre de portions"
    const val RECETTE_CREATE_REGIME_LABEL = "Regime alimentaire"
    const val RECETTE_CREATE_ADD_INGREDIENT = "Ajouter un ingredient"
    const val RECETTE_CREATE_INGREDIENT_SEARCH = "Rechercher un aliment"
    const val RECETTE_CREATE_INGREDIENT_QUANTITY = "Quantite (g)"
    const val RECETTE_CREATE_ADD_ETAPE = "Ajouter une etape"
    const val RECETTE_CREATE_ETAPE_PLACEHOLDER = "Decrivez cette etape..."
    const val RECETTE_CREATE_SUBMIT = "Creer la recette"
    const val RECETTE_CREATE_SUCCESS = "Recette creee avec succes"
    const val RECETTE_CREATE_REMOVE_INGREDIENT = "Supprimer"
    const val RECETTE_CREATE_REMOVE_ETAPE = "Supprimer"
    const val RECETTE_CREATE_MOVE_UP = "Monter"
    const val RECETTE_CREATE_MOVE_DOWN = "Descendre"

    // Journal — Recette integration (JOURNAL-02)
    const val JOURNAL_MODE_ALIMENT = "Aliment"
    const val JOURNAL_MODE_RECETTE = "Recette"
    const val JOURNAL_SEARCH_RECETTE_PLACEHOLDER = "Rechercher une recette..."
    const val JOURNAL_RECETTE_NO_RESULTS = "Aucune recette trouvee"
    const val JOURNAL_RECETTE_ADD_PORTIONS = "Ajouter au journal"

    // Recommandations recettes (RECO-02)
    const val RECO_RECETTES_TITLE = "Recettes suggerees"
    const val RECO_RECETTE_ADD_JOURNAL = "Ajouter au journal"
    fun recoRecetteCoverage(percent: Int) = "Couvre $percent% de vos besoins"
    fun recoRecetteNutrients(nutrients: String) = "Nutriments : $nutrients"

    // Recommandations — validation rapide (RECO-03)
    const val RECO_PORTIONS_LABEL = "Portions"
    fun recoPortionCount(count: Int) = "$count portion${if (count > 1) "s" else ""}"
    fun recoAlimentAddedToJournal(alimentNom: String) = "$alimentNom ajoute au journal"
    const val RECO_ADDED_TO_JOURNAL_SUCCESS = "Recette ajoutee au journal"

    // Poids — Recalcul quotas (POIDS-02)
    const val POIDS_RECALCUL_DIALOG_TITLE = "Recalculer vos quotas ?"
    fun poidsRecalculDialogMessage(changeKg: Double) =
        "Votre poids a change de ${((kotlin.math.abs(changeKg) * 10).toInt() / 10.0)} kg. Voulez-vous recalculer vos quotas nutritionnels ?"
    const val POIDS_RECALCUL_SUCCESS = "Quotas recalcules avec succes"

    // POIDS-02 — Historique des recalculs
    const val POIDS_RECALCUL_HISTORY_TITLE = "Historique des ajustements"
    const val POIDS_RECALCUL_HISTORY_EMPTY = "Aucun recalcul de quotas enregistre"
    fun poidsRecalculHistoryEntry(date: String, ancienPoids: Double, nouveauPoids: Double) =
        "$date : $ancienPoids kg -> $nouveauPoids kg"

    // Disclaimer (UX-05)
    const val DISCLAIMER_TITLE = "Information importante"
    const val DISCLAIMER_TEXT = "appFood est un outil d'aide au suivi nutritionnel. Il ne remplace pas l'avis d'un professionnel de sante."
    const val DISCLAIMER_ACCEPT_BUTTON = "J'ai compris"
    const val SCREEN_DISCLAIMER = "Disclaimer"

    // Consent (LEGAL-03)
    const val CONSENT_TITLE = "Vos choix de confidentialite"
    const val CONSENT_SUBTITLE = "Avant de commencer, choisissez les donnees que vous acceptez de partager. Vous pourrez modifier ces choix a tout moment dans les parametres."
    const val CONSENT_ANALYTICS_LABEL = "Statistiques d'utilisation"
    const val CONSENT_ANALYTICS_DESC = "Nous aide a comprendre comment l'application est utilisee pour l'ameliorer."
    const val CONSENT_ADVERTISING_LABEL = "Publicite personnalisee"
    const val CONSENT_ADVERTISING_DESC = "Permet d'afficher des publicites adaptees a vos centres d'interet."
    const val CONSENT_IMPROVEMENT_LABEL = "Amelioration du service"
    const val CONSENT_IMPROVEMENT_DESC = "Permet d'analyser vos habitudes nutritionnelles de maniere anonymisee pour ameliorer nos recommandations."
    const val CONSENT_ACCEPT_ALL = "Tout accepter"
    const val CONSENT_REFUSE_ALL = "Tout refuser"
    const val CONSENT_CONFIRM = "Confirmer mes choix"
    const val CONSENT_PRIVACY_NOTICE = "Aucun tracking n'est active sans votre consentement explicite. Conforme RGPD et ePrivacy."
    const val CONSENT_SETTINGS_TITLE = "Gestion des consentements"
    const val CONSENT_SETTINGS_SUBTITLE = "Modifiez vos choix de confidentialite a tout moment."
    const val CONSENT_SETTINGS_SAVED = "Vos preferences de confidentialite ont ete mises a jour."

    // Offline
    const val OFFLINE_BANNER_MESSAGE = "Mode hors ligne — vos donnees seront synchronisees automatiquement"

    // About (SUPPORT-01)
    const val ABOUT_TITLE = "A propos"
    const val ABOUT_APP_VERSION_LABEL = "Version"
    const val ABOUT_APP_VERSION = "1.0.0"
    const val ABOUT_CONTACT_TITLE = "Nous contacter"
    const val ABOUT_SUPPORT_EMAIL = "support@appfood.fr"
    const val ABOUT_SUPPORT_EMAIL_LABEL = "E-mail de support"
    const val ABOUT_LEGAL_TITLE = "Informations legales"
    const val ABOUT_CGU = "Conditions generales d'utilisation"
    const val ABOUT_PRIVACY_POLICY = "Politique de confidentialite"
    const val ABOUT_LEGAL_MENTIONS_TITLE = "Mentions legales"
    const val ABOUT_EDITOR_LABEL = "Editeur"
    const val ABOUT_EDITOR_VALUE = "appFood SAS"
    const val ABOUT_HOST_LABEL = "Hebergeur"
    const val ABOUT_HOST_VALUE = "Railway (railway.app)"
    const val ABOUT_DESCRIPTION = "appFood est une application de suivi nutritionnel personnalise pour vegans, vegetariens et sportifs."
    const val ABOUT_BACK = "Retour"

    // Settings
    const val SETTINGS_TITLE = "Parametres"
    const val SETTINGS_BACK = "Retour"
    const val SETTINGS_SECTION_GENERAL = "General"
    const val SETTINGS_SECTION_HEALTH = "Sante et suivi"
    const val SETTINGS_SECTION_LEGAL = "Informations legales"
    const val SETTINGS_ABOUT = "A propos"
    const val SETTINGS_CONSENT = "Gestion des consentements"
    const val SETTINGS_PRIVACY = "Politique de confidentialite"
    const val SETTINGS_CGU = "Conditions generales d'utilisation"
    const val SETTINGS_POIDS = "Suivi du poids"
    const val SETTINGS_HYDRATATION = "Hydratation"
    const val SETTINGS_QUOTAS = "Gestion des quotas"
    const val SETTINGS_FAQ = "Questions frequentes"
    const val SETTINGS_VERSION = "Version de l'application"

    // FAQ (SUPPORT-02)
    const val FAQ_TITLE = "Questions frequentes"
    const val FAQ_BACK = "Retour"
    const val FAQ_LOADING = "Chargement de la FAQ..."
    const val FAQ_ERROR = "Impossible de charger la FAQ"
    const val FAQ_CONTACT_SUPPORT_TITLE = "Ma question n'est pas ici"
    const val FAQ_CONTACT_SUPPORT_BUTTON = "Contacter le support"
    const val FAQ_THEME_COMPTE = "Compte"
    const val FAQ_THEME_SAISIE = "Saisie"
    const val FAQ_THEME_QUOTAS = "Quotas"
    const val FAQ_THEME_RECETTES = "Recettes"
    const val FAQ_THEME_DONNEES = "Donnees"

    // Legal — Privacy Policy (LEGAL-01)
    const val LEGAL_PRIVACY_POLICY_TITLE = "Politique de confidentialite"
    const val LEGAL_PRIVACY_POLICY_VERSION = "Version 1.0 — 8 avril 2026"
    const val LEGAL_PRIVACY_POLICY_INTRO = "La presente politique de confidentialite decrit comment appFood (ci-apres \u00ab nous \u00bb, \u00ab notre \u00bb) collecte, utilise et protege vos donnees personnelles conformement au Reglement General sur la Protection des Donnees (RGPD) et a la legislation francaise applicable."
    const val LEGAL_PRIVACY_POLICY_SECTION_1_TITLE = "1. Responsable du traitement"
    const val LEGAL_PRIVACY_POLICY_SECTION_1_BODY = "[Placeholder — Nom et coordonnees du responsable de traitement a completer par un juriste]"
    const val LEGAL_PRIVACY_POLICY_SECTION_2_TITLE = "2. Donnees collectees"
    const val LEGAL_PRIVACY_POLICY_SECTION_2_BODY = "Nous collectons les categories de donnees suivantes :\n\n\u2022 Donnees d'identification : adresse e-mail, identifiant unique\n\u2022 Donnees de sante et alimentaires : poids, taille, age, sexe, regime alimentaire, allergies, journal alimentaire, hydratation, quotas nutritionnels\n\u2022 Donnees d'utilisation : historique de navigation dans l'application, preferences\n\u2022 Donnees techniques : identifiant d'appareil, systeme d'exploitation\n\nLes donnees alimentaires et de sante sont des donnees sensibles au sens de l'article 9 du RGPD. Leur traitement repose sur votre consentement explicite."
    const val LEGAL_PRIVACY_POLICY_SECTION_3_TITLE = "3. Finalites du traitement"
    const val LEGAL_PRIVACY_POLICY_SECTION_3_BODY = "Vos donnees sont traitees pour les finalites suivantes :\n\n\u2022 Calcul de vos apports et quotas nutritionnels personnalises\n\u2022 Recommandations alimentaires adaptees a votre profil\n\u2022 Suivi de votre poids et de votre hydratation\n\u2022 Synchronisation de vos donnees entre vos appareils\n\u2022 Amelioration du service et statistiques anonymisees"
    const val LEGAL_PRIVACY_POLICY_SECTION_4_TITLE = "4. Base legale du traitement"
    const val LEGAL_PRIVACY_POLICY_SECTION_4_BODY = "Le traitement de vos donnees repose sur :\n\n\u2022 Votre consentement explicite (article 6.1.a et 9.2.a du RGPD) pour les donnees de sante et alimentaires\n\u2022 L'execution du contrat (article 6.1.b du RGPD) pour la fourniture du service\n\u2022 Notre interet legitime (article 6.1.f du RGPD) pour l'amelioration du service"
    const val LEGAL_PRIVACY_POLICY_SECTION_5_TITLE = "5. Duree de conservation"
    const val LEGAL_PRIVACY_POLICY_SECTION_5_BODY = "Vos donnees sont conservees pendant toute la duree d'utilisation de votre compte, puis supprimees dans un delai de 30 jours suivant la suppression de votre compte.\n\nLes donnees anonymisees a des fins statistiques peuvent etre conservees sans limitation de duree."
    const val LEGAL_PRIVACY_POLICY_SECTION_6_TITLE = "6. Partage et transfert des donnees"
    const val LEGAL_PRIVACY_POLICY_SECTION_6_BODY = "Vos donnees ne sont jamais vendues a des tiers. Elles peuvent etre partagees avec :\n\n\u2022 Nos sous-traitants techniques (hebergement, authentification) dans le cadre strict de la fourniture du service\n\u2022 Les autorites competentes en cas d'obligation legale\n\n[Placeholder — Liste des sous-traitants a completer par un juriste]"
    const val LEGAL_PRIVACY_POLICY_SECTION_7_TITLE = "7. Vos droits"
    const val LEGAL_PRIVACY_POLICY_SECTION_7_BODY = "Conformement au RGPD, vous disposez des droits suivants :\n\n\u2022 Droit d'acces a vos donnees\n\u2022 Droit de rectification\n\u2022 Droit a l'effacement (\u00ab droit a l'oubli \u00bb)\n\u2022 Droit a la portabilite\n\u2022 Droit d'opposition\n\u2022 Droit a la limitation du traitement\n\u2022 Droit de retrait du consentement a tout moment\n\nPour exercer ces droits, contactez-nous a : [placeholder@email.com]\n\nVous disposez egalement du droit d'introduire une reclamation aupres de la CNIL (www.cnil.fr)."
    const val LEGAL_PRIVACY_POLICY_SECTION_8_TITLE = "8. Securite des donnees"
    const val LEGAL_PRIVACY_POLICY_SECTION_8_BODY = "Nous mettons en oeuvre des mesures techniques et organisationnelles appropriees pour proteger vos donnees, notamment :\n\n\u2022 Chiffrement des donnees sensibles en base de donnees\n\u2022 Communications chiffrees (HTTPS/TLS)\n\u2022 Authentification securisee\n\u2022 Controle d'acces strict"
    const val LEGAL_PRIVACY_POLICY_SECTION_9_TITLE = "9. Modifications de la politique"
    const val LEGAL_PRIVACY_POLICY_SECTION_9_BODY = "Nous nous reservons le droit de modifier cette politique. Toute modification substantielle vous sera notifiee dans l'application. La date de derniere mise a jour est indiquee en haut de ce document."
    const val LEGAL_PRIVACY_POLICY_HISTORY_TITLE = "Historique des modifications"
    const val LEGAL_PRIVACY_POLICY_HISTORY_BODY = "\u2022 Version 1.0 (8 avril 2026) — Version initiale"

    // Legal — Terms of Service (LEGAL-02)
    const val LEGAL_TOS_TITLE = "Conditions Generales d'Utilisation"
    const val LEGAL_TOS_VERSION = "Version 1.0 — 8 avril 2026"
    const val LEGAL_TOS_INTRO = "Les presentes Conditions Generales d'Utilisation (ci-apres \u00ab CGU \u00bb) regissent l'utilisation de l'application mobile appFood (ci-apres \u00ab l'Application \u00bb). En utilisant l'Application, vous acceptez sans reserve les presentes CGU."
    const val LEGAL_TOS_SECTION_1_TITLE = "1. Objet du service"
    const val LEGAL_TOS_SECTION_1_BODY = "appFood est une application de suivi nutritionnel personnalise destinee aux personnes suivant un regime vegetarien, vegan ou flexitarien, et/ou pratiquant une activite sportive. L'Application permet de :\n\n\u2022 Suivre ses apports nutritionnels quotidiens\n\u2022 Obtenir des recommandations alimentaires personnalisees\n\u2022 Gerer ses quotas nutritionnels\n\u2022 Suivre son poids et son hydratation\n\u2022 Consulter des recettes adaptees a son regime"
    const val LEGAL_TOS_SECTION_2_TITLE = "2. Avertissement medical"
    const val LEGAL_TOS_SECTION_2_BODY = "L'Application ne constitue en aucun cas un avis medical, un diagnostic ou un traitement. Les informations et recommandations fournies sont a titre indicatif uniquement et ne se substituent pas a une consultation aupres d'un professionnel de sante (medecin, dieteticien, nutritionniste).\n\nEn cas de doute sur votre alimentation ou votre sante, consultez un professionnel de sante qualifie."
    const val LEGAL_TOS_SECTION_3_TITLE = "3. Inscription et compte utilisateur"
    const val LEGAL_TOS_SECTION_3_BODY = "L'utilisation de l'Application necessite la creation d'un compte. Vous etes responsable de la confidentialite de vos identifiants et de toute activite effectuee sous votre compte.\n\nVous vous engagez a fournir des informations exactes et a les maintenir a jour."
    const val LEGAL_TOS_SECTION_4_TITLE = "4. Responsabilites de l'utilisateur"
    const val LEGAL_TOS_SECTION_4_BODY = "En utilisant l'Application, vous vous engagez a :\n\n\u2022 Utiliser l'Application conformement a sa destination\n\u2022 Ne pas tenter d'acceder de maniere non autorisee aux systemes\n\u2022 Ne pas utiliser l'Application a des fins illegales ou prejudiciables\n\u2022 Fournir des informations exactes concernant votre profil nutritionnel"
    const val LEGAL_TOS_SECTION_5_TITLE = "5. Responsabilites de l'editeur"
    const val LEGAL_TOS_SECTION_5_BODY = "Nous nous efforcons d'assurer la disponibilite et le bon fonctionnement de l'Application. Toutefois, nous ne garantissons pas un fonctionnement ininterrompu.\n\nNous ne saurions etre tenus responsables :\n\n\u2022 Des decisions alimentaires prises sur la base des informations de l'Application\n\u2022 Des dommages resultant d'une utilisation non conforme\n\u2022 Des interruptions temporaires du service pour maintenance\n\n[Placeholder — Clauses de responsabilite a completer par un juriste]"
    const val LEGAL_TOS_SECTION_6_TITLE = "6. Propriete intellectuelle"
    const val LEGAL_TOS_SECTION_6_BODY = "L'ensemble des elements de l'Application (design, code, textes, images, logos, base de donnees alimentaire) sont proteges par le droit de la propriete intellectuelle.\n\nToute reproduction, representation ou exploitation non autorisee de tout ou partie de l'Application est interdite.\n\nLes donnees nutritionnelles sont issues de la base Ciqual (ANSES) et d'Open Food Facts, utilisees conformement a leurs licences respectives."
    const val LEGAL_TOS_SECTION_7_TITLE = "7. Donnees personnelles"
    const val LEGAL_TOS_SECTION_7_BODY = "Le traitement de vos donnees personnelles est regi par notre Politique de confidentialite, accessible depuis les parametres de l'Application."
    const val LEGAL_TOS_SECTION_8_TITLE = "8. Modification et resiliation"
    const val LEGAL_TOS_SECTION_8_BODY = "Nous nous reservons le droit de modifier les presentes CGU. Les modifications seront notifiees dans l'Application.\n\nVous pouvez supprimer votre compte a tout moment depuis les parametres de l'Application. La suppression entraine l'effacement de vos donnees conformement a notre Politique de confidentialite."
    const val LEGAL_TOS_SECTION_9_TITLE = "9. Droit applicable"
    const val LEGAL_TOS_SECTION_9_BODY = "Les presentes CGU sont soumises au droit francais. Tout litige sera porte devant les juridictions competentes.\n\n[Placeholder — Juridiction competente a preciser par un juriste]"
    const val LEGAL_TOS_HISTORY_TITLE = "Historique des modifications"
    const val LEGAL_TOS_HISTORY_BODY = "\u2022 Version 1.0 (8 avril 2026) — Version initiale"

    // Legal — Registration consent (LEGAL-01 / LEGAL-02)
    const val REGISTER_ACCEPT_PRIVACY_POLICY = "Politique de confidentialite"
    const val REGISTER_ACCEPT_TOS = "Conditions Generales d'Utilisation"
    const val REGISTER_ACCEPT_LEGAL_PREFIX = "J'accepte les "
    const val REGISTER_ACCEPT_LEGAL_AND = " et la "
    const val REGISTER_CONSENT_REQUIRED = "Vous devez accepter les CGU et la politique de confidentialite pour continuer"

    // Legal — Navigation
    const val SCREEN_PRIVACY_POLICY = "Politique de confidentialite"
    const val SCREEN_TOS = "Conditions Generales d'Utilisation"
}
