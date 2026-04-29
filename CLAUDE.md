# appFood — Instructions pour Claude Code

Application mobile de suivi nutritionnel personnalise pour vegans/vegetariens sportifs.
Stack : Kotlin Multiplatform + Compose Multiplatform (mobile) + Ktor (backend) + PostgreSQL + Meilisearch.

## Documents de reference

| Document | Contenu |
|----------|---------|
| `docs/workflow-claude-code.md` | **Organisation du travail** — roles, format taches, workflow (reference active) |
| `CONVENTIONS.md` | Conventions de code, architecture, patterns obligatoires |
| `docs/phase1-vision-rapport.md` | Vision produit, personas, modele economique |
| `docs/phase2-architecture-rapport.md` | Architecture technique, stack, infrastructure |
| `docs/phase3-backlog-rapport.md` | Backlog complet avec toutes les User Stories (reference produit) |
| `docs/project-structure.md` | Arborescence du projet, structure des modules |
| `docs/data-models.md` | Modeles de donnees (Kotlin, Exposed, SQLDelight) |
| `docs/api-contracts.md` | 50 endpoints API avec Request/Response DTOs |
| `docs/us-clarifications.md` | Specifications detaillees pour QUOTAS-01, RECO-01/02, DATA-01, SYNC-01/02 |
| `docs/sprint-tracker.md` | Suivi d'avancement + backlog actif |
| `docs/TODO-HUMAIN.md` | Actions humaines requises (cles API, comptes, contenu) |
| `docs/WAITING-REVIEW.md` | Taches en attente de validation superviseur |
| `docs/phase4-dispatch-plan-agents.md` | ~~Ancien plan agents~~ (historique, remplace par workflow-claude-code.md) |

## Organisation du travail

**Claude Code** est le developpeur principal (full-stack, toutes couches).
**Le superviseur** (utilisateur) coordonne, priorise et valide les resultats par test fonctionnel.

Voir `docs/workflow-claude-code.md` pour le detail complet.

### Agents actifs
- **PROJECT-MASTER** — Orchestrateur (point d'entree pour faire le bilan et planifier)
- **REVIEW** — Revue de code sur taches critiques uniquement (algo, crypto, sync)
- **DATA** — Import/transformation de donnees volumineuses
- **INFRA** — CI/CD, Docker, deploiement

### Conventions par couche (reference, pas dispatch)
- `.CLAUDE/commands/agent-backend.md` — Conventions backend Ktor
- `.CLAUDE/commands/agent-mobile.md` — Conventions UI Compose
- `.CLAUDE/commands/agent-shared.md` — Conventions logique metier KMP

## Infrastructure

| Service | URL |
|---------|-----|
| Backend (production) | `https://appfood-production.up.railway.app` |
| Health check | `GET /api/health` |
| PostgreSQL (interne) | `postgres.railway.internal:5432/railway` |
| Meilisearch (interne) | `http://meilisearch.railway.internal:7700` |
| Firebase projet | `foodapp-5ea23` |

Backend en mode `FIREBASE_MOCK=false` en prod (vrai Firebase Admin SDK configure).

## Regles cles

- **Architecture** : Clean Architecture (UseCase → Repository → DataSource) cote shared, Routes → Service → DAO cote backend
- **Erreurs** : `AppResult<T>` partout dans shared, jamais de throw. Exceptions metier dans backend (StatusPages les intercepte)
- **Serialization** : kotlinx.serialization uniquement, pas de Gson/Moshi/Jackson
- **DI** : Koin uniquement, pas de Dagger/Hilt
- **UI** : Compose Multiplatform, `androidx.compose.*` est le namespace officiel depuis CMP 1.6+
- **Offline-first** : SQLDelight local + sync_queue pour journal/poids/hydratation
- **Langue** : Code en anglais, UI/commentaires en francais
- **Tests** : Kotest (shared), Ktor test engine + Testcontainers (backend)
- **RTK** : Toujours prefixer les commandes Bash avec `rtk` pour reduire la consommation de tokens (ex: `rtk git status`, `rtk ./gradlew build`, `rtk docker ps`). Meme dans les chaines avec `&&`, chaque commande doit etre prefixee : `rtk git add . && rtk git commit -m "msg"`
