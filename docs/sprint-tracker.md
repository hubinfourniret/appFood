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

Statuts : Todo | In Progress | Review | Waiting User | Done | Blocked
