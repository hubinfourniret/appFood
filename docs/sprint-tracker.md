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
| UX-01 | Navigation principale | MOBILE | Done | ✅ (mineurs) | SETUP-01b |

## Sprint en cours : Sprint 1 (Auth & Profil + Data)

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| AUTH-01 | Inscription email/password | SHARED, BACKEND, MOBILE | In Progress | - | SETUP-01a, SETUP-02 |
| AUTH-02 | Connexion / Deconnexion | SHARED, BACKEND, MOBILE | In Progress | - | AUTH-01 |
| AUTH-03 | Connexion Google Sign-In | MOBILE | In Progress | - | AUTH-01 |
| AUTH-04 | Reset mot de passe | MOBILE | In Progress | - | AUTH-01 |
| AUTH-05 | Connexion Apple Sign-In | MOBILE | In Progress | - | AUTH-03 |
| PROFIL-01 | Questionnaire profil (onboarding) | SHARED, BACKEND, MOBILE | In Progress | - | AUTH-01, SETUP-05 |
| PROFIL-02 | Edition du profil | SHARED, BACKEND, MOBILE | In Progress | - | PROFIL-01 |
| PROFIL-03 | Preferences alimentaires | SHARED, BACKEND, MOBILE | In Progress | - | PROFIL-01, SETUP-04 |
| PROFIL-04 | Suppression de compte (RGPD) | BACKEND, MOBILE | In Progress | - | PROFIL-01 |
| DATA-01b | Import Ciqual (insert DB + index) | DATA | In Progress | - | SETUP-03, SETUP-04, DATA-01a |
| DATA-02 | Integration Open Food Facts | DATA | In Progress | - | SETUP-04, DATA-01 |
| DATA-03 | Tables AJR/ANC | SHARED, DATA | In Progress | - | SETUP-03 |
| INFRA-02 | Deploiement Railway | INFRA | Blocked | - | Action humaine: compte Railway |
| INFRA-03 | Monitoring Sentry/UptimeRobot | INFRA | Blocked | - | INFRA-02, action humaine |

Statuts : Todo | In Progress | Review | Waiting User | Done | Blocked
