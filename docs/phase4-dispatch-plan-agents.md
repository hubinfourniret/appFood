# appFood — Phase 4 : Plan d'action des agents

> Date : 2026-03-23
> Version : 1.0 (draft pour validation)

---

## 1. Architecture des agents

### Vue d'ensemble

```
                    ┌─────────────────────────────┐
                    │      PROJECT-MASTER          │
                    │  (Orchestrateur principal)   │
                    │                              │
                    │  - Dispatche les US          │
                    │  - Verifie la coherence      │
                    │  - Detecte les duplications  │
                    │  - Valide l'integration      │
                    └──────────┬──────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                     │
          ▼                    ▼                     ▼
   ┌──────────────┐   ┌──────────────┐    ┌──────────────┐
   │  BACKEND     │   │  MOBILE      │    │  DATA        │
   │  AGENT       │   │  AGENT       │    │  AGENT       │
   │              │   │              │    │              │
   │ Ktor API     │   │ KMP/Compose  │    │ Import       │
   │ PostgreSQL   │   │ UI           │    │ Ciqual/OFF   │
   │ Modules      │   │ Navigation   │    │ Meilisearch  │
   │ Endpoints    │   │ Ecrans       │    │ Indexation   │
   └──────────────┘   └──────────────┘    └──────────────┘
          │                    │                     │
          │                    │                     │
          ▼                    ▼                     ▼
   ┌──────────────┐   ┌──────────────┐    ┌──────────────┐
   │  INFRA       │   │  SHARED      │    │  REVIEW      │
   │  AGENT       │   │  AGENT       │    │  AGENT       │
   │              │   │              │    │              │
   │ CI/CD        │   │ Logique      │    │ Review code  │
   │ Docker       │   │ metier       │    │ sur chaque   │
   │ Railway      │   │ partagee     │    │ US terminee  │
   │ Monitoring   │   │ (KMP shared) │    │              │
   └──────────────┘   └──────────────┘    └──────────────┘
```

---

## 2. Description detaillee de chaque agent

---

### AGENT 1 : PROJECT-MASTER (Orchestrateur)

**Role** : Chef de projet technique. Ne code pas directement. Dispatche, verifie, integre.

**Responsabilites :**
- Lire le backlog et dispatcher les US aux bons agents
- Verifier apres chaque US que le code est coherent avec l'existant
- Detecter les duplications de code (memes modeles definis 2 fois, logique dupliquee)
- Verifier que les interfaces entre modules sont respectees (contrats API, modeles partages)
- Gerer l'ordre d'execution (respecter les dependances)
- Lancer les agents en parallele quand c'est possible
- Valider l'integration finale (tout compile, tout fonctionne ensemble)

**Quand il intervient :**
- Au debut de chaque sprint/batch de US
- Apres chaque US terminee (avant merge)
- En fin de sprint pour la verification globale

**Skill** : `/project-master`

---

### AGENT 2 : BACKEND

**Role** : Developpe tout le backend Ktor.

**Perimetre :**
- Configuration Ktor (routing, serialization, auth, CORS)
- Endpoints API REST
- ORM Exposed (entites, DAOs, migrations)
- Integration Meilisearch cote serveur
- Logique serveur (validation, calculs cote API, sync)
- Docker / Dockerfile

**Stories assignees (MVP) :**
- SETUP-02 (Ktor init)
- SETUP-03 (PostgreSQL schema)
- SETUP-04 (Meilisearch config)
- AUTH-01 a AUTH-05 (partie serveur : verification tokens, endpoints profil)
- PROFIL-01 a PROFIL-04 (endpoints API)
- QUOTAS-01, QUOTAS-02 (endpoints)
- JOURNAL-01 a JOURNAL-04, JOURNAL-06 (endpoints API — JOURNAL-05 = V1.1)
- DASHBOARD-01, DASHBOARD-02 (endpoints d'agregation)
- RECO-01, RECO-02 (endpoints recommandation)
- RECETTES-01 a RECETTES-03 (endpoints + admin)
- POIDS-01, POIDS-02 (endpoints)
- HYDRA-01 (endpoints)
- PORTIONS-01 (endpoints + donnees de reference)
- NOTIF-01 (endpoint envoi notifications)
- SYNC-02 (endpoints de sync)
- LEGAL-04 (chiffrement en base)
- SUPPORT-02 (endpoint FAQ)

**Skill** : `/agent-backend`

---

### AGENT 3 : MOBILE

**Role** : Developpe l'UI mobile KMP + Compose Multiplatform.

**Perimetre :**
- Ecrans Compose (UI)
- Navigation
- State management (ViewModels)
- Integration avec la couche shared (appels repository)
- Gestion des permissions (camera, notifications)
- Theming Material Design 3

**Stories assignees (MVP) :**
- SETUP-01 (KMP init — en collab avec SHARED)
- AUTH-01 a AUTH-05 (ecrans login/inscription, flow Firebase)
- PROFIL-01 a PROFIL-03 (ecrans profil, questionnaire onboarding)
- PROFIL-04 (ecran suppression compte)
- QUOTAS-02 (ecran gestion quotas)
- JOURNAL-01 a JOURNAL-04, JOURNAL-06 (ecrans saisie, favoris, recents)
- DASHBOARD-01, DASHBOARD-02 (ecrans dashboard)
- RECO-01 a RECO-03 (ecrans recommandations)
- RECETTES-01, RECETTES-02 (ecrans livre de recettes)
- POIDS-01 (ecran suivi poids, graphique)
- HYDRA-01 (widget hydratation)
- PORTIONS-01 (UI selection de portions)
- UX-01 a UX-05 (navigation, etats vides/chargement/erreur, disclaimer)
- LEGAL-01 a LEGAL-03 (ecrans CGU, confidentialite, consentement)
- SUPPORT-01, SUPPORT-02 (ecrans A propos, FAQ)

**Skill** : `/agent-mobile`

---

### AGENT 4 : SHARED (Logique metier partagee)

**Role** : Developpe le module `shared` KMP — la logique metier partagee entre Android et iOS.

**Perimetre :**
- Modeles de donnees (data classes Kotlin)
- Repository pattern (interface vers les data sources)
- Logique metier pure (calcul des quotas, algorithme de recommandation, scoring)
- Client API (Ktor Client pour appeler le backend)
- SQLDelight (schema local, DAOs, queries)
- Logique de synchronisation offline/online

**Stories assignees (MVP) :**
- SETUP-01 (structure KMP shared — en collab avec MOBILE)
- SETUP-05 (SQLDelight config + schema local)
- QUOTAS-01 (algorithme de calcul des quotas — logique pure)
- RECO-01 (algorithme de recommandation — logique pure)
- POIDS-02 (logique de recalcul des quotas)
- HYDRA-01 (calcul objectif hydratation)
- SYNC-01, SYNC-02, SYNC-03 (logique de sync, cache, resolution de conflits)
- DATA-03 (tables AJR/ANC — modeles + donnees de reference)

**Skill** : `/agent-shared`

---

### AGENT 5 : DATA

**Role** : Gere l'import, la transformation et l'indexation des donnees nutritionnelles.

**Perimetre :**
- Scripts d'import Ciqual (CSV → PostgreSQL)
- Integration API Open Food Facts
- Indexation Meilisearch (configuration, synonymes, filtres)
- Mapping des nutriments
- Donnees de reference (AJR/ANC, portions standard)
- Qualite des donnees (validation, nettoyage)

**Stories assignees (MVP) :**
- DATA-01 (import Ciqual)
- DATA-02 (integration Open Food Facts)
- DATA-03 (tables AJR/ANC — partie donnees)
- PORTIONS-01 (donnees de portions standard)

**Skill** : `/agent-data`

---

### AGENT 6 : INFRA

**Role** : Met en place l'infrastructure, le CI/CD et le monitoring.

**Perimetre :**
- Docker Compose (dev local)
- Dockerfile backend
- GitHub Actions (CI/CD)
- Configuration Railway (staging, prod)
- Monitoring (Sentry, UptimeRobot)
- Variables d'environnement et secrets

**Stories assignees (MVP) :**
- INFRA-01 (pipeline CI/CD)
- INFRA-02 (deploiement Railway)
- INFRA-03 (monitoring)

**Skill** : `/agent-infra`

---

### AGENT 7 : REVIEW

**Role** : Revue de code poussee sur chaque US terminee. Ne code pas, ne merge pas.

**Responsabilites :**
- Lire le diff de chaque US terminee
- Verifier la qualite du code (lisibilite, conventions, nommage)
- Verifier la securite (pas de secrets en dur, injections, validation des entrees)
- Verifier la coherence avec l'architecture definie (Phase 2)
- Verifier les criteres d'acceptation de la US
- Verifier qu'il n'y a pas de duplication avec le code existant
- Verifier que les tests sont presents et pertinents
- Laisser un rapport de review structure

**Format du rapport de review :**
```
## Review : [US-ID] — [Titre]

### Statut : APPROVE / CHANGES_REQUESTED / BLOQUANT

### Checklist
- [ ] Criteres d'acceptation respectes
- [ ] Code lisible et bien structure
- [ ] Pas de duplication
- [ ] Securite OK (pas de secrets, validation inputs)
- [ ] Coherent avec l'architecture (Phase 2)
- [ ] Tests presents et pertinents
- [ ] Pas de regression sur l'existant

### Commentaires
[Details des points a corriger ou ameliorer]

### Verdict
[Resume en une phrase]
```

**Quand il intervient :**
- Apres chaque US terminee par un agent dev
- Avant que le PROJECT-MASTER ne valide l'integration

**Skill** : `/agent-review`

---

## 3. Parallelisme — Quoi peut tourner en parallele

### Sprint -1 — Fondations (fait par nous, pas par les agents)

**Objectif** : Creer les fichiers de reference que TOUS les agents utiliseront. Sans ca, chaque agent invente ses propres conventions et ca diverge.

**Livrables :**

1. **`CONVENTIONS.md`** — Regles pour tous les agents :
   - Structure de packages (`com.appfood.feature.auth`, etc.)
   - Patterns a suivre (MVVM, Repository, UseCase)
   - Conventions de nommage (classes, fonctions, endpoints, tables)
   - Regles d'import, de formatting (ktlint config)

2. **`docs/api-contracts.md`** — Contrats API complets :
   - Chaque endpoint : URL, methode HTTP, headers, request body, response body, codes d'erreur
   - Modeles de request/response en Kotlin (data classes)
   - Format d'authentification (header Bearer token)
   - Format d'erreur standard

3. **`docs/data-models.md`** — Modeles de donnees source de verite :
   - Data classes Kotlin pour chaque entite (User, Aliment, Recette, etc.)
   - Schema PostgreSQL (tables Exposed)
   - Schema SQLDelight (tables locales)
   - Mapping entre les 3 representations

4. **`docs/project-structure.md`** — Arborescence cible du projet :
   - Structure des modules KMP (shared, androidApp, iosApp, backend)
   - Structure interne de chaque module (packages par feature)
   - Ou chaque agent doit ecrire ses fichiers

**Pourquoi c'est indispensable** : Les US actuelles sont ecrites pour des humains. Les agents ont besoin de savoir exactement quels fichiers creer, quelles signatures utiliser, quels patterns suivre. Ces fichiers de fondation transforment les US en instructions precises.

---

### Sprint 0 — Setup (tout en parallele)

```
En parallele :
├── INFRA-AGENT  → INFRA-01 (CI/CD GitHub Actions)
├── BACKEND      → SETUP-02 (Ktor init) + SETUP-03 (PostgreSQL)
├── SHARED       → SETUP-01a (KMP init : structure modules, build.gradle.kts, di/, util/, DatabaseDriverFactory)
├── MOBILE       → SETUP-01b (UI init : ui/navigation/, ui/theme/, Strings.kt, wrappers Android/iOS) — apres SETUP-01a
├── SHARED       → SETUP-05 (SQLDelight)
├── DATA         → DATA-01 (import Ciqual — preparation scripts)
└── MOBILE       → UX-01 (navigation principale — structure ecrans) — apres SETUP-01b

Sequentiel apres :
BACKEND (SETUP-04 Meilisearch) → depend de SETUP-02
DATA (indexation Meilisearch) → depend de SETUP-04 + DATA-01
```

### Sprint 1 — Auth & Profil + Data

```
En parallele :
├── BACKEND      → AUTH-01 a AUTH-05 endpoints + PROFIL-01 a PROFIL-04 endpoints
├── MOBILE       → AUTH-01 a AUTH-05 ecrans + PROFIL-01 a PROFIL-04 ecrans (PROFIL-04 = ecran suppression)
├── BACKEND      → PROFIL-04 endpoint (DELETE /auth/account) en parallele avec MOBILE
├── SHARED       → DATA-03 (modeles AJR/ANC) + modeles de donnees auth/profil
├── DATA         → DATA-01 (finalisation import) + DATA-02 (Open Food Facts)
└── INFRA        → INFRA-02 (deploy Railway) + INFRA-03 (monitoring)

REVIEW intervient apres chaque US terminee.
```

### Sprint 2 — Core (Quotas + Journal + Dashboard)

```
En parallele :
├── SHARED       → QUOTAS-01 (algorithme calcul quotas) + RECO-01 (algo recommandation)
├── BACKEND      → QUOTAS-01/02 endpoints + JOURNAL-01 a 04, JOURNAL-06 endpoints (JOURNAL-05 = V1.1)
├── MOBILE       → JOURNAL-01 a 04 ecrans + PORTIONS-01 UI
├── DATA         → PORTIONS-01 (donnees portions standard)

Sequentiel :
SHARED (QUOTAS-01) → puis MOBILE (DASHBOARD-01) + BACKEND (DASHBOARD-01 endpoint)
SHARED (RECO-01)   → puis MOBILE (RECO-01 a 03 ecrans) + BACKEND (RECO-01/02 endpoints)
```

### Sprint 3 — Recettes + Poids + Hydratation + Sync

```
En parallele :
├── BACKEND      → RECETTES-01 a 03 endpoints + POIDS-01/02 + HYDRA-01 endpoints
├── MOBILE       → RECETTES-01/02 ecrans + POIDS-01 ecran + HYDRA-01 widget
├── SHARED       → SYNC-01 a 03 (logique offline) + POIDS-02 (recalcul quotas) + HYDRA-01 (calcul objectif)

REVIEW sur chaque US.
```

### Sprint 4 — Legal + Support + Qualite + Dashboard hebdo

```
En parallele :
├── MOBILE       → LEGAL-01 a 03 ecrans + SUPPORT-01/02 + UX-02 a 05 + DASHBOARD-02
├── BACKEND      → LEGAL-04 (chiffrement) + NOTIF-01 + SUPPORT-02 endpoint
├── SHARED       → QUALITE-01 (tests unitaires logique metier)
├── BACKEND      → QUALITE-02 (tests integration API)

PROJECT-MASTER : verification globale finale.
```

---

## 4. Workflow d'execution d'une US

### US standard (non-critique)

```
1. PROJECT-MASTER selectionne la US a executer
   │
2. PROJECT-MASTER dispatche aux agents concernes
   │  (souvent 2-3 agents par US : shared + backend + mobile)
   │
3. Agents executent en parallele quand possible
   │  - SHARED : modeles + logique metier
   │  - BACKEND : endpoints API
   │  - MOBILE : ecrans UI
   │
4. Chaque agent fait un commit sur sa branche feature/[US-ID]
   │
5. REVIEW analyse le code de chaque agent
   │  - Si APPROVE → passe a l'etape 6
   │  - Si CHANGES_REQUESTED → retour a l'agent dev pour correction
   │
6. PROJECT-MASTER verifie l'integration
   │  - Compile tout ensemble
   │  - Verifie pas de duplication
   │  - Verifie coherence entre modules
   │  - Verifie que les tests passent
   │
7. Merge dans main
   │
8. US marquee comme terminee
```

### US critique (necessite validation utilisateur)

Les US suivantes sont marquees **CRITIQUE** — le workflow s'arrete apres la review agent et attend la validation de l'utilisateur avant le merge :

| US critique | Raison |
|-------------|--------|
| SETUP-01 | Structure fondatrice du projet — tout en depend |
| SETUP-02 | Structure backend — tout en depend |
| SETUP-05 | Schema SQLDelight — impacte tout le offline |
| QUOTAS-01 | Algorithme de calcul des quotas — coeur metier |
| RECO-01 | Algorithme de recommandation — coeur metier |
| LEGAL-01, LEGAL-02 | Politique de confidentialite et CGU — contenu legal |
| LEGAL-04 | Chiffrement des donnees sensibles |
| SYNC-01, SYNC-02 | Logique de synchronisation offline — complexe |
| DATA-01 | Import Ciqual — qualite des donnees |

```
Workflow US critique :

1-6. Meme workflow que US standard
   │
7. ⛔ BLOQUANT — Attente validation utilisateur
   │  - PROJECT-MASTER ecrit dans docs/WAITING-REVIEW.md
   │  - L'utilisateur review le code
   │  - L'utilisateur valide ou demande des modifications
   │
8. Si valide → Merge dans main
   │
9. US marquee comme terminee
```

### Taches impossibles pour les agents → TODO utilisateur

Certaines taches necessitent une action humaine (comptes, cles API, configuration externe). Les agents ne peuvent PAS les faire. Le PROJECT-MASTER les remonte dans un fichier dedie.

**`docs/TODO-HUMAIN.md`** — maintenu par le PROJECT-MASTER :

```markdown
# Actions requises par l'utilisateur

| Priorite | Action | Bloque quelles US | Statut |
|----------|--------|-------------------|--------|
| 🔴 Bloquant | Creer un projet Firebase + obtenir les fichiers google-services.json (Android) et GoogleService-Info.plist (iOS) | AUTH-01 a AUTH-05, NOTIF-01 | Todo |
| 🔴 Bloquant | Configurer Google Sign-In dans la console Firebase | AUTH-03 | Todo |
| 🔴 Bloquant | Configurer Apple Sign-In (Apple Developer Account + Firebase) | AUTH-05 | Todo |
| 🔴 Bloquant | Creer un compte Railway + provisionner les services | INFRA-02 | Todo |
| 🟡 Important | Telecharger la base Ciqual (CSV) depuis le site ANSES | DATA-01 | Todo |
| 🟡 Important | Creer un compte Sentry + obtenir le DSN | INFRA-03 | Todo |
| 🟡 Important | Creer un compte UptimeRobot | INFRA-03 | Todo |
| 🟡 Important | Rediger le contenu final de la politique de confidentialite (consulter juriste) | LEGAL-01 | Todo |
| 🟡 Important | Rediger le contenu final des CGU | LEGAL-02 | Todo |
| 🔵 Plus tard | Creer les comptes Google Play Console et Apple Developer | Publication | Todo |
| 🔵 Plus tard | Rediger le contenu de la FAQ | SUPPORT-02 | Todo |
| 🔵 Plus tard | Creer 50-100 recettes vegan/vegetariennes | RECETTES-03 | Todo |
```

Les agents utilisent des **mocks/placeholders** quand une action humaine est en attente (ex: fichier Firebase factice pour que le build passe, contenu lorem ipsum pour les CGU) et le signalent dans le TODO.

---

## 5. Skills (slash commands) a creer

| Skill | Description | Utilise par |
|-------|-------------|-------------|
| `/project-master` | Orchestre un sprint : lit le backlog, dispatche les US, verifie l'integration | Utilisateur |
| `/agent-backend` | Execute une US cote backend (Ktor, PostgreSQL, endpoints) | PROJECT-MASTER |
| `/agent-mobile` | Execute une US cote mobile (Compose, ecrans, navigation) | PROJECT-MASTER |
| `/agent-shared` | Execute une US dans le module shared KMP (logique metier, modeles, sync) | PROJECT-MASTER |
| `/agent-data` | Execute une tache d'import/transformation de donnees | PROJECT-MASTER |
| `/agent-infra` | Execute une tache d'infrastructure (CI/CD, deploy, monitoring) | PROJECT-MASTER |
| `/agent-review` | Review de code sur une US terminee | PROJECT-MASTER |
| `/sprint-status` | Affiche l'etat d'avancement du sprint en cours | Utilisateur |
| `/us-status [US-ID]` | Affiche le statut detaille d'une US | Utilisateur |

---

## 6. Strategie Git

### Branches

```
main (production)
├── feature/SETUP-01-kmp-init
├── feature/AUTH-01-inscription-email
├── feature/JOURNAL-01-saisie-aliment
├── ...
```

### Regles

- 1 branche par US (`feature/[US-ID]-description-courte`)
- Chaque agent commit sur la branche de la US qu'il traite
- Pas de merge dans main sans review APPROVE
- Le PROJECT-MASTER fait le merge apres validation
- CI doit passer (lint + tests) avant merge

---

## 7. Fichiers de suivi

Le PROJECT-MASTER maintient un fichier de suivi dans le repo :

**`docs/sprint-tracker.md`** — Etat de chaque US :

```markdown
| US-ID | Titre | Agents | Statut | Review | Merge |
|-------|-------|--------|--------|--------|-------|
| SETUP-01 | KMP init | SHARED, MOBILE | Done | APPROVE | Merged |
| SETUP-02 | Ktor init | BACKEND | In Progress | - | - |
| AUTH-01 | Inscription | BACKEND, MOBILE, SHARED | Todo | - | - |
```

---

## 8. Points d'attention

### Risques specifiques aux agents

1. **Duplication de modeles** : Le plus gros risque. Les modeles de donnees (User, Aliment, Recette...) existent en 3 endroits : base PostgreSQL (Exposed), module shared (data classes), et SQLDelight (schema local). Le SHARED agent est la source de verite pour les modeles Kotlin. Le BACKEND les reutilise ou les adapte. Le PROJECT-MASTER verifie la coherence.

2. **Contrats API** : Les endpoints doivent etre definis AVANT que MOBILE et BACKEND codent en parallele. Le SHARED agent definit les interfaces (request/response models). BACKEND implemente. MOBILE consomme.

3. **Conflits Git** : Avec plusieurs agents sur le meme repo, les conflits sont possibles. Mitigation : chaque agent travaille sur des fichiers differents (modules separes). Le PROJECT-MASTER resout les conflits si necessaire.

4. **Coherence des noms** : Tous les agents doivent utiliser les memes conventions de nommage. Le SHARED agent definit les conventions dans un fichier `CONVENTIONS.md` au sprint 0.

### Ordre de priorite des conventions

Le SHARED agent cree au Sprint 0 :
- `CONVENTIONS.md` : Nommage, structure, patterns
- Modeles de donnees partages (data classes)
- Interfaces des repositories
- Contrats API (request/response models)

Tous les autres agents s'alignent sur ces definitions.

---

## 9. Decisions validees

| Question | Decision |
|----------|----------|
| Nombre d'agents | 7 (project-master, backend, mobile, shared, data, infra, review) |
| Autonomie PROJECT-MASTER | Autonome — lance les sprints seul, l'utilisateur verifie en parallele |
| Review | Double review : agent REVIEW + utilisateur sur les US critiques (blocage avant merge) |
| Branches | 1 branche par US |
| Equipe | Tout passe par le PROJECT-MASTER, pas d'equipier dans le workflow agents |
| Taches humaines | Remontees dans `docs/TODO-HUMAIN.md`, agents utilisent des mocks en attendant |
| Precision des US | Sprint -1 (fondations) fait par nous pour creer les fichiers de reference avant de lancer les agents |

---

## 10. Prochaines etapes

1. **[A faire maintenant]** Creer les fichiers de fondation (Sprint -1) :
   - `CONVENTIONS.md`
   - `docs/api-contracts.md`
   - `docs/data-models.md`
   - `docs/project-structure.md`

2. **[Ensuite]** Creer les skills des 7 agents + skills utilitaires

3. **[Ensuite]** Initialiser `docs/sprint-tracker.md`, `docs/TODO-HUMAIN.md`, `docs/WAITING-REVIEW.md`

4. **[Ensuite]** Lancer le Sprint 0 via `/project-master`

---

*Document valide. Prochaine etape : Sprint -1 (fondations).*
