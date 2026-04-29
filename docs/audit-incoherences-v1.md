# Audit d'incoherences — appFood v1

> Date : 2026-04-28
> Contexte : Audit complet du codebase apres 5 sprints developpes par 7 agents isoles.
> Objectif : Identifier tous les problemes causes par l'approche agents horizontaux
> pour les corriger en tranches verticales avec la nouvelle approche.

---

## Resume executif

| Categorie | Critique | Haute | Moyenne | Basse | Total |
|-----------|----------|-------|---------|-------|-------|
| API client vs backend (desync endpoints) | 4 | 5 | 1 | 1 | 11 |
| DTOs shared vs backend (champs/types) | 3 | 2 | 2 | 0 | 7 |
| Logique metier dupliquee | 1 | 2 | 3 | 1 | 7 |
| ViewModels incomplets (stubs/TODO) | 2 | 3 | 2 | 2 | 9 |
| Compilation / imports | 1 | 1 | 0 | 0 | 2 |
| UI / Strings | 0 | 0 | 1 | 1 | 2 |
| **Total** | **11** | **13** | **9** | **5** | **38** |

---

## TACHE-R01 — API client desynchronise du backend

**Priorite : Critique**
**Objectif** : Aligner les appels API cote client (shared/*Api.kt) avec les vrais endpoints backend.

Les agents MOBILE/SHARED et BACKEND ont travaille en isolation. Le client appelle des endpoints
avec des paths, params et types de retour differents de ce que le backend fournit reellement.

### Problemes identifies

#### R01-A : RecetteApi — mauvais noms de parametres
- **Client** : `shared/.../data/remote/RecetteApi.kt` ligne 23-27
  - Envoie `query=` au lieu de `q=`
  - Envoie `limit=` au lieu de `size=`
- **Backend** : `backend/.../routes/RecetteRoutes.kt` lignes 35-37
  - Attend `q=` et `size=`
- **Impact** : La recherche de recettes ne retourne jamais de resultats

#### R01-B : HydratationApi — mauvais paths et parametres
- **Client** : `shared/.../data/remote/HydratationApi.kt` lignes 13-31
  - Appelle `GET /api/v1/hydratation/daily?date=` → backend sert `/api/v1/hydratation?date=`
  - Appelle `GET /api/v1/hydratation/weekly?dateFrom=&dateTo=` → backend attend `?weekOf=`
- **Backend** : `backend/.../routes/HydratationRoutes.kt` lignes 26-42
- **Impact** : L'hydratation quotidienne et hebdomadaire ne charge jamais

#### R01-C : HydratationApi — type de retour incorrect (weekly)
- **Client** : attend `List<HydratationResponse>` (ligne 29)
- **Backend** : retourne `HydratationWeeklyResponse` (objet unique)
- **Impact** : Crash de deserialisation

#### R01-D : PoidsApi — endpoint inexistant + type de retour incorrect
- **Client** : `shared/.../data/remote/PoidsApi.kt` lignes 12, 30-32
  - `getHistory()` attend `List<PoidsResponse>` → backend retourne `PoidsListResponse`
  - `getCurrent()` appelle `GET /api/v1/poids/current` → endpoint inexistant
- **Impact** : Crash de deserialisation sur l'historique, 404 sur current

#### R01-E : SyncApi — backend inexistant
- **Client** : `shared/.../data/remote/SyncApi.kt` — `pushSync()` et `pullSync()` implementes
- **Backend** : Aucune route, aucun service, aucun DAO pour la sync
- **Impact** : La synchronisation offline ne fonctionne pas du tout cote serveur

#### R01-F : NotificationApi — client inexistant
- **Backend** : `backend/.../routes/NotificationRoutes.kt` — 4 endpoints implementes
- **Client** : Aucun `NotificationApi.kt` dans shared
- **Impact** : Les notifications ne sont pas consommees cote mobile

#### R01-G : PortionApi — client incomplet
- **Client** : `shared/.../data/remote/PortionApi.kt` — seulement GET implemente
- **Backend** : GET, POST, PUT, DELETE implementes
- **Impact** : Impossible de creer/modifier/supprimer des portions depuis le mobile

---

## TACHE-R02 — DTOs desalignes shared vs backend

**Priorite : Critique**
**Objectif** : Unifier les champs, types et nullabilite des DTOs entre shared et backend.

#### R02-A : AddJournalEntryRequest — champ `nom` manquant
- **Shared** : `shared/.../api/request/JournalRequests.kt` ligne 6 — a un champ `nom: String? = null`
- **Backend** : `backend/.../routes/dto/JournalDtos.kt` ligne 8 — pas de champ `nom`
- **Impact** : Le nom de l'aliment envoye par le client est silencieusement ignore

#### R02-B : UserExportResponse — types de listes incompatibles
- **Shared** : `shared/.../api/response/UserResponses.kt` ligne 34
  - Attend `List<JournalEntryResponse>`, `List<QuotaResponse>`, `List<PoidsResponse>`, etc.
- **Backend** : `backend/.../routes/dto/UserDtos.kt` ligne 62
  - Retourne `List<JournalEntryExportResponse>`, `List<QuotaExportResponse>`, etc.
- **Impact** : L'export RGPD crash a la deserialisation (5 types incompatibles)

#### R02-C : Backend DTOs — erreurs de compilation (imports manquants)
- `backend/.../routes/dto/AlimentDtos.kt` ligne 15 — utilise `NutrimentValuesResponse` non importe
- `backend/.../routes/dto/DashboardDtos.kt` — utilise 6 types d'autres fichiers sans import
- **Impact** : Potentielle erreur de compilation (fonctionne si meme package, mais fragile)

#### R02-D : QuotaResponse — valeurs par defaut inconsistantes
- **Shared** : `total: Int = 0`, `updatedAt: String = ""`
- **Backend** : `total: Int` (pas de defaut), `updatedAt: String` (pas de defaut)
- **Impact** : Mineur — fonctionne grace a `ignoreUnknownKeys = true` et defauts cote client

---

## TACHE-R03 — Logique metier dupliquee (recommandations)

**Priorite : Critique**
**Objectif** : Eliminer la duplication de logique entre shared et backend pour les recommandations.

Le backend `RecommandationService` (541 lignes) reimplemente ~70% de la logique qui existe
deja dans `RecommandationAlimentUseCase` et `RecommandationRecetteUseCase` du shared.

#### R03-A : Algorithme de recommandation duplique
- **Shared** : `shared/.../domain/recommandation/RecommandationAlimentUseCase.kt` (350 lignes)
- **Backend** : `backend/.../service/RecommandationService.kt` (541 lignes)
- **Fonctions dupliquees** :
  - `identifierDeficits()` / `identifyDeficits()` — memes seuils (70%, 90%)
  - `getNutrimentsCritiques()` / `getCriticalNutrients()` — copie exacte
  - `scoreAliment()` — reimplemente independamment
  - `filtrerAliments()` / `isRegimeCompatible()` — approches differentes (List vs JSON string parsing)
  - `getAllergyPatterns()` / `ALLERGEN_PATTERNS` — donnees identiques maintenues separement
  - `appliquerDiversite()` / `applyDiversity()` — reimplemente
  - `calculerQuantiteSuggeree()` / `roundToTen()` — le backend oublie le minimum de 10g

#### R03-B : Accesseurs nutriments dupliques 3 fois
- **Backend** : 3 fonctions `when` de 16 cas chacune dans RecommandationService et QuotaService
  - `getAlimentNutrientValue()` (lignes 428-450)
  - `getRecetteNutrientValue()` (lignes 452-474)
  - `getConsumedValue()` dans QuotaService (lignes 205-227)
- **Shared** : `NutrimentValues.getByType()` (lignes 41-58) fait la meme chose en 1 methode
- **Risque** : Ajout d'un nutriment = modifier 4 endroits separement

#### R03-C : Serialisation inconsistante (List vs JSON string)
- **Shared** : `regimesCompatibles: List<RegimeAlimentaire>`
- **Backend** : stocke en JSON string `"[\"VEGAN\",\"VEGETARIEN\"]"`, parse manuellement
- Le backend fait `contains("VEGAN")` au lieu d'une comparaison d'enum typee
- **Risque** : Erreurs silencieuses si les noms d'enum changent

---

## TACHE-R04 — ViewModels incomplets et stubs restants

**Priorite : Haute**
**Objectif** : Finaliser le cablage de tous les ViewModels, supprimer les stubs et TODO.

#### R04-A : Google/Apple Sign-In stubbes (AuthViewModel)
- **Fichier** : `shared/.../ui/auth/AuthViewModel.kt` lignes 257, 267
- `signInWithGoogle()` et `signInWithApple()` retournent toujours `Success(needsOnboarding = true)` sans appel reel
- **TODO** : "Call googleSignInUseCase when created by SHARED agent"

#### R04-B : RecommandationViewModel — userId hardcode
- **Fichier** : `shared/.../ui/recommandation/RecommandationViewModel.kt` ligne 30-31
- `currentUserId = "current-user"` en dur
- **TODO** : "Inject a real user ID provider when auth session is wired"
- **Impact** : Les recommandations ne sont jamais liees au vrai utilisateur

#### R04-C : RecettesViewModel — favori non persiste
- **Fichier** : `shared/.../ui/recette/RecettesViewModel.kt` ligne 247
- `onToggleDetailFavorite()` met a jour l'UI locale mais pas le serveur
- **TODO** : "Call toggleFavoriRecetteUseCase when created by SHARED agent"

#### R04-D : JournalViewModel — navigation recette incomplete
- **Fichier** : `shared/.../ui/journal/JournalViewModel.kt` ligne 416
- **TODO** : "Naviguer vers le detail recette ou ajouter une portion recette au journal"
- Ajoute directement 1 portion au lieu de naviguer vers le detail

#### R04-E : 3 ViewModels avec repositories nullable et stubs fallback
- **DashboardViewModel** (lignes 24-25, 38-41, 119-147) : `dashboardRepository: DashboardRepository? = null` → fallback a des donnees fictives (2759 kcal hardcode)
- **WeeklyDashboardViewModel** (ligne 25, 57-59, 117-148) : meme pattern
- **ProfilViewModel** (lignes 25-26, 96-105, 167-170, 232-235) : fallback a profil fictif (Homme, 30 ans, 75kg, Vegan)
- **Impact** : Si Koin n'injecte pas correctement, l'app affiche des donnees fausses sans erreur visible

#### R04-F : println de debug en production
- `AuthViewModel.kt` ligne 174 : `println("WARNING: Echec envoi email de verification")`
- `JournalViewModel.kt` ligne 230 : `println("JournalViewModel.onValidateEntry error: ...")`
- **Conventions** : `println` interdit en prod (CONVENTIONS.md section 13)

---

## TACHE-R05 — Violation d'architecture (backend importe shared)

**Priorite : Haute**
**Objectif** : Resoudre l'import illegal de shared dans backend.

- **Fichier** : `backend/.../service/QuotaService.kt` lignes 11-12
  ```
  import com.appfood.shared.domain.quota.CalculerQuotasUseCase
  import com.appfood.shared.model.UserProfile
  ```
- **Probleme** : Le backend n'est pas cense dependre du module shared (pas de JVM target dans shared). Ca fonctionne actuellement car le build Gradle est configure de maniere permissive, mais c'est fragile.
- **Decision requise** : Soit ajouter un JVM target a shared (architecture "shared-jvm"), soit dupliquer le calcul dans backend (deja partiellement fait dans RecommandationService), soit creer un module `shared-logic` commun.

---

## TACHE-R06 — Violations convention datetime

**Priorite : Haute**
**Objectif** : Corriger les 8 fichiers qui utilisent `kotlinx.datetime.Instant` au lieu de `kotlin.time.Instant` dans shared.

Convention stricte (CONVENTIONS.md section 4) : shared utilise `kotlin.time.Instant`, backend utilise `kotlinx.datetime.Instant`.

| Fichier | Lignes |
|---------|--------|
| `shared/.../domain/hydratation/AjouterEauUseCase.kt` | 14 |
| `shared/.../domain/hydratation/GetHydratationJourUseCase.kt` | 14 |
| `shared/.../domain/poids/EnregistrerPoidsUseCase.kt` | 20 |
| `shared/.../ui/dashboard/DashboardViewModel.kt` | 43 |
| `shared/.../ui/dashboard/WeeklyDashboardViewModel.kt` | 62 |
| `shared/.../ui/hydratation/HydratationViewModel.kt` | 248, 265 |
| `shared/.../ui/poids/PoidsViewModel.kt` | 165, 197 |
| `shared/.../ui/recommandation/RecommandationViewModel.kt` | 32 |

**Note** : Ces fichiers utilisent le pattern de conversion epoch qui est correct en substance
(`kotlinx.datetime.Instant.fromEpochMilliseconds(nowMs)`) mais violent la convention stricte
du projet. La convention dit d'utiliser `kotlin.time.Instant` dans shared et de convertir
seulement quand on appelle des fonctions `kotlinx.datetime` (ex: `toLocalDateTime`).

---

## TACHE-R07 — Enums dupliques (11 enums)

**Priorite : Moyenne**
**Objectif** : Decider d'une strategie pour les 11 enums dupliques entre shared et backend.

Les enums sont identiques dans les deux modules (pas de divergence de valeurs) :
- `Role`, `Sexe`, `RegimeAlimentaire`, `NiveauActivite`, `MealType`, `NutrimentType`
- `SourceAliment`, `SourceRecette`, `ConsentType`, `NotificationType`, `ObjectifPoids`

**Localisation** :
- Shared : `shared/.../model/Enums.kt`
- Backend : `backend/.../database/tables/Enums.kt` (commentaire : "Dupliques depuis shared")

Le backend a aussi `SyncStatus` qui n'existe pas dans shared (mort-ne, jamais utilise).

**Decision requise** : Lie a R05 — si on ajoute un JVM target a shared ou un module `shared-logic`, les enums peuvent etre partages. Sinon, la duplication est acceptable mais necessite une discipline de synchronisation.

---

## TACHE-R08 — Sync backend inexistant

**Priorite : Moyenne** (la sync locale fonctionne, mais ne push/pull jamais)
**Objectif** : Implementer les endpoints sync cote backend ou decider de reporter a V1.1.

- **Shared** : SyncManager, SyncApi, SyncRequests, SyncResponses, sync_queue SQLDelight — tout est implemente
- **Backend** : Zero implementation (pas de routes, pas de service, pas de DAO)
- Les DTOs sync existent cote shared (SyncPushRequest, SyncPullResponse, etc.) mais n'ont aucun correspondant backend

**Impact actuel** : L'app enregistre les entries offline dans sync_queue mais ne les envoie jamais au serveur. Les donnees restent en local jusqu'a ce que l'utilisateur refasse un appel API direct.

---

## TACHE-R09 — Features mortes et code inutilise

**Priorite : Basse**
**Objectif** : Nettoyer le code mort.

#### R09-A : ObjectifPoids — enum utilise mais logique absente
- Le champ `UserProfile.objectifPoids` existe et est stocke en base
- Mais `CalculerQuotasUseCase` ne l'utilise jamais dans le calcul des quotas
- L'algo ignore completement si l'utilisateur veut prendre de la masse, maintenir ou perdre du poids

#### R09-B : SyncStatus — enum jamais utilise
- Defini dans shared `Enums.kt` ligne 65
- Aucun modele ne l'utilise, aucun code n'y fait reference

#### R09-C : CopyJournalDayRequest — DTO orphelin
- Defini dans `shared/.../api/request/JournalRequests.kt` ligne 26
- Prevu pour JOURNAL-05 (V1.1) — pas d'endpoint backend correspondant
- A supprimer ou a garder pour V1.1 avec un commentaire explicite

---

## TACHE-R10 — UI mineure

**Priorite : Basse**
**Objectif** : Corriger les petites incoherences UI.

#### R10-A : Fleches retour hardcodees
5 ecrans utilisent `Text("\u2190")` au lieu d'une constante Strings :
- `PrivacyPolicyScreen.kt` ligne 50
- `TermsOfServiceScreen.kt` ligne 49
- `HydratationScreen.kt` ligne 150
- `WeeklyDashboardScreen.kt` lignes 81, 220

#### R10-B : Fichier mal nomme
- `shared/.../ui/journal/PortionSelector.kt` exporte `PortionSelectorScreen`
- Convention : le fichier devrait s'appeler `PortionSelectorScreen.kt`

---

## Corrections deja appliquees (2026-04-28)

### R05 — CORRIGE : Enums unifies via typealias
- `backend/.../database/tables/Enums.kt` : les 11 enums dupliques ont ete remplaces par des
  `typealias` vers `com.appfood.shared.model.*` (source de verite unique)
- Le JVM target et `implementation(project(":shared"))` etaient deja en place dans les build.gradle.kts
- Zero impact sur les ~55 fichiers qui importent ces enums (meme package, resolution transparente)

### R07 — CORRIGE : consequence de R05
- Les 11 enums pointent maintenant vers shared. Plus de duplication.

### R03 (partiel) — CORRIGE : Constantes de recommandation unifiees
- Cree `shared/.../domain/recommandation/RecommandationConstants.kt` : objet public contenant
  seuils de deficit (70%, 90%), poids (3.0, 2.0, 1.0), nutriments critiques par regime,
  patterns d'allergenes, et fonction d'arrondi avec minimum 10g
- `RecommandationAlimentUseCase` (shared) : refactorise pour utiliser `RecommandationConstants`
- `RecommandationService` (backend) : refactorise pour utiliser `RecommandationConstants`
  - Supprime `getCriticalNutrients()` (duplique) → delegue a `RecommandationConstants.getNutrimentsCritiques()`
  - Supprime `ALLERGEN_PATTERNS` (duplique) → utilise `RecommandationConstants.ALLERGEN_PATTERNS`
  - Supprime `roundToTen()` (sans minimum 10g) → utilise `RecommandationConstants.arrondirQuantite()` (avec minimum)
  - Remplace seuils hardcodes (70.0, 90.0, 3.0, 2.0, 1.0) par constantes nommees
- `QuotaService` : simplifie les conversions d'enum desormais inutiles (typealias = meme type)

### Compilation verifiee
- `:shared:compileCommonMainKotlinMetadata` : BUILD SUCCESSFUL
- `:backend:compileKotlin` : BUILD SUCCESSFUL

---

## Corrections appliquees (2026-04-29)

### R01 — CORRIGE : API client aligne avec le backend
- **RecetteApi** : `query=` → `q=`, `limit=` → `size=` (R01-A)
- **HydratationApi** : path `/hydratation/daily` → `/hydratation`, params `dateFrom/dateTo` → `weekOf`, type retour `List<HydratationResponse>` → `HydratationWeeklyResponse` (R01-B/C)
- **HydratationRepositoryImpl** : adapte pour mapper `HydratationWeeklyResponse.parJour` vers `List<HydratationJournaliere>`
- **HydratationRepository** interface : signature `getWeekly` simplifiee (weekOf au lieu de dateFrom/dateTo)
- **HydratationViewModel** : appel `getWeekly` corrige
- **PoidsApi** : type retour `List<PoidsResponse>` → `PoidsListResponse`, suppression de `getCurrent()` (endpoint inexistant), `addEntry` retourne `AddPoidsResponse` (R01-D)
- **PoidsRepositoryImpl** : `getHistory` utilise `listResponse.data`, `addEntry` utilise `response.poids`, `getCurrent` utilise `getHistory` + premier element
- **PortionApi** : ajout `createPortion()`, `updatePortion()`, `deletePortion()` (R01-G)
- **Non traite** : R01-E (SyncApi backend inexistant → R08) et R01-F (NotificationApi client → feature future)

### R02 — CORRIGE : DTOs alignes
- **AddJournalEntryRequest** backend : ajout champ `nom: String? = null` (R02-A)
- **HydratationResponse** shared : `entrees` avec defaut `= emptyList()` (R02-B, compatibilite export)
- R02-C (imports backend DTOs) : non-bloquant (meme package)
- R02-D (QuotaResponse defaults) : deja compatible

### R03 — CORRIGE (complet) : Duplication eliminee
- Cree `NutrientAccessors.kt` : extensions `AlimentRow.getNutrientValue()`, `RecetteRow.getNutrientValue()`, `NutrientSums.getByType()`
- `RecommandationService` : 2 fonctions de 20 lignes chacune → 2 delegations d'une ligne
- `QuotaService` : `getConsumedValue()` (17 lignes) → delegation d'une ligne

**Compilation verifiee** : `:shared` BUILD SUCCESSFUL | `:backend` BUILD SUCCESSFUL

---

## Plan de correction restant

| # | Tache | Priorite | Effort | Statut |
|---|-------|----------|--------|--------|
| 1 | **R01-F** — NotificationApi client | Basse | S | Todo (feature future) |
| - | ~~**R04**~~ — ViewModels stubs/TODO | ~~Haute~~ | - | **Done** |
| - | ~~**R06**~~ — Violations datetime | ~~Haute~~ | - | **Done** |
| - | ~~**R08**~~ — Backend sync | ~~Moyenne~~ | - | **Done** |
| - | ~~**R09**~~ — Code mort | ~~Basse~~ | - | **Non applicable** (SyncStatus utilise, ObjectifPoids = V1.1) |
| - | ~~**R10**~~ — UI mineures | ~~Basse~~ | - | **Done** |
| - | ~~**R01**~~ — API client vs backend | ~~Critique~~ | - | **Done** |
| - | ~~**R02**~~ — DTOs shared vs backend | ~~Critique~~ | - | **Done** |
| - | ~~**R03**~~ — Duplication recommandations | ~~Critique~~ | - | **Done** |
| - | ~~**R05**~~ — Enums typealias | ~~Haute~~ | - | **Done** |
| - | ~~**R07**~~ — Strategie enums | ~~Moyenne~~ | - | **Done** |

---

*Ce document est le point de depart pour la correction. Chaque TACHE-R0x peut etre traitee
comme une tache verticale selon la nouvelle approche documentee dans `docs/workflow-claude-code.md`.*
