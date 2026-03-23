# appFood — Structure du projet

> Source de verite pour l'arborescence du projet.
> Tous les agents doivent respecter cette structure.

---

## 1. Vue d'ensemble des modules

```
appFood/
├── shared/                  # Module KMP partage (Android + iOS)
├── androidApp/              # Application Android (Compose)
├── iosApp/                  # Application iOS (Compose Multiplatform)
├── backend/                 # Serveur Ktor
├── buildSrc/                # Configuration Gradle partagee (versions, plugins)
├── docs/                    # Documentation projet
├── gradle/                  # Wrapper Gradle
├── .github/                 # GitHub Actions workflows
├── docker-compose.yml       # Dev local : Ktor + PostgreSQL + Meilisearch
├── settings.gradle.kts      # Configuration multi-module Gradle
├── build.gradle.kts          # Build root
├── CONVENTIONS.md           # Conventions de code pour les agents
└── CLAUDE.md                # Instructions pour Claude Code
```

---

## 2. Module `shared` (KMP — commonMain)

Le module shared contient **toute la logique metier** et les modeles partages entre Android, iOS et potentiellement le backend.

**Package racine** : `com.appfood.shared`

```
shared/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/com/appfood/shared/
    │   ├── model/                          # Modeles de donnees (data classes)
    │   │   ├── Enums.kt                    # Toutes les enumerations partagees
    │   │   ├── User.kt                     # User, UserProfile, UserPreferences
    │   │   ├── Aliment.kt                  # Aliment, NutrimentValues
    │   │   ├── Recette.kt                  # Recette, IngredientRecette
    │   │   ├── Journal.kt                  # JournalEntry, MealType
    │   │   ├── Quota.kt                    # QuotaJournalier, NutrimentType
    │   │   ├── Poids.kt                    # HistoriquePoids
    │   │   ├── Hydratation.kt              # HydratationJournaliere, HydratationEntry
    │   │   ├── Portion.kt                  # PortionStandard
    │   │   ├── Notification.kt             # AppNotification, NotificationType
    │   │   ├── Consentement.kt             # Consentement, ConsentType
    │   │   └── Recommandation.kt           # RecommandationAliment, RecommandationRecette
    │   │
    │   ├── domain/                         # Logique metier pure (use cases)
    │   │   ├── quota/
    │   │   │   ├── CalculerQuotasUseCase.kt       # Calcul AJR personnalises
    │   │   │   └── RecalculerQuotasUseCase.kt     # Recalcul apres changement poids
    │   │   ├── recommandation/
    │   │   │   ├── RecommandationAlimentUseCase.kt # Algo suggestion aliments
    │   │   │   └── RecommandationRecetteUseCase.kt # Algo suggestion recettes
    │   │   ├── journal/
    │   │   │   ├── AjouterEntreeUseCase.kt         # Ajout aliment/recette au journal
    │   │   │   ├── CalculerApportsUseCase.kt       # Calcul nutriments du jour/semaine
    │   │   │   └── CopierRepasUseCase.kt           # Copie repas/journee
    │   │   ├── hydratation/
    │   │   │   ├── CalculerObjectifHydratationUseCase.kt
    │   │   │   └── AjouterHydratationUseCase.kt
    │   │   └── poids/
    │   │       ├── EnregistrerPoidsUseCase.kt
    │   │       └── DetecterChangementPoidsUseCase.kt
    │   │
    │   ├── data/                           # Couche data (repositories + data sources)
    │   │   ├── repository/                 # Interfaces des repositories
    │   │   │   ├── UserRepository.kt
    │   │   │   ├── AlimentRepository.kt
    │   │   │   ├── RecetteRepository.kt
    │   │   │   ├── JournalRepository.kt
    │   │   │   ├── QuotaRepository.kt
    │   │   │   ├── PoidsRepository.kt
    │   │   │   ├── HydratationRepository.kt
    │   │   │   ├── PortionRepository.kt
    │   │   │   ├── RecommandationRepository.kt
    │   │   │   ├── NotificationRepository.kt
    │   │   │   └── ConsentRepository.kt
    │   │   │
    │   │   ├── remote/                     # Data sources distantes (API client)
    │   │   │   ├── ApiClient.kt            # Configuration Ktor Client
    │   │   │   ├── AuthApi.kt              # Endpoints auth
    │   │   │   ├── AlimentApi.kt           # Endpoints aliments
    │   │   │   ├── RecetteApi.kt           # Endpoints recettes
    │   │   │   ├── JournalApi.kt           # Endpoints journal
    │   │   │   ├── QuotaApi.kt             # Endpoints quotas
    │   │   │   ├── PoidsApi.kt             # Endpoints poids
    │   │   │   ├── HydratationApi.kt       # Endpoints hydratation
    │   │   │   ├── RecommandationApi.kt    # Endpoints recommandation
    │   │   │   ├── PortionApi.kt           # Endpoints portions
    │   │   │   ├── ConsentApi.kt           # Endpoints consentements
    │   │   │   └── NotificationApi.kt      # Endpoints notifications
    │   │   │
    │   │   ├── local/                      # Data sources locales (SQLDelight)
    │   │   │   ├── DatabaseDriverFactory.kt  # Factory platform-specific
    │   │   │   ├── LocalJournalDataSource.kt
    │   │   │   ├── LocalAlimentDataSource.kt
    │   │   │   ├── LocalRecetteDataSource.kt
    │   │   │   ├── LocalUserDataSource.kt
    │   │   │   ├── LocalPoidsDataSource.kt
    │   │   │   ├── LocalHydratationDataSource.kt
    │   │   │   ├── LocalQuotaDataSource.kt
    │   │   │   ├── LocalPortionDataSource.kt
    │   │   │   └── LocalSyncQueueDataSource.kt
    │   │   │
    │   │   └── impl/                       # Implementations des repositories
    │   │       ├── UserRepositoryImpl.kt    # Combine remote + local, gere sync
    │   │       ├── AlimentRepositoryImpl.kt
    │   │       ├── RecetteRepositoryImpl.kt
    │   │       ├── JournalRepositoryImpl.kt
    │   │       ├── QuotaRepositoryImpl.kt
    │   │       ├── PoidsRepositoryImpl.kt
    │   │       ├── HydratationRepositoryImpl.kt
    │   │       ├── PortionRepositoryImpl.kt
    │   │       ├── RecommandationRepositoryImpl.kt
    │   │       ├── NotificationRepositoryImpl.kt
    │   │       └── ConsentRepositoryImpl.kt
    │   │
    │   ├── sync/                           # Logique de synchronisation offline/online
    │   │   ├── SyncManager.kt              # Orchestrateur de sync (utilise SyncStatus de model/Enums.kt)
    │   │   ├── ConflictResolver.kt         # Resolution last-write-wins
    │   │   └── ConnectivityMonitor.kt      # Detection connexion (expect/actual)
    │   │
    │   ├── api/                            # Modeles API (request/response)
    │   │   ├── request/
    │   │   │   ├── AuthRequests.kt          # LoginRequest, RegisterRequest
    │   │   │   ├── ProfileRequests.kt       # CreateProfileRequest, UpdateProfileRequest, UpdatePreferencesRequest
    │   │   │   ├── JournalRequests.kt       # AddJournalEntryRequest, UpdateJournalEntryRequest, CopyJournalDayRequest (V1.1)
    │   │   │   ├── QuotaRequests.kt         # UpdateQuotaRequest
    │   │   │   ├── PoidsRequests.kt         # AddPoidsRequest
    │   │   │   ├── HydratationRequests.kt   # AddHydratationRequest, UpdateHydratationObjectifRequest
    │   │   │   ├── ConsentRequests.kt       # UpdateConsentRequest, InitialConsentRequest
    │   │   │   ├── PortionRequests.kt        # CreatePortionRequest, UpdatePortionRequest (portions personnalisees)
    │   │   │   ├── RecetteRequests.kt       # CreateRecetteRequest, UpdateRecetteRequest (admin uniquement — MVP)
    │   │   │   ├── SyncRequests.kt          # SyncPushRequest (entrees en attente)
    │   │   │   └── NotificationRequests.kt  # RegisterFcmTokenRequest
    │   │   │
    │   │   └── response/
    │   │       ├── AuthResponses.kt         # AuthResponse, UserResponse
    │   │       ├── UserResponses.kt         # UserProfileResponse, ProfileResponse, PreferencesResponse, UserExportResponse
    │   │       ├── AlimentResponses.kt      # AlimentResponse, SearchAlimentResponse
    │   │       ├── RecetteResponses.kt      # RecetteSummaryResponse, RecetteListResponse, RecetteDetailResponse
    │   │       ├── JournalResponses.kt      # JournalEntryResponse, JournalListResponse, DailySummaryResponse, WeeklySummaryResponse
    │   │       ├── QuotaResponses.kt        # QuotaResponse, QuotaStatusResponse
    │   │       ├── DashboardResponses.kt    # DashboardResponse, WeeklyDashboardResponse
    │   │       ├── RecommandationResponses.kt # RecommandationAlimentListResponse, RecommandationAlimentResponse, RecommandationRecetteListResponse, RecommandationRecetteResponse
    │   │       ├── PoidsResponses.kt        # PoidsListResponse, PoidsResponse, AddPoidsResponse
    │   │       ├── HydratationResponses.kt  # HydratationResponse, HydratationEntryResponse, HydratationWeeklyResponse, HydratationDaySummary
    │   │       ├── PortionResponses.kt      # PortionResponse
    │   │       ├── FaqResponses.kt          # FaqResponse
    │   │       ├── ConsentResponses.kt      # ConsentResponse
    │   │       ├── SyncResponses.kt         # SyncPullResponse, SyncPushResponse
    │   │       └── NotificationResponses.kt # NotificationResponse, NotificationListResponse
    │   │
    │   ├── ui/                             # UI Compose Multiplatform (partagee iOS + Android)
    │   │   ├── navigation/
    │   │   │   ├── AppNavigation.kt        # NavHost, routes
    │   │   │   ├── Screen.kt              # Sealed class des ecrans
    │   │   │   └── BottomNavBar.kt        # Barre de navigation inferieure
    │   │   │
    │   │   ├── auth/
    │   │   │   ├── LoginScreen.kt
    │   │   │   ├── RegisterScreen.kt
    │   │   │   ├── ForgotPasswordScreen.kt
    │   │   │   └── AuthViewModel.kt
    │   │   │
    │   │   ├── onboarding/
    │   │   │   ├── OnboardingScreen.kt     # Questionnaire profil (multi-step)
    │   │   │   ├── ConsentScreen.kt        # CGU + consentement
    │   │   │   ├── DisclaimerScreen.kt     # Disclaimer medical
    │   │   │   └── OnboardingViewModel.kt
    │   │   │
    │   │   ├── dashboard/
    │   │   │   ├── DashboardScreen.kt      # Ecran principal
    │   │   │   ├── WeeklyDashboardScreen.kt
    │   │   │   ├── HydratationWidget.kt    # Widget hydratation sur le dashboard
    │   │   │   └── DashboardViewModel.kt
    │   │   │
    │   │   ├── journal/
    │   │   │   ├── AddEntryScreen.kt       # Ecran de saisie
    │   │   │   ├── SearchAlimentScreen.kt  # Recherche + resultats
    │   │   │   ├── PortionSelector.kt      # Selecteur portion/grammes
    │   │   │   ├── FavoritesSection.kt     # Section favoris
    │   │   │   ├── RecentsSection.kt       # Section recents
    │   │   │   └── JournalViewModel.kt
    │   │   │
    │   │   ├── recommandation/
    │   │   │   ├── RecommandationsScreen.kt
    │   │   │   ├── RecommandationCard.kt
    │   │   │   └── RecommandationViewModel.kt
    │   │   │
    │   │   ├── recettes/
    │   │   │   ├── RecettesListScreen.kt
    │   │   │   ├── RecetteDetailScreen.kt
    │   │   │   ├── RecetteCard.kt
    │   │   │   └── RecettesViewModel.kt
    │   │   │
    │   │   ├── poids/
    │   │   │   ├── PoidsScreen.kt          # Historique + graphique
    │   │   │   ├── AddPoidsDialog.kt
    │   │   │   └── PoidsViewModel.kt
    │   │   │
    │   │   ├── profil/
    │   │   │   ├── ProfilScreen.kt
    │   │   │   ├── EditProfilScreen.kt
    │   │   │   ├── PreferencesAlimentairesScreen.kt
    │   │   │   ├── QuotasScreen.kt         # Gestion des quotas
    │   │   │   └── ProfilViewModel.kt
    │   │   │
    │   │   ├── settings/
    │   │   │   ├── SettingsScreen.kt
    │   │   │   ├── NotificationSettingsScreen.kt
    │   │   │   ├── ConsentSettingsScreen.kt # Modification consentements
    │   │   │   ├── AboutScreen.kt          # A propos + contact
    │   │   │   ├── FaqScreen.kt
    │   │   │   ├── LegalScreen.kt          # CGU + politique confidentialite
    │   │   │   └── SettingsViewModel.kt
    │   │   │
    │   │   ├── common/                     # Composants reutilisables
    │   │   │   ├── NutrimentProgressBar.kt # Barre de progression nutriment
    │   │   │   ├── EmptyState.kt           # Etats vides generiques
    │   │   │   ├── LoadingSkeleton.kt      # Skeleton screens
    │   │   │   ├── ErrorMessage.kt         # Messages d'erreur
    │   │   │   ├── OfflineBanner.kt        # Bandeau mode offline
    │   │   │   └── SkipLink.kt             # Lien "Passer" discret (onboarding)
    │   │   │
    │   │   ├── Strings.kt                  # Constantes UI en francais (MVP) — migration i18n en V1.1
    │   │   │
    │   │   └── theme/
    │   │       ├── Theme.kt                # Theme Material 3 appFood
    │   │       ├── Color.kt
    │   │       ├── Typography.kt
    │   │       └── Shape.kt
    │   │
    │   ├── di/                             # Dependency injection (Koin)
    │   │   └── SharedModule.kt             # Module Koin shared
    │   │
    │   └── util/                           # Utilitaires partages
    │       ├── DateUtils.kt                # Helpers date/heure
    │       ├── NutrimentUtils.kt           # Conversions, arrondis nutriments
    │       └── Result.kt                   # Wrapper Result<T> pour les use cases
    │
    ├── commonTest/kotlin/com/appfood/shared/
    │   ├── domain/quota/
    │   │   └── CalculerQuotasUseCaseTest.kt
    │   ├── domain/recommandation/
    │   │   └── RecommandationAlimentUseCaseTest.kt
    │   └── ...
    │
    ├── androidMain/kotlin/com/appfood/shared/
    │   ├── data/local/
    │   │   └── DatabaseDriverFactory.android.kt   # actual SQLDelight driver Android
    │   └── sync/
    │       └── ConnectivityMonitor.android.kt     # actual ConnectivityMonitor Android
    │
    ├── iosMain/kotlin/com/appfood/shared/
    │   ├── data/local/
    │   │   └── DatabaseDriverFactory.ios.kt       # actual SQLDelight driver iOS
    │   └── sync/
    │       └── ConnectivityMonitor.ios.kt         # actual ConnectivityMonitor iOS
    │
    └── commonMain/sqldelight/com/appfood/shared/
        ├── AppDatabase.sq                          # Schema SQLDelight
        ├── JournalQueries.sq
        ├── AlimentQueries.sq
        ├── RecetteQueries.sq
        ├── UserQueries.sq
        ├── PoidsQueries.sq
        ├── HydratationQueries.sq
        ├── QuotaQueries.sq
        ├── PortionQueries.sq
        └── SyncQueueQueries.sq
```

### Regles du module shared

- **Aucune dependance Android ou iOS** dans `commonMain` — uniquement du Kotlin pur et des librairies KMP/CMP
- Les `expect`/`actual` sont reserves aux factory platform-specific (database driver, connectivity)
- Les **modeles** (`model/`) sont la source de verite — utilises par tous les modules
- Les **use cases** (`domain/`) sont des fonctions pures — pas d'effets de bord, testables unitairement
- Les **repositories** (`data/repository/`) sont des interfaces — les implementations sont dans `data/impl/`
- Les **modeles API** (`api/`) definissent les contrats request/response — utilises par le client ET le serveur

### Separation SHARED agent vs MOBILE agent dans `shared/`

Le module `shared/` est travaille par 2 agents differents :
- **SHARED agent** : `model/`, `domain/`, `data/`, `sync/`, `api/`, `di/`, `util/` — logique metier, donnees, sync
- **MOBILE agent** : `ui/` — ecrans, ViewModels, composants, theme, navigation

Cette separation est **stricte**. Le MOBILE agent ne touche jamais aux fichiers hors de `ui/`. Le SHARED agent ne touche jamais aux fichiers dans `ui/`.

### Regles de l'UI (shared/ui/)

- **Pattern MVVM** : 1 ViewModel par feature (pas par ecran)
- Les ViewModels appellent les **use cases** du module shared — jamais les repositories directement
- Les ecrans sont des **composables purs** — pas de logique metier dedans
- Les composants reutilisables vont dans `ui/common/`
- **Compose Multiplatform uniquement** : pas d'imports `android.*`, `androidx.activity.*`, ou `UIKit`
- Utiliser `org.jetbrains.compose.*` et non `androidx.compose.*`

---

## 3. Module `androidApp` (wrapper Android)

**IMPORTANT** : L'UI Compose est dans `shared/commonMain/ui/`, PAS dans `androidApp/`. Ce module est un wrapper minimal qui lance l'app Compose Multiplatform sur Android.

**Package racine** : `com.appfood.android`

```
androidApp/
├── build.gradle.kts
└── src/main/
    ├── kotlin/com/appfood/android/
    │   ├── AppFoodApplication.kt           # Application class (init Koin, Firebase)
    │   ├── MainActivity.kt                 # Single Activity, lance le ComposeApp shared
    │   └── di/
    │       └── AndroidModule.kt            # Module Koin platform-specific (Context, etc.)
    │
    ├── res/
    │   ├── values/
    │   │   ├── strings.xml                 # Chaines Android-specific (app_name, etc.)
    │   │   └── themes.xml
    │   └── ...
    │
    ├── google-services.json                # Firebase config (TODO-HUMAIN)
    └── AndroidManifest.xml
```

### Regles du module androidApp

- **Wrapper uniquement** — aucun ecran, aucun ViewModel, aucune logique metier
- Contient seulement : `Application` class, `MainActivity`, DI platform-specific, resources Android
- Toute l'UI est dans `shared/commonMain/ui/`
- Ne doit JAMAIS contenir d'imports `androidx.compose.*` — utiliser Compose Multiplatform depuis shared

---

## 4. Module `iosApp` (wrapper iOS)

**IMPORTANT** : Meme principe que `androidApp` — c'est un wrapper minimal.

```
iosApp/
├── iosApp.xcodeproj
└── iosApp/
    ├── iOSApp.swift                        # Point d'entree
    ├── ContentView.swift                   # Vue racine (lance le ComposeApp shared)
    ├── GoogleService-Info.plist            # Firebase config iOS (TODO-HUMAIN)
    └── Info.plist
```

### Regles du module iosApp

- **Wrapper uniquement** — lance le ComposeApp shared
- Aucun ecran SwiftUI custom — tout est en Compose Multiplatform dans shared
- Contient seulement : point d'entree, config Firebase, config iOS

### Note sur l'architecture Compose Multiplatform

Avec CMP, **100% de l'UI** est ecrite en Compose dans `shared/commonMain/ui/` et compilee pour les deux plateformes. Les modules `androidApp` et `iosApp` ne sont que des points d'entree. Cela signifie :
- **1 seul code UI** a maintenir
- Les agents MOBILE et SHARED travaillent principalement dans `shared/`
- Les agents ne doivent JAMAIS utiliser d'imports `android.*` ou `UIKit` dans le code UI partagé

---

## 5. Module `backend`

**Package racine** : `com.appfood.backend`

```
backend/
├── build.gradle.kts
├── Dockerfile
├── src/
│   ├── main/
│   │   ├── kotlin/com/appfood/backend/
│   │   │   ├── Application.kt              # Point d'entree Ktor
│   │   │   ├── Config.kt                   # Configuration HOCON
│   │   │   │
│   │   │   ├── plugins/                    # Configuration Ktor
│   │   │   │   ├── Serialization.kt        # kotlinx.serialization
│   │   │   │   ├── Authentication.kt       # Firebase JWT verification
│   │   │   │   ├── CORS.kt
│   │   │   │   ├── StatusPages.kt          # Gestion globale des erreurs
│   │   │   │   └── RateLimiting.kt
│   │   │   │
│   │   │   ├── routes/                     # Endpoints API (par feature)
│   │   │   │   ├── AuthRoutes.kt           # /api/auth/*
│   │   │   │   ├── UserRoutes.kt           # /api/users/*
│   │   │   │   ├── AlimentRoutes.kt        # /api/aliments/*
│   │   │   │   ├── RecetteRoutes.kt        # /api/recettes/*
│   │   │   │   ├── JournalRoutes.kt        # /api/journal/*
│   │   │   │   ├── QuotaRoutes.kt          # /api/quotas/*
│   │   │   │   ├── PoidsRoutes.kt          # /api/poids/*
│   │   │   │   ├── HydratationRoutes.kt    # /api/hydratation/*
│   │   │   │   ├── RecommandationRoutes.kt # /api/recommandations/*
│   │   │   │   ├── DashboardRoutes.kt      # /api/dashboard/* (endpoint agrege)
│   │   │   │   ├── PortionRoutes.kt        # /api/portions/*
│   │   │   │   ├── NotificationRoutes.kt   # /api/notifications/*
│   │   │   │   ├── ConsentRoutes.kt        # /api/consents/*
│   │   │   │   ├── SupportRoutes.kt        # /api/support/* (FAQ)
│   │   │   │   ├── SyncRoutes.kt           # /api/sync/* (synchronisation client/serveur)
│   │   │   │   └── HealthRoutes.kt         # /api/health
│   │   │   │
│   │   │   ├── database/                   # Couche base de donnees
│   │   │   │   ├── DatabaseFactory.kt      # Configuration Exposed + PostgreSQL
│   │   │   │   ├── tables/                 # Tables Exposed
│   │   │   │   │   ├── UsersTable.kt
│   │   │   │   │   ├── AlimentsTable.kt
│   │   │   │   │   ├── RecettesTable.kt
│   │   │   │   │   ├── IngredientsTable.kt
│   │   │   │   │   ├── JournalEntriesTable.kt
│   │   │   │   │   ├── QuotasTable.kt
│   │   │   │   │   ├── PoidsHistoryTable.kt
│   │   │   │   │   ├── HydratationTable.kt
│   │   │   │   │   ├── PortionsTable.kt
│   │   │   │   │   ├── NotificationsTable.kt
│   │   │   │   │   ├── FcmTokensTable.kt
│   │   │   │   │   ├── ConsentsTable.kt
│   │   │   │   │   └── FaqTable.kt
│   │   │   │   │
│   │   │   │   └── dao/                    # Data Access Objects
│   │   │   │       ├── UserDao.kt
│   │   │   │       ├── AlimentDao.kt
│   │   │   │       ├── RecetteDao.kt
│   │   │   │       ├── JournalDao.kt
│   │   │   │       ├── QuotaDao.kt
│   │   │   │       ├── PoidsDao.kt
│   │   │   │       ├── HydratationDao.kt
│   │   │   │       ├── PortionDao.kt
│   │   │   │       ├── ConsentDao.kt
│   │   │   │       ├── NotificationDao.kt
│   │   │   │       └── FaqDao.kt
│   │   │   │
│   │   │   ├── service/                    # Logique metier serveur
│   │   │   │   ├── AuthService.kt          # Verification Firebase tokens
│   │   │   │   ├── AlimentService.kt       # Recherche Meilisearch + DB
│   │   │   │   ├── RecetteService.kt       # CRUD recettes — creation/edition ADMIN ONLY au MVP (RECETTES-03)
│   │   │   │   ├── JournalService.kt
│   │   │   │   ├── QuotaService.kt
│   │   │   │   ├── RecommandationService.kt
│   │   │   │   ├── PoidsService.kt
│   │   │   │   ├── HydratationService.kt
│   │   │   │   ├── DashboardService.kt     # Agregation des donnees dashboard (appelle les autres services)
│   │   │   │   ├── NotificationService.kt  # Envoi via FCM
│   │   │   │   ├── ConsentService.kt       # CRUD consentements
│   │   │   │   ├── FaqService.kt           # Lecture FAQ
│   │   │   │   ├── PortionService.kt       # CRUD portions (standard + personnalisees)
│   │   │   │   └── SyncService.kt          # Gestion sync client/serveur
│   │   │   │
│   │   │   ├── search/                     # Integration Meilisearch
│   │   │   │   ├── MeilisearchClient.kt    # Client HTTP Meilisearch
│   │   │   │   ├── AlimentIndexer.kt       # Indexation aliments
│   │   │   │   └── SearchModels.kt         # Modeles de recherche
│   │   │   │
│   │   │   ├── external/                   # Clients APIs externes
│   │   │   │   ├── OpenFoodFactsClient.kt  # API Open Food Facts
│   │   │   │   ├── FirebaseAdmin.kt        # Firebase Admin SDK
│   │   │   │   └── CiqualImporter.kt       # Import CSV Ciqual
│   │   │   │
│   │   │   ├── security/                   # Securite
│   │   │   │   ├── Encryption.kt           # Chiffrement donnees sensibles
│   │   │   │   └── InputValidation.kt      # Validation des entrees
│   │   │   │
│   │   │   └── di/
│   │   │       └── BackendModule.kt        # Module Koin backend
│   │   │
│   │   └── resources/
│   │       ├── application.conf            # Configuration HOCON (dev)
│   │       ├── application-staging.conf
│   │       ├── application-prod.conf
│   │       └── db/migration/              # Migrations Flyway
│   │           ├── V001__initial_schema.sql
│   │           ├── V002__seed_ajr.sql
│   │           ├── V003__seed_portions.sql
│   │           └── V004__add_objectif_poids.sql  # V1.1 — ALTER TABLE user_profiles ADD objectif_poids
│   │
│   └── test/
│       └── kotlin/com/appfood/backend/
│           ├── routes/
│           │   ├── AuthRoutesTest.kt
│           │   ├── JournalRoutesTest.kt
│           │   └── ...
│           ├── service/
│           │   ├── QuotaServiceTest.kt
│           │   └── ...
│           └── TestUtils.kt               # Helpers de test (Testcontainers)
│
└── docker/
    └── meilisearch/
        └── config.json                    # Configuration Meilisearch (synonymes, filtres)
```

### Regles du module backend

- **Pattern** : Routes → Service → DAO (3 couches)
- Les **routes** gerent le HTTP (parsing, serialization, codes de retour) — pas de logique metier
- Les **services** contiennent la logique metier serveur
- Les **DAOs** gerent les requetes Exposed — pas de logique metier
- Les modeles API (`request/response`) sont definis dans le module **shared** (`shared/api/`) et reutilises ici
- Les tables Exposed sont specifiques au backend (pas dans shared)
- Chaque route est une extension function sur `Route`

---

## 6. Fichiers racine

```
appFood/
├── docker-compose.yml          # PostgreSQL + Meilisearch + Ktor (dev)
├── docker-compose.test.yml     # PostgreSQL + Meilisearch (tests integration)
├── settings.gradle.kts         # Inclut : shared, androidApp, iosApp, backend
├── build.gradle.kts            # Plugins root, versions
├── gradle.properties           # Proprietes Gradle (JVM args, KMP config)
├── buildSrc/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── Versions.kt         # Toutes les versions de dependances
│       └── Dependencies.kt    # Groupes de dependances
├── .github/
│   └── workflows/
│       ├── ci.yml              # Lint + tests + build sur PR
│       ├── deploy-staging.yml  # Deploy sur merge main
│       └── deploy-prod.yml    # Deploy prod (declenchement manuel)
├── .gitignore
├── CONVENTIONS.md
├── CLAUDE.md                  # Instructions pour Claude Code (pointers vers tous les docs)
└── docs/
    ├── phase1-vision-rapport.md
    ├── phase2-architecture-rapport.md
    ├── phase3-backlog-rapport.md
    ├── phase4-dispatch-plan-agents.md
    ├── project-structure.md         # Ce fichier
    ├── data-models.md
    ├── api-contracts.md
    ├── us-clarifications.md         # Specs detaillees : QUOTAS-01, RECO-01/02, DATA-01, SYNC-01/02, LEGAL-04
    ├── sprint-tracker.md
    ├── TODO-HUMAIN.md
    └── WAITING-REVIEW.md
```

---

## 7. Dependances principales (buildSrc/Versions.kt)

| Dependance | Usage | Module |
|------------|-------|--------|
| Kotlin | Langage | Tous |
| Compose Multiplatform | UI cross-platform | shared, androidApp, iosApp |
| Ktor (Server) | API REST | backend |
| Ktor (Client) | Client HTTP | shared |
| Exposed | ORM PostgreSQL | backend |
| SQLDelight | Base locale | shared |
| kotlinx.serialization | Serialization JSON | Tous |
| kotlinx.datetime | Dates cross-platform | shared |
| Koin | Dependency Injection | Tous |
| Firebase Auth | Authentification | shared, androidApp, iosApp |
| Firebase Cloud Messaging | Notifications push | androidApp, iosApp |
| Firebase Admin | Verification tokens serveur | backend |
| Flyway | Migrations DB | backend |
| Meilisearch | Client recherche | backend |
| Kotest | Tests unitaires | shared, backend |
| Testcontainers | Tests integration | backend |

---

## 8. Mapping Agent → Fichiers

Ce tableau indique quel agent est responsable de quels fichiers. **Un agent ne doit JAMAIS modifier les fichiers d'un autre agent** sauf s'il est explicitement instruite de le faire par le PROJECT-MASTER.

| Agent | Fichiers/Dossiers | Lecture seule |
|-------|-------------------|---------------|
| **SHARED** | `shared/` (model, domain, data, sync, api, di, util) | `docs/api-contracts.md`, `docs/data-models.md`, `docs/us-clarifications.md`, `CONVENTIONS.md` |
| **MOBILE** | `shared/ui/` (ecrans, ViewModels, composants, theme, navigation) | `shared/model/`, `shared/domain/`, `docs/api-contracts.md`, `CONVENTIONS.md` |
| **BACKEND** | `backend/` | `shared/model/`, `shared/api/`, `docs/api-contracts.md`, `docs/us-clarifications.md`, `CONVENTIONS.md` |
| **DATA** | `backend/src/main/kotlin/.../external/CiqualImporter.kt`, `backend/src/main/resources/db/migration/V002__seed_ajr.sql`, `backend/src/main/resources/db/migration/V003__seed_portions.sql`, `backend/docker/meilisearch/` | `shared/model/`, `backend/database/tables/`, `docs/us-clarifications.md` |
| **INFRA** | `.github/`, `docker-compose*.yml`, `Dockerfile`, `backend/src/main/resources/application*.conf` | Tout le reste |
| **REVIEW** | Aucun (lecture seule) | Tout |
| **PROJECT-MASTER** | `docs/sprint-tracker.md`, `docs/TODO-HUMAIN.md`, `docs/WAITING-REVIEW.md` | Tout |

---

*Ce document est la reference pour la structure du projet. Tout ecart doit etre justifie et valide par le PROJECT-MASTER.*
