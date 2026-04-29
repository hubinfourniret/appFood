# appFood — Organisation du travail avec Claude Code

> Date : 2026-04-28
> Remplace : `docs/phase4-dispatch-plan-agents.md` (conserve a titre historique)
> Statut : Reference active

---

## 1. Roles et responsabilites

### Claude Code — Developpeur principal

Claude Code est le **seul developpeur** du projet. Il a acces a l'integralite du codebase et implemente toutes les features, corrections et optimisations.

**Ce que Claude Code fait :**
- Implemente les features de bout en bout (backend + shared + mobile)
- Corrige les bugs
- Ecrit les tests
- Maintient la coherence du code
- Met a jour les fichiers de suivi (`sprint-tracker.md`, `TODO-HUMAIN.md`, `WAITING-REVIEW.md`)
- Propose des diagnostics et des plans d'action

**Ce que Claude Code ne fait PAS :**
- Les actions humaines listees dans `docs/TODO-HUMAIN.md` (cles API, comptes, contenu legal, etc.)
- Les decisions produit (priorisation, scope, coupes)
- Les tests manuels sur device/emulateur

### Superviseur (utilisateur) — Coordinateur et decideur

Le superviseur coordonne le travail, donne les directives, valide les resultats et peut faire des retouches manuelles ponctuelles.

**Ce que le superviseur fait :**
- Definit les priorites et l'ordre des taches
- Valide les features terminees (test fonctionnel sur emulateur/device)
- Prend les decisions produit (quoi garder, quoi couper, quoi reporter)
- Realise les actions humaines (comptes, cles, contenu)
- Fait des retouches manuelles si necessaire
- Donne les directives de haut niveau ("corrige ce bug", "ajoute cette feature")

**Ce que le superviseur ne fait PAS en general :**
- Coder les features lui-meme (sauf retouches ponctuelles)
- Faire des code reviews ligne par ligne (Claude Code gere la qualite)
- Gerer les details d'implementation (choix de pattern, structure de fichiers)

---

## 2. Format des taches

### Ancien format : User Stories formelles (avant Sprint 5)

L'ancien format etait calque sur une equipe humaine :
- US decoupees par couche technique (BACKEND, MOBILE, SHARED)
- Criteres d'acceptation detailles
- Dispatch a des agents specialises
- Integration post-sprint

**Problemes constates :**
- Desync DTOs entre agents (9 bugs P0 post-Sprint 3)
- 56 ViewModels stubbes pendant 2 sprints (couches developpees separement)
- 11 enums dupliques (agents isolés, chacun cree ses types)
- Sprint entier de cablage (reconnexion des stubs)
- Audits post-sprint systematiques pour corriger les incoherences

### Nouveau format : Taches verticales orientees resultat

Une tache = une feature complete, de la base de donnees a l'ecran.

```markdown
## TACHE-{ID} : {Titre court}

**Priorite** : Critique | Haute | Moyenne | Basse
**Objectif** : {Description en 1-3 phrases de ce que l'utilisateur final voit/fait}
**Contraintes** : {Contraintes techniques specifiques, si applicable}
**Validation** : {Comment le superviseur verifie que c'est fait}
**Statut** : Todo | En cours | A valider | Done
```

### Exemples

**Bon** (tache verticale) :
```markdown
## TACHE-501 : Dashboard affiche les vraies donnees

**Priorite** : Critique
**Objectif** : Apres login, le dashboard affiche les calories du jour, les macros,
et les repas saisis — pas "0/0 kcal".
**Contraintes** : Investiguer si le probleme vient du backend (endpoint /api/v1/dashboard)
ou du client (DashboardViewModel fallback a 0).
**Validation** : Login → dashboard affiche des donnees reelles (ou "Aucune saisie" 
si le journal est vide, mais les quotas doivent etre > 0).
**Statut** : Todo
```

**Mauvais** (ancien format, decoupe horizontale) :
```markdown
DASH-BUG-01 dispatche a :
- BACKEND : verifier l'endpoint
- SHARED : verifier le ViewModel
- MOBILE : verifier l'affichage
→ 3 agents travaillent en isolation, risque de desync
```

### Quand utiliser l'ancien format US

Les US formelles restent pertinentes pour :
- La **documentation produit** (backlog `phase3-backlog-rapport.md` = reference des fonctionnalites)
- Les **US critiques metier** (QUOTAS-01, RECO-01, SYNC-01) qui necessitent des specs detaillees
- La **tracabilite** (savoir ce qui a ete fait et quand)

Mais pour le **dispatch du travail**, on utilise les taches verticales.

---

## 3. Organisation des agents

### Avant : 7 agents specialises

```
PROJECT-MASTER → BACKEND, MOBILE, SHARED, DATA, INFRA, REVIEW
```

Chaque agent etait isole, avec son propre contexte, ses propres fichiers. Cela creait des frontieres artificielles puisque Claude Code a acces a tout le codebase.

### Apres : structure simplifiee

| Agent | Role | Quand l'utiliser |
|-------|------|-----------------|
| **PROJECT-MASTER** | Orchestrateur. Lit le backlog, propose un plan, coordonne. | Au debut d'une session de travail, pour faire le point. |
| **DEV** (Claude Code direct) | Implemente les features full-stack. Un seul agent qui voit tout. | Pour toutes les taches de developpement. C'est le mode par defaut. |
| **REVIEW** | Relecture apres une feature critique. | Pour les US critiques metier uniquement (algo, crypto, sync). |
| **DATA** | Taches d'import/transformation de donnees volumineuses. | Import Ciqual, coherence Meilisearch/PostgreSQL. |
| **INFRA** | CI/CD, Docker, deploiement. | Configuration Railway, GitHub Actions, monitoring. |

Les agents **BACKEND**, **MOBILE** et **SHARED** sont **supprimes en tant qu'agents separes**. Claude Code travaille directement sur toutes les couches en meme temps. Les fichiers `.CLAUDE/commands/agent-backend.md`, `agent-mobile.md` et `agent-shared.md` restent comme **reference des conventions par couche** mais ne sont plus utilises pour dispatcher des sous-processus isoles.

### Pourquoi ce changement

Un developpeur humain a un contexte limite — il se specialise sur une couche. Claude Code peut tenir tout le contexte du projet. Le decoupage en agents specialises :
- **Perd du contexte** a chaque lancement de subprocess
- **Cree des frontieres artificielles** entre des fichiers qui doivent etre coherents
- **Retarde l'integration** (stubs → cablage → audit)
- **Multiplie les risques de desync** (DTOs, enums, signatures)

En travaillant en tranche verticale, Claude Code cree un DTO, l'utilise dans le backend, le consomme dans le client API, le cable dans le ViewModel et l'affiche dans l'ecran — le tout dans la meme passe. Zero stub, zero desync.

---

## 4. Workflow d'execution

### Flux standard (feature ou bugfix)

```
1. Superviseur donne une directive
   ("corrige le bug dashboard", "ajoute les portions par categorie")
   │
2. Claude Code analyse le probleme
   - Lit le code concerne
   - Identifie les fichiers a modifier
   - Propose un plan si la tache est complexe
   │
3. Claude Code implemente (tranche verticale)
   - Modeles/DTOs si necessaire
   - Backend (endpoint, service, DAO)
   - Shared (repository, use case, API client)
   - Mobile (ViewModel, ecran)
   - Tests si pertinent
   │
4. Claude Code verifie
   - Compilation (`:shared` + `:backend`)
   - Coherence avec les conventions
   - Pas de regression evidente
   │
5. Claude Code rapporte
   - Resume court de ce qui a change
   - Ce que le superviseur doit tester
   │
6. Superviseur valide
   - Test fonctionnel sur emulateur/device
   - Feedback ("OK" ou "le bouton X ne marche pas")
   │
7. Si OK → Tache marquee Done
   Si KO → Claude Code corrige (retour etape 3)
```

### Flux tache critique (algo metier, crypto, sync)

Meme flux, avec une etape supplementaire :

```
4b. Agent REVIEW relit le code critique
    - Verification de l'algorithme
    - Verification de la securite
    - Rapport structure
    │
4c. Si REVIEW signale un probleme → correction avant etape 5
```

Les taches critiques sont :
- Tout ce qui touche au **calcul des quotas** (QUOTAS-01 et derivees)
- Tout ce qui touche a la **recommandation** (RECO-01 et derivees)
- Tout ce qui touche au **chiffrement** (LEGAL-04)
- Tout ce qui touche a la **synchronisation offline** (SYNC-01/02)
- Tout ce qui touche aux **donnees de sante** (suppression compte, export RGPD)

### Flux tache infra ou data

Pour les taches purement infra (CI/CD, Docker, Railway) ou data (import Ciqual, coherence Meilisearch), les agents specialises DATA et INFRA restent pertinents car :
- Ce sont des taches isolees qui ne touchent pas au code applicatif
- Elles necessitent des connaissances specifiques (config Railway, format CSV Ciqual)
- Elles peuvent tourner en parallele du dev applicatif

---

## 5. Gestion du backlog

### Avant : sprints formels avec batches

```
Sprint 2 Batch 1 → 9 US en parallele sur 5 agents
Sprint 2 Batch 2 → 3 US sequentielles
Sprint 2 Reporte → 4 US au sprint suivant
```

### Apres : backlog priorise, flux continu

Le backlog est une **liste ordonnee par priorite**. Claude Code prend les taches dans l'ordre. Le superviseur peut reordonner a tout moment.

```markdown
# Backlog actif

1. [Critique] TACHE-501 — Dashboard affiche les vraies donnees
2. [Critique] TACHE-502 — Ajout aliment corrige (coherence Meilisearch/PG)
3. [Haute]    TACHE-503 — Onglet Recette fonctionnel dans AddEntry
4. [Haute]    TACHE-504 — Portions adaptees par categorie
5. [Moyenne]  TACHE-505 — Optimiser RecommandationService
```

**Regles :**
- Le superviseur peut inserer, reordonner ou retirer des taches a tout moment
- Claude Code prend la tache suivante quand la precedente est validee (ou en attente de validation)
- Pas de "sprint planning" formel — le superviseur dit "continue" ou "fais X d'abord"
- Le sprint tracker (`docs/sprint-tracker.md`) reste maintenu pour la tracabilite historique

### Conservation des sprints pour l'historique

Les sprints 0 a 4 dans `sprint-tracker.md` restent tels quels — c'est l'historique du projet. A partir du Sprint 5, le tracker utilise le nouveau format de taches.

---

## 6. Validation par le superviseur

### Ce qui change

| Avant | Apres |
|-------|-------|
| Code review ligne par ligne | Test fonctionnel sur emulateur/device |
| Agent REVIEW sur chaque US | Agent REVIEW uniquement sur taches critiques |
| `WAITING-REVIEW.md` pour chaque US critique | `WAITING-REVIEW.md` uniquement pour decisions produit ou taches critiques |
| Validation avant merge (bloquant) | Validation apres implementation (le superviseur teste le resultat) |

### Comment le superviseur valide

1. Claude Code termine et decrit ce qui a change + quoi tester
2. Le superviseur lance l'app sur emulateur (ou device)
3. Il teste le flow concerne :
   - **Golden path** : le cas normal fonctionne
   - **Edge cases** : les cas limites sont geres (offline, champs vides, etc.)
4. Il donne un feedback :
   - "OK" → tache Done
   - "Le bouton X ne fait rien" → Claude Code corrige
   - "Ca marche mais j'aimerais aussi Y" → nouvelle tache dans le backlog

### Taches qui ne necessitent PAS de validation superviseur

- Bugfixes evidents (crash, erreur de compilation)
- Refactoring interne sans changement de comportement
- Mise a jour de dependances mineures
- Ajout de tests

### Taches qui necessitent une validation superviseur

- Toute nouvelle feature visible par l'utilisateur
- Tout changement d'algo metier (quotas, recommandations)
- Tout changement de flow (navigation, onboarding)
- Tout changement de securite (chiffrement, auth)

---

## 7. Fichiers de suivi

### Fichiers maintenus

| Fichier | Role | Maintenu par |
|---------|------|-------------|
| `docs/sprint-tracker.md` | Historique + backlog actif | Claude Code |
| `docs/TODO-HUMAIN.md` | Actions humaines en attente | Claude Code |
| `docs/WAITING-REVIEW.md` | Taches en attente de validation superviseur | Claude Code |
| `CONVENTIONS.md` | Conventions de code (inchange) | Claude Code |
| `docs/api-contracts.md` | Contrats API (reference) | Claude Code |
| `docs/data-models.md` | Modeles de donnees (reference) | Claude Code |

### Fichiers conserves en lecture seule (historique)

| Fichier | Raison |
|---------|--------|
| `docs/phase3-backlog-rapport.md` | Reference produit — liste des fonctionnalites prevues |
| `docs/phase4-dispatch-plan-agents.md` | Historique — ancien plan d'agents (remplace par ce document) |
| `docs/us-clarifications.md` | Specs detaillees des US critiques (toujours valides) |

### Format du backlog actif dans sprint-tracker.md

```markdown
## Backlog actif

| # | ID | Titre | Priorite | Statut | Validation |
|---|-----|-------|----------|--------|------------|
| 1 | TACHE-501 | Dashboard affiche les vraies donnees | Critique | Todo | Test emulateur |
| 2 | TACHE-502 | Ajout aliment corrige | Critique | Todo | Test emulateur |
| 3 | TACHE-503 | Onglet Recette fonctionnel | Haute | Todo | Test emulateur |
```

---

## 8. Ce qui ne change PAS

Les elements suivants restent identiques :

- **Architecture** : Clean Architecture (UseCase → Repository → DataSource) cote shared, Routes → Service → DAO cote backend
- **Conventions de code** : `CONVENTIONS.md` reste la reference
- **Stack technique** : KMP + Compose + Ktor + PostgreSQL + Meilisearch
- **Contrats API** : `docs/api-contracts.md` reste la reference
- **Modeles de donnees** : `docs/data-models.md` reste la reference
- **Regles de securite** : Inchangees
- **TODO-HUMAIN** : Meme fonctionnement (actions humaines remontees par Claude Code)

---

## 9. Retrocompatibilite avec les anciens sprints

Les sprints 0 a 4 ont ete executes avec l'ancien modele (7 agents, US horizontales). Cela a fonctionne mais avec un cout d'integration significatif (audits, cablage, corrections P0).

A partir du Sprint 5 :
- Les taches existantes du Sprint 5 (`DASH-BUG-01`, `JOURNAL-BUG-01`, `UX-06`, `UX-07`, `RECO-PERF-01`) sont converties au nouveau format
- Le sprint tracker est mis a jour avec le nouveau format
- Les nouveaux travaux utilisent le format tache verticale

Les references aux "agents BACKEND, MOBILE, SHARED" dans les anciens sprints sont conservees telles quelles dans l'historique.

---

## 10. Resume des changements

| Aspect | Avant | Apres |
|--------|-------|-------|
| Developpeur | 7 agents specialises | Claude Code (1 dev full-stack) |
| Superviseur | Review code + validation | Test fonctionnel + directives |
| Format taches | US formelles par couche | Taches verticales orientees resultat |
| Decoupe | Horizontale (backend/mobile/shared) | Verticale (feature de bout en bout) |
| Planning | Sprints formels avec batches | Backlog priorise, flux continu |
| Integration | Post-sprint (cablage, audit) | Continue (chaque feature est complete) |
| Agents actifs | 7 (PROJECT-MASTER, BACKEND, MOBILE, SHARED, DATA, INFRA, REVIEW) | 3 reguliers (PROJECT-MASTER, DATA, INFRA) + REVIEW occasionnel |
| Validation | Code review + compilation | Test fonctionnel par superviseur |

---

*Ce document est la reference active pour l'organisation du travail. Il remplace `docs/phase4-dispatch-plan-agents.md` pour tout nouveau travail.*
