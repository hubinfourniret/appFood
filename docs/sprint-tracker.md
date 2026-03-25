# Sprint Tracker — appFood

## Sprint en cours : Sprint 0 (Setup)

| US-ID | Titre | Agents | Statut | Review | Bloquant |
|-------|-------|--------|--------|--------|----------|
| SETUP-01a | KMP init (shared) | SHARED | Done | ✅ | - |
| SETUP-01b | UI init (navigation, theme) | MOBILE | Done | ✅ | SETUP-01a |
| SETUP-02 | Ktor init | BACKEND | Done | ✅ | - |
| SETUP-03 | PostgreSQL schema | BACKEND | Done | ✅ | SETUP-02 |
| SETUP-04 | Meilisearch config | BACKEND | Done | ✅ | SETUP-02 |
| SETUP-05 | SQLDelight config | SHARED | Done | ✅ | SETUP-01a |
| INFRA-01 | CI/CD GitHub Actions | INFRA | Todo | - | SETUP-01a, SETUP-02 |
| DATA-01a | Import Ciqual (prep CSV parser) | DATA | Todo | - | - |
| DATA-01b | Import Ciqual (insert DB + index) | DATA | Todo | - | SETUP-03, SETUP-04, DATA-01a |
| UX-01 | Navigation principale | MOBILE | Todo | - | SETUP-01b |

Statuts : Todo | In Progress | Review | Waiting User | Done | Blocked
