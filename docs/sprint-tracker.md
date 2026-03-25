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

### Notes Sprint 1
- Firebase non configure → mock mode utilise (FIREBASE_MOCK=true)
- Backend ne peut pas dependre de :shared directement (pas de JVM target) → DTOs dans backend/routes/dto/
- 3 erreurs de compilation mineures pre-existantes dans AppNavigation.kt et OnboardingScreen.kt (a corriger)

Statuts : Todo | In Progress | Review | Waiting User | Done | Blocked
