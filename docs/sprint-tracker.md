# Sprint Tracker — appFood

## Sprint 0 (Setup) — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| SETUP-01a | KMP init (shared) | SHARED | Done | ✅ | - |
| SETUP-01b | UI init (navigation, theme) | MOBILE | Done | ✅ | SETUP-01a |
| SETUP-02 | Ktor init | BACKEND | Done | ✅ | - |
| SETUP-03 | PostgreSQL schema | BACKEND | Done | ✅ | SETUP-02 |
| SETUP-04 | Meilisearch config | BACKEND | Done | ✅ | SETUP-02 |
| SETUP-05 | SQLDelight config | SHARED | Done | ✅ | SETUP-01a |
| INFRA-01 | CI/CD GitHub Actions | INFRA | Done | ✅ | SETUP-01a, SETUP-02 |
| DATA-01a | Import Ciqual (prep CSV parser) | DATA | Done | ✅ | - |
| DATA-01b | Import Ciqual (insert DB + index) | DATA | Done | ✅ (corrections appliquees) | SETUP-03, SETUP-04, DATA-01a |
| UX-01 | Navigation principale | MOBILE | Done | ✅ (mineurs) | SETUP-01b |

## Sprint 1 (Auth & Profil + Data) — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| AUTH-01 | Inscription email/password | SHARED, BACKEND, MOBILE | Done | ✅ (corrections appliquees) | - |
| AUTH-02 | Connexion / Deconnexion | SHARED, BACKEND, MOBILE | Done | ✅ (corrections appliquees) | - |
| AUTH-03 | Connexion Google Sign-In | MOBILE | Done | ✅ (corrections appliquees) | - |
| AUTH-04 | Reset mot de passe | MOBILE | Done | ✅ (corrections appliquees) | - |
| AUTH-05 | Connexion Apple Sign-In | MOBILE | Done | ✅ (corrections appliquees) | - |
| PROFIL-01 | Questionnaire profil (onboarding) | SHARED, BACKEND, MOBILE | Done | ✅ (corrections appliquees) | - |
| PROFIL-02 | Edition du profil | SHARED, BACKEND, MOBILE | Done | ✅ (corrections appliquees) | - |
| PROFIL-03 | Preferences alimentaires | SHARED, BACKEND, MOBILE | Done | ✅ (corrections appliquees) | - |
| PROFIL-04 | Suppression de compte (RGPD) | BACKEND, MOBILE | Done | ✅ (corrections appliquees) | - |
| DATA-02 | Integration Open Food Facts | DATA | Done | ✅ (corrections appliquees) | - |
| DATA-03 | Tables AJR/ANC | SHARED, DATA | Done | ✅ | - |
| PORTIONS-01 | Portions standard (donnees) | DATA | Done | ✅ | - |
| INFRA-02 | Deploiement Railway | INFRA (utilisateur) | Done | ✅ | - |
| INFRA-03 | Monitoring Sentry/UptimeRobot | INFRA | Blocked | - | INFRA-02, action humaine |
| PROFIL-05 | Export donnees personnelles (RGPD) | BACKEND | Done | ✅ | PROFIL-01 |

### Notes Sprint 1
- Firebase non configure → mock mode utilise (FIREBASE_MOCK=true)
- Backend ne peut pas dependre de :shared directement (pas de JVM target) → DTOs dans backend/routes/dto/
- ~~3 erreurs de compilation mineures pre-existantes dans AppNavigation.kt et OnboardingScreen.kt~~ → corrigees (audit 2026-03-26)

### Audit Sprint 0-1 (2026-03-26)

Corrections appliquees suite a l'audit des 4 epics (INFRA, DATA, AUTH, PROFIL) :

**Backend :**
- Auth JWT corrige : backend genere un JWT HMAC256 custom apres verification Firebase token
- FirebaseAdmin : fail-fast en prod (plus de fallback mock silencieux)
- AlimentRoutes + AlimentService crees (GET /search, /barcode/{code}, /{id})
- PortionRoutes + PortionService crees (GET, POST, PUT, DELETE)
- SearchRoutes deprece → remplace par AlimentRoutes
- Meilisearch : sel/sucres ajoutes dans l'index, champs renommes en camelCase, filtres corriges

**Shared :**
- 10 modeles domain crees (Aliment, Portion, Quota, Journal, Poids, Hydratation, Recette, Notification, Consentement, Recommandation)
- 24 DTOs request/response crees
- @Contextual ajoute sur tous les champs Instant
- AppNavigation.kt : popUpTo(navController.graph.id) → popUpTo(0) corrige

**Infra :**
- Dockerfile corrige (settings-docker.gradle.kts pour build backend-only)
- .dockerignore cree
- docker-compose : version obsolete supprimee

**Constat principal :**
- Backend endpoints : CONFORMES pour toutes les US
- UI ecrans : COMPLETS visuellement
- Use cases shared : ABSENTS — tous les ViewModels sont stubbes (a connecter au Sprint 2)
- CiqualImporter : adapte pour Ciqual 2025 (separateur virgule, headers multi-lignes)

**Compilation :** :backend:compileKotlin ✅ | :shared:compileCommonMainKotlinMetadata ✅

## Sprint 2 (Core: Quotas + Journal + Dashboard) — TERMINE

### Batch 1 — Parallele — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| QUOTAS-01 | Calcul quotas personnalises (algo) | SHARED | Done | ✅ Valide utilisateur | PROFIL-01, DATA-03 |
| RECO-01 | Recommandation aliments (algo) | SHARED | Done | ✅ Valide utilisateur | PROFIL-03, DATA-01 |
| JOURNAL-01 | Saisie aliment (endpoints + ecrans) | BACKEND, MOBILE | Done | - | SETUP-04, DATA-01 |
| JOURNAL-03 | Aliments favoris (endpoints + ecrans) | BACKEND, MOBILE | Done | - | JOURNAL-01 |
| JOURNAL-04 | Repas recents (endpoints + ecrans) | BACKEND, MOBILE | Done | - | JOURNAL-01 |
| JOURNAL-06 | Modification/suppression (endpoints + ecrans) | BACKEND, MOBILE | Done | - | JOURNAL-01 |
| QUOTAS-01 | Calcul quotas (endpoints) | BACKEND | Done | - | - |
| QUOTAS-02 | Modification quotas (endpoints) | BACKEND | Done | - | QUOTAS-01 |
| PORTIONS-01 | Portions standard (endpoints + UI) | BACKEND, MOBILE | Done | - | - |

### Batch 2 — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| DASHBOARD-01 | Dashboard journalier | BACKEND, MOBILE | Done | ✅ (compilation OK) | QUOTAS-01, JOURNAL-01 |
| QUOTAS-02 | Gestion quotas (ecran) | MOBILE | Done | ✅ (compilation OK) | QUOTAS-01 |
| RECO-01 | Recommandations aliments (endpoints + ecrans) | BACKEND, MOBILE | Done | ✅ (compilation OK) | RECO-01 algo |

### Reporte (dependances Sprint 3)

| US-ID | Titre | Raison |
|-------|-------|--------|
| JOURNAL-02 | Saisie recette | Depend de RECETTES-01 (Sprint 3) |
| RECO-02 | Recommandation recettes | Depend de RECETTES-01 (Sprint 3) |
| RECO-03 | Validation rapide plat recommande | Depend de RECO-02 + JOURNAL-02 |
| DASHBOARD-02 | Vue hebdomadaire | Depend de DASHBOARD-01 (Batch 2) |

## Sprint 3 (Recettes + Poids + Hydratation + Sync) — TERMINE

### Batch 1 — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| RECETTES-01 | Consultation livre de recettes | BACKEND, MOBILE | Done | ✅ (compilation OK) | SETUP-03 |
| POIDS-01 | Saisie et historique du poids | BACKEND, MOBILE | Done | ✅ (compilation OK) | PROFIL-01 |
| HYDRA-01 | Saisie et suivi hydratation | BACKEND, MOBILE | Done | ✅ (compilation OK) | DASHBOARD-01 |
| DASHBOARD-02 | Vue hebdomadaire | MOBILE | Done | ✅ (compilation OK) | DASHBOARD-01 |
| SYNC-01 | Saisie offline des repas | SHARED | Done | ✅ Validé utilisateur | SETUP-05, JOURNAL-01 |
| SYNC-03 | Cache local aliments/recettes | SHARED | Done | ✅ (data sources existants) | SETUP-05 |
| POIDS-02 | Recalcul quotas apres poids (logique) | SHARED | Done | ✅ (compilation OK) | POIDS-01, QUOTAS-01 |

### Notes Sprint 3 Batch 1
- Tous les ViewModels utilisent des stubs (TODO: injection use cases via Koin)
- ConnectivityMonitor expect/actual crees (Android + iOS) avec stubs
- SyncManager, SyncApi, SyncResponses, SyncRequests implementes
- SYNC-01 est une US CRITIQUE — validee par l'utilisateur
- Deprecation warnings kotlinx.datetime.Instant → kotlin.time.Instant (non-bloquants)

### Batch 2 — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| RECETTES-02 | Detail d'une recette | BACKEND, MOBILE | Done | ✅ (compilation OK) | RECETTES-01 |
| RECETTES-03 | Ajout recette personnalisee | BACKEND, MOBILE | Done | ✅ (compilation OK) | RECETTES-01 |
| JOURNAL-02 | Saisie recette consommee | BACKEND, MOBILE | Done | ✅ (compilation OK) | RECETTES-01 |
| RECO-02 | Recommandation recettes | SHARED, BACKEND, MOBILE | Done | ✅ (compilation OK) | RECO-01, RECETTES-01 |
| SYNC-02 | Sync auto au retour en ligne | SHARED | Done | ✅ (compilation OK) | SYNC-01 |
| POIDS-02 | Recalcul quotas (ecran) | MOBILE | Done | ✅ (compilation OK) | POIDS-02 logic |

### Notes Sprint 3 Batch 2
- Corrige imports `kotlinx.datetime.Clock` → `kotlin.time.Clock` (Kotlin 2.3.0)
- Corrige `cubicTo` → `curveTo` dans les path builders Compose Multiplatform
- Corrige `String.format` → calcul manuel (non disponible en KMP common)
- Corrige types SQLDelight (String→Long pour timestamps, Long→Double pour hydratation)
- SyncManager : auto-sync via ConnectivityMonitor, exponential backoff, SyncPreferences
- RECO-02 : onglets Aliments/Recettes dans l'ecran recommandations
- JOURNAL-02 : toggle Aliment/Recette dans AddEntryScreen
- POIDS-02 : dialog recalcul quotas apres changement de poids significatif
- ViewModels avec stubs TODO (meme pattern que Batch 1)

### Batch 3 — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| RECO-03 | Validation rapide plat recommande | MOBILE | Done | ✅ (compilation OK) | RECO-02, JOURNAL-02 |

### Notes Sprint 3 Batch 3
- RecetteRecommandationCard : selecteur de portions (+/-) + bouton "J'ai mange ca"
- ViewModel : Map<recetteId, portions> pour gerer les portions par recette
- Feedback succes via successMessage StateFlow
- Stub TODO pour ajout effectif au journal (use case a connecter)

## Audit post-Sprint 3 (2026-04-08)

### Corrections P0 appliquees (bugs critiques)

| Correction | Fichiers | Impact |
|-----------|---------|--------|
| AuthResponse : champ `token` manquant cote shared | AuthResponses.kt, UserRepositoryImpl.kt | Auth cassee — client ne recuperait pas le JWT |
| ApiClient non enregistre dans Koin | SharedModule.kt, AppFoodApplication.kt, androidApp/build.gradle.kts | Crash runtime NoBeanDefFoundException |
| SyncManager userId = "" apres pull | SyncManager.kt, SharedModule.kt | Corruption donnees — entries sans userId |
| Import `toRoute` manquant | AppNavigation.kt | Erreur compilation pre-existante |
| RecommandationAlimentResponse : shared attendait AlimentResponse, backend envoyait AlimentSummaryResponse | RecommandationResponses.kt, RecommandationRepositoryImpl.kt | Crash deserialisation |
| RecommandationRecetteResponse : shared attendait objet nested, backend envoyait champs plats | RecommandationResponses.kt, RecommandationRepositoryImpl.kt | Crash deserialisation |
| DashboardResponse.hydratation : type mismatch (HydratationResponse vs HydratationDashboardResponse) | DashboardResponses.kt | Crash deserialisation |
| QuotaResponse : updatedAt + total manquants cote shared | QuotaResponses.kt | Champs silencieusement ignores |
| Logout ne nettoyait pas le token | UserRepository.kt, UserRepositoryImpl.kt | Token restait actif apres deconnexion |

### Problemes connus restants (non-bloquants MVP)

- Module Koin iOS absent (app iOS non testable sans Mac)
- HttpClient sans timeout configuration
- Token non persiste en local (perdu au redemarrage, login obligatoire)
- 56 ViewModels stubs TODO (use cases a connecter au Sprint 4)
- Duplication calcul quotas entre UseCase et backend Service
- Enums dupliques backend/shared (11 enums)

**Compilation** : `:shared:compileCommonMainKotlinMetadata` ✅ | `:backend:compileKotlin` ✅

## Sprint 4 (Legal + Support + Qualite + UX) — EN COURS

### Batch 1 — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| UX-02 | Etats vides | MOBILE | Done | - | UX-01 |
| UX-03 | Etats de chargement | MOBILE | Done | - | UX-01 |
| UX-04 | Gestion erreurs utilisateur | MOBILE | Done | - | UX-01 |
| UX-05 | Disclaimer legal | MOBILE | Done | - | AUTH-01 |
| LEGAL-01 | Politique de confidentialite | MOBILE | Done | ⚠️ Waiting User | - |
| LEGAL-02 | CGU | MOBILE | Done | ⚠️ Waiting User | - |
| LEGAL-03 | Gestion consentement | MOBILE | Done | - | AUTH-01 |
| SUPPORT-01 | Page A propos | MOBILE | Done | - | UX-01 |
| NOTIF-01 | Config FCM (endpoints) | BACKEND | Done | - | SETUP-02 |
| LEGAL-04 | Chiffrement donnees sensibles | BACKEND | Done | ⚠️ Waiting User | SETUP-03 |
| QUALITE-01 | Tests unitaires logique metier | SHARED | Done | - | QUOTAS-01, RECO-01 |
| QUALITE-02 | Tests integration API | BACKEND | Done | - | INFRA-01 |
| Consents endpoints | Endpoints consentements + FAQ | BACKEND | Done | - | - |

### Notes Sprint 4 Batch 1
- LEGAL-01/02 : contenu placeholder, a remplacer par contenu juriste
- LEGAL-04 : AES-256-GCM, cle via ENCRYPTION_KEY env var, mode clair en dev
- QUALITE-01 : 68 tests (CalculerQuotas 27, RecoAliment 20, RecoRecette 21)
- QUALITE-02 : 32 tests integration (Auth 10, Journal 9, Quota 7, Dashboard 6)
- Compilation corrigee : imports Icons Material → texte simple (Compose Multiplatform)
- Navigation : Login → Consent → Onboarding → Disclaimer → Dashboard

### Batch 2 — TERMINE

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| SUPPORT-02 | FAQ integree | BACKEND + MOBILE | Done | - | SUPPORT-01 |

### Notes Sprint 4 Batch 2
- FaqScreen avec 5 themes, items expandables, contenu placeholder statique
- Lien "Contacter le support" → mailto:support@appfood.fr
- Cable depuis AboutScreen

**Compilation** : `:shared` ✅ | `:backend` ✅

## Cablage ViewModels (post-Sprint 4, 2026-04-08)

Tous les ViewModels ont ete cables aux repositories/use cases. Aucun stub TODO restant.

| Feature | ViewModel | Cable a | Statut |
|---------|-----------|---------|--------|
| Auth | AuthViewModel | UserRepository (login, register, logout, delete) | ✅ Done |
| Auth | AuthViewModel | Firebase SDK (resetPassword) | ✅ Done |
| Onboarding | OnboardingViewModel | UserRepository (createProfile, updatePreferences) | ✅ Done |
| Profil | ProfilViewModel | UserRepository (getProfile, updateProfile, updatePreferences, export) | ✅ Done |
| Quotas | QuotaViewModel | QuotaRepository (load, update, reset, resetAll) | ✅ Done |
| Dashboard | DashboardViewModel | DashboardRepository (getDaily) | ✅ Done |
| Dashboard | WeeklyDashboardViewModel | DashboardRepository (getWeekly) | ✅ Done |
| Hydratation | HydratationViewModel | UseCases (ajouter, getJour, updateObjectif) + Repository (weekly) | ✅ Done |
| Poids | PoidsViewModel | UseCases (enregistrer, historique, detecter, recalculer) | ✅ Done |
| Journal | JournalViewModel | JournalRepository + AlimentRepository + RecetteRepository | ✅ Done |
| Recettes | RecettesViewModel | RecetteRepository (search, detail) | ✅ Done |
| Recommandations | RecommandationViewModel | RecommandationRepository (aliments, recettes) | ✅ Done |
| Consent | ConsentViewModel | ConsentApi (initial, get, update) | ✅ Done |
| Disclaimer | DisclaimerViewModel | Local-only (in-memory) | ✅ Done |
| FAQ | FaqViewModel | SupportApi (getFaq) + fallback statique | ✅ Done |

### Couche data creee
- DashboardApi + DashboardRepository + impl
- JournalApi + JournalRepository + impl
- AlimentApi + AlimentRepository + impl
- PortionApi
- ConsentApi
- SupportApi

## Criteres manquants identifies (en cours de correction)

| US | Critere manquant | Statut |
|----|-----------------|--------|
| JOURNAL-01 | Resume nutritionnel avant validation | ✅ Done |
| JOURNAL-01 | Offline-first enqueue sync | ✅ Done |
| JOURNAL-03 | Favoris persistes cote serveur | ✅ Done (3 endpoints backend + cablage) |
| JOURNAL-06 | Confirmation avant suppression | ✅ Done (AlertDialog + JournalScreen) |
| RECO-03 | Ajout au journal non stubbe | ✅ Done (aliments + recettes) |
| RECETTES-03 | Creation recette non stubbee | ✅ Done (RecetteApi.create + ViewModel) |

## US non-MVP (reportees V1.1)

| US | Titre | Raison |
|----|-------|--------|
| JOURNAL-05 | Copie repas/journee | V1.1 |
| JOURNAL-07 | Scan code-barres | V1.1 |
| NOTIF-02/03/04 | Notifications push (bilan, hebdo, preferences) | V1.1 |
| AUTH-03 | Google Sign-In | Reporte |
| AUTH-05 | Apple Sign-In | Action humaine requise |
| INFRA-03 | Monitoring Sentry/UptimeRobot | Action humaine en cours |

## US en attente validation utilisateur

- LEGAL-01 — Politique de confidentialite (contenu placeholder)
- LEGAL-02 — CGU (contenu placeholder)
- LEGAL-04 — Chiffrement AES-256-GCM (ENCRYPTION_KEY configuree sur Railway)

## Incidents production

### INCIDENT-2026-04-10 — Meilisearch OOM + ajout aliment + dashboard timeout

**Symptomes** : Meilisearch crash OOM sur Railway. Ajout d'aliment mobile bloque en "Valider → loader infini". Dashboard renvoie `Socket timeout has expired [..., socket_timeout=unknown] ms`.

**Causes racines** :
1. Meilisearch sans limite d'indexing memory sur Railway (OOM au demarrage)
2. HttpClient Android sans `HttpTimeout` installe → requetes infinies
3. `DashboardService.getDashboard` n'avait pas de timeout sur les appels `RecommandationService` (lents en cold start)
4. `RecommandationService` charge 2000 aliments en memoire a chaque cache miss (vrai bottleneck, pas Meilisearch)
5. Tests E2E aveugles : client Ktor in-memory, pas de simulation de latence, reco en try/catch silencieux

**Fixes appliques** :
- Railway : `MEILI_MAX_INDEXING_MEMORY=256Mb`, `MEILI_MAX_INDEXING_THREADS=1`, `MEILI_LOG_LEVEL=WARN`
- `androidApp/.../AppFoodApplication.kt` : `HttpTimeout` (req=30s/conn=15s/sock=30s)
- `shared/.../JournalViewModel.kt` : log `result.code + message` dans onValidateEntry
- `backend/di/BackendModule.kt` : `HttpTimeout` HttpClient interne (req=10s/conn=5s/sock=10s)
- `backend/service/DashboardService.kt` : logs perf par etape + `withTimeoutOrNull(5_000)` sur reco avec degradation gracieuse
- `backend/test/.../JournalPerformanceE2ETest.kt` : 3 nouveaux E2E

**Suivi** : US RECO-PERF-01 a creer au backlog pour optimiser `RecommandationService` (SQL pre-filter + reduction du pool candidat).

## Sprint 5 (Audit UX + Stabilisation) — EN COURS

### Corrections appliquees (2026-04-17)

| Correction | Fichiers | Statut |
|-----------|---------|--------|
| BUG #3 : Recherche aliments exclus sans feedback | ProfilViewModel, PreferencesAlimentairesScreen, Strings | Done |
| BUG #4 : SettingsScreen placeholder vide | SettingsScreen.kt (nouveau), AppNavigation, Strings | Done |
| BUG #5 : Reessayer ne reset pas le formulaire login | LoginScreen.kt | Done |
| BUG #6 : EditProfil/Preferences sans bouton Retour | EditProfilScreen, PreferencesAlimentairesScreen, AppNavigation | Done |
| BUG #7 : Token JWT non persiste (perdu au restart) | AppDatabase.sq, UserQueries.sq, LocalUserDataSource, UserRepositoryImpl, ApiClient, SharedModule, AppNavigation | Done |

### US a faire — Sprint 5

| US-ID | Titre | Description | Agents | Statut | Priorite |
|-------|-------|-------------|--------|--------|----------|
| UX-06 | Ajout recette depuis AddEntry | L'ecran "Ajouter un aliment" a les onglets Aliment/Recette mais l'onglet Recette ne permet pas de chercher et ajouter une recette au journal. Le flow doit etre : tap Recette → recherche → selection → choix portions → valider → retour dashboard. Actuellement seul l'onglet Aliment fonctionne. | MOBILE, SHARED | Todo | Haute |
| UX-07 | Portions adaptees par categorie (fruits, etc.) | Implemente : CategoryPortions.kt avec mapping categorie → portions suggerees (13 categories, ~50 portions). Affiche dans PortionSelectorScreen entre les portions specifiques et generiques. Pur client, pas de backend. | SHARED, MOBILE | Done | Haute |
| DASH-BUG-01 | Dashboard affiche 0/0 kcal ou reste vide | Corrige : auto-init quotas a la creation profil (UserRoutes) + auto-recalcul si vides au chargement dashboard (DashboardService). | BACKEND | Done | Critique |
| JOURNAL-BUG-01 | Ajout aliment echoue "Aliment non trouve" | Meilisearch retourne des UUIDs qui n'existent pas/plus dans PostgreSQL. Le reimport ne suffit pas. A investiguer : (1) verifier la coherence Meilisearch vs PostgreSQL en prod, (2) ajouter un endpoint de diagnostic /api/v1/admin/data-consistency, (3) si l'aliment n'existe pas en DB, le re-fetcher depuis Meilisearch ou Open Food Facts. | BACKEND, DATA | Todo | Critique |
| RECO-PERF-01 | Optimiser RecommandationService | RecommandationService charge 2000 aliments en memoire a chaque cache miss. Implementer un pre-filtre SQL + reduire le pool candidat pour eviter les timeouts en cold start. | BACKEND | Todo | Moyenne |

### Notes Sprint 5
- Token JWT maintenant persiste en SQLDelight (table local_auth_token) et restaure au demarrage
- L'app navigue directement vers Dashboard si un token valide est en cache (skip login)
- 19 tests Maestro couvrent auth, navigation, profil, recettes, ajout aliment, performance, edge cases
- FIREBASE_MOCK=false en prod, le vrai Firebase Admin SDK est utilise
- Compilation : `:shared` OK | `:backend` OK

Statuts : Todo | In Progress | Review | Waiting User | Done | Blocked
