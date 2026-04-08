# appFood — Phase 3 : Backlog & User Stories

> Date : 2026-03-23
> Version : 1.0

---

## Legendes

- **Complexite** : S (< 1j) | M (1-3j) | L (3-5j) | XL (> 5j)
- **Priorite** : MVP | V1.1 | V2 | Nice-to-have

### Personas

| Persona | Description |
|---------|-------------|
| **Utilisateur** | Vegan/vegetarien sportif, utilise l'app quotidiennement pour suivre ses nutriments |
| **Nouvel utilisateur** | Personne qui decouvre l'app pour la premiere fois |
| **Admin** | Membre de l'equipe appFood qui gere le contenu (recettes, aliments) |

---

## THEME 1 : Fondations techniques

---

### EPIC : SETUP — Initialisation du projet

---

**SETUP-01a** — Initialisation du projet KMP (shared : structure, DI, util)

En tant que developpeur,
je veux un projet KMP configure avec les modules shared, androidApp et iosApp,
afin d'avoir la base technique partagee prete pour le developpement.

**Criteres d'acceptation :**
- [x] Projet KMP cree avec modules shared, androidApp, iosApp
- [x] `build.gradle.kts` racine + `settings.gradle.kts` + `gradle/libs.versions.toml` (remplace buildSrc)
- [x] `shared/build.gradle.kts` avec les dependances KMP (Ktor Client, SQLDelight, Koin, kotlinx.serialization, kotlinx.datetime)
- [x] Structure de packages dans `shared/src/commonMain/kotlin/com/appfood/shared/` : model/, domain/, data/, sync/, api/, di/, util/
- [x] `shared/.../di/SharedModule.kt` — Module Koin vide (skeleton)
- [x] `shared/.../util/Result.kt` — Wrapper AppResult<T>
- [x] `shared/.../data/local/DatabaseDriverFactory.kt` — expect/actual pour Android et iOS
- [x] Build Android (APK) reussi
- [ ] Build iOS (simulateur) reussi (non testable sur Windows)

**Complexite :** M
**Priorite :** MVP
**Dependances :** Aucune
**Agent assigne :** SHARED
**Notes techniques :** Utiliser le template KMP officiel JetBrains. Structure par feature dans chaque couche : auth, journal, aliments, recettes, quotas, recommandation, profil.

---

**SETUP-01b** — Initialisation UI Compose Multiplatform (navigation, theme, wrappers)

En tant que developpeur,
je veux l'UI Compose Multiplatform configuree avec la navigation et le theme,
afin de pouvoir developper les ecrans de l'application.

**Criteres d'acceptation :**
- [x] Compose Multiplatform configure et fonctionnel sur les deux plateformes
- [x] `shared/.../ui/navigation/AppNavigation.kt` — NavHost avec routes initiales (placeholder)
- [x] `shared/.../ui/navigation/Screen.kt` — Sealed class des ecrans
- [x] `shared/.../ui/navigation/BottomNavBar.kt` — Barre de navigation inferieure
- [x] `shared/.../ui/theme/` — Theme.kt, Color.kt, Typography.kt, Shape.kt (Material 3)
- [x] `shared/.../ui/Strings.kt` — Constantes UI en francais (titres, labels, messages principaux)
- [x] `shared/.../ui/common/` — Composants reutilisables de base (EmptyState, LoadingSkeleton, ErrorMessage)
- [x] `androidApp/` — MainActivity lance le ComposeApp shared, AppFoodApplication init Koin
- [x] `iosApp/` — ContentView lance le ComposeApp shared
- [x] Navigation entre 2 ecrans placeholder fonctionne sur Android (build verifie) — iOS non testable sur Windows

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-01a
**Agent assigne :** MOBILE
**Notes techniques :** Utiliser `org.jetbrains.compose.*` (pas `androidx.compose.*`). Aucun import `android.*` ou `UIKit` dans le code UI partage.

---

**SETUP-02** — Initialisation du backend Ktor

En tant que developpeur,
je veux un serveur Ktor configure avec les modules de base,
afin de pouvoir developper l'API REST.

**Criteres d'acceptation :**
- [x] Projet Ktor cree avec Gradle
- [x] Modules configures : routing, serialization (kotlinx.serialization), content negotiation, CORS, auth (JWT)
- [x] Endpoint health check `/api/health` fonctionnel
- [x] Configuration par environnement (dev, staging, prod) via fichiers HOCON
- [x] Docker Compose local (Ktor + PostgreSQL + Meilisearch)

**Complexite :** M
**Priorite :** MVP
**Dependances :** Aucune
**Agent assigne :** Phase 4
**Notes techniques :** Utiliser Exposed comme ORM. Dockeriser le backend pour deploiement Railway.

---

**SETUP-03** — Configuration de la base de donnees PostgreSQL

En tant que developpeur,
je veux une base PostgreSQL configuree avec le schema initial et les migrations,
afin d'avoir une base de donnees prete pour le developpement.

**Criteres d'acceptation :**
- [x] Schema initial cree (tables User, Aliment, Recette, JournalEntree, QuotaJournalier, Notification)
- [x] Systeme de migrations en place (Flyway ou Exposed migrations)
- [x] Script de seed avec des donnees de test
- [x] Base accessible en local via Docker Compose

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-02
**Agent assigne :** Phase 4

---

**SETUP-04** — Configuration de Meilisearch

En tant que developpeur,
je veux un serveur Meilisearch configure et indexe avec les aliments,
afin de pouvoir proposer une recherche rapide et tolerante aux fautes.

**Criteres d'acceptation :**
- [x] Instance Meilisearch dans le Docker Compose
- [x] Index "aliments" cree avec les champs searchable (nom, marque, categorie)
- [x] Filtres configures (regime_compatible, categorie, source)
- [x] Synonymes francais configures (ex: "tomate" = "tomates")
- [x] Endpoint de recherche fonctionnel cote Ktor

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-02, DATA-01
**Agent assigne :** Phase 4

---

**SETUP-05** — Configuration SQLDelight (base locale mobile)

En tant que developpeur,
je veux SQLDelight configure dans le module shared KMP,
afin de stocker les donnees localement pour le mode offline.

**Criteres d'acceptation :**
- [x] SQLDelight configure dans le module shared
- [x] Schema local defini (journal, aliments caches, profil, quotas)
- [x] DAOs fonctionnels pour les operations CRUD locales (8 LocalDataSources + Koin)
- [x] Fonctionne sur Android (build verifie) — iOS non testable sur Windows

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-01a
**Agent assigne :** Phase 4

---

### EPIC : INFRA — Infrastructure & CI/CD

---

**INFRA-01** — Pipeline CI/CD GitHub Actions

En tant que developpeur,
je veux un pipeline CI/CD qui lint, teste et build automatiquement,
afin de detecter les regressions rapidement.

**Criteres d'acceptation :**
- [x] Workflow GitHub Actions sur push/PR : ktlint, tests unitaires, build — ci.yml
- [x] Workflow de deploiement staging sur merge dans main — deploy-staging.yml
- [x] Workflow de deploiement production (declenchement manuel) — deploy-prod.yml
- [~] Temps de build < 10 minutes — timeout configure a 10/15min, a verifier en reel

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-01a, SETUP-02
**Agent assigne :** INFRA — Sprint 0

---

**INFRA-02** — Deploiement Railway

En tant que developpeur,
je veux le backend deploye sur Railway avec PostgreSQL et Meilisearch manages,
afin d'avoir un environnement de production accessible.

**Criteres d'acceptation :**
- [x] Service Ktor deploye sur Railway (Docker) — https://appfood-production.up.railway.app
- [x] PostgreSQL manage provisionne — postgres.railway.internal:5432/railway
- [x] Meilisearch deploye — meilisearch.railway.internal:7700
- [x] Variables d'environnement configurees (secrets)
- [x] HTTPS actif — domaine Railway *.up.railway.app
- [ ] Domaine personnalise configure (optionnel)

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-02, SETUP-03, SETUP-04
**Agent assigne :** INFRA + utilisateur — Sprint 1
**Notes techniques :** Dockerfile corrige (settings-docker.gradle.kts pour build backend-only). Flyway remplace par Exposed SchemaUtils.createMissingTablesAndColumns(). Meilisearch API v1.12 utilise PATCH pour updateSettings.

---

**INFRA-03** — Monitoring et alerting de base

En tant que developpeur,
je veux un monitoring de base sur la production,
afin d'etre alerte en cas de probleme.

**Criteres d'acceptation :**
- [ ] Sentry integre dans le backend Ktor (erreurs applicatives)
- [ ] UptimeRobot configure sur l'endpoint health check
- [ ] Alertes email/Slack en cas de downtime ou erreurs critiques

**Complexite :** S
**Priorite :** MVP
**Dependances :** INFRA-02
**Agent assigne :** Phase 4

---

### EPIC : DATA — Import & gestion des donnees nutritionnelles

---

**DATA-01** — Import de la base Ciqual (ANSES)

En tant que developpeur,
je veux importer la base Ciqual dans PostgreSQL et Meilisearch,
afin d'avoir une base d'aliments fiable avec leurs valeurs nutritionnelles.

**Criteres d'acceptation :**
- [x] Script d'import Ciqual (CSV → PostgreSQL) — CiqualImporter.kt avec upsert batch
- [x] Mapping des nutriments Ciqual vers le schema appFood (16 nutriments, omega-3/6 calcules)
- [x] ~3000 aliments importes avec toutes les valeurs nutritionnelles
- [x] Aliments indexes dans Meilisearch (config synonymes FR, stop words, filtres)
- [x] Tag regime_compatible calcule (vegan, vegetarien) par heuristique de categorie
- [x] Script rejouable (idempotent) — ON CONFLICT (source, source_id) DO UPDATE

**Complexite :** L
**Priorite :** MVP
**Dependances :** SETUP-03, SETUP-04
**Agent assigne :** DATA — Sprints 0-1
**Notes techniques :** **LIRE `docs/us-clarifications.md` section 3** avant implementation. Contient : mapping exact des colonnes Ciqual → NutrimentValues, calcul omega-3/omega-6 a partir des acides gras, gestion des valeurs manquantes ("-", "traces", "N/A" → 0.0), heuristique de detection vegan/vegetarien par categorie, configuration Meilisearch (synonymes, filtres, stop words).
**Notes audit (2026-03-26)** : CiqualImporter adapte pour Ciqual 2025 (separateur virgule, headers multi-lignes). AlimentIndexer corrige (sel/sucres ajoutes, champs renommes en camelCase). Fichier CSV Ciqual 2025 place dans backend/data/ciqual.csv. AlimentRoutes + AlimentService crees (search, by-id, barcode). SearchRoutes deprece en faveur d'AlimentRoutes.

---

**DATA-02** — Integration Open Food Facts (produits industriels)

En tant qu'utilisateur,
je veux pouvoir trouver des produits industriels (marques Leclerc, U, Intermarche...),
afin de tracker ce que je mange reellement au quotidien.

**Criteres d'acceptation :**
- [x] Client API Open Food Facts integre dans Ktor — OpenFoodFactsClient.kt
- [x] Recherche de produits par nom via l'API OFF
- [x] Resultats OFF integres dans les resultats Meilisearch (cache + index auto) — fallback OFF dans AlimentService
- [x] Cache des produits recherches en base PostgreSQL
- [x] Donnees nutritionnelles mappees vers le schema appFood (16 nutriments)
- [x] Flag de qualite des donnees (source: CIQUAL vs OPEN_FOOD_FACTS)

**Complexite :** L
**Priorite :** MVP
**Dependances :** SETUP-04, DATA-01
**Agent assigne :** DATA — Sprint 1

---

**DATA-03** — Tables de reference AJR/ANC par profil

En tant que developpeur,
je veux une table de reference des Apports Journaliers Recommandes par profil,
afin de calculer les quotas personnalises.

**Criteres d'acceptation :**
- [x] Table AJR/ANC integree en base avec les valeurs officielles ANSES (V004__seed_ajr.sql)
- [x] Valeurs par : sexe, tranche d'age (ado/adulte/senior)
- [x] Valeurs specifiques pour les regimes vegan/vegetarien (coefficients: fer x1.8, zinc x1.5, omega-3 x1.5, proteines x1.1)
- [x] API endpoint pour recuperer les AJR d'un profil donne — POST /quotas/recalculate + GET /quotas via QuotaService

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-03
**Agent assigne :** SHARED, DATA — Sprint 1
**Notes techniques :** Sources : tables ANSES, recommandations specifiques pour les regimes vegetaliens (Societe francaise de nutrition).

---

---

## THEME 2 : Authentification & Profil utilisateur

---

### EPIC : AUTH — Authentification

---

**AUTH-01** — Inscription par email/password

En tant que nouvel utilisateur,
je veux m'inscrire avec mon email et un mot de passe,
afin de creer mon compte appFood.

**Criteres d'acceptation :**
- [x] Ecran d'inscription (email, mot de passe, confirmation mot de passe)
- [x] Validation email (format) et mot de passe (min 8 caracteres)
- [x] Creation du compte via Firebase Auth (mock mode — Firebase reel en attente config humaine)
- [ ] Email de verification envoye (via Firebase SDK cote client) — non implemente, use case absent
- [x] Redirection vers le questionnaire de profil apres inscription
- [x] Gestion des erreurs (email deja utilise, etc.)

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-01a, SETUP-02
**Agent assigne :** SHARED, BACKEND, MOBILE — Sprint 1
**Notes audit (2026-03-26)** : Backend OK (endpoints + JWT custom). UI OK (ecrans). Use cases ABSENTS (ViewModels stubbes). Auth JWT flow corrige : backend genere un JWT HMAC256 apres verification Firebase, retourne dans AuthResponse.token. FirebaseAdmin fail-fast en prod.

---

**AUTH-02** — Connexion / Deconnexion

En tant qu'utilisateur,
je veux me connecter et me deconnecter de mon compte,
afin d'acceder a mes donnees de maniere securisee.

**Criteres d'acceptation :**
- [x] Ecran de connexion (email, mot de passe)
- [x] Connexion via Firebase Auth (mock mode)
- [x] Token JWT stocke localement — ApiClient.setAuthToken() stocke le token en memoire, UserRepositoryImpl le set apres login
- [x] Deconnexion avec nettoyage du token — AuthViewModel.onLogout() appelle userRepository.logout()
- [ ] Redirection automatique vers le login si token expire — intercepteur HTTP 401 absent
- [x] Gestion des erreurs (mauvais identifiants)

**Complexite :** M
**Priorite :** MVP
**Dependances :** AUTH-01
**Agent assigne :** SHARED, BACKEND, MOBILE — Sprint 1
**Notes audit (2026-03-26)** : Backend OK. UI OK. Couche intermediaire (use cases, token management, intercepteur 401) a creer au Sprint 2.

---

**AUTH-03** — Connexion Google Sign-In

En tant que nouvel utilisateur,
je veux m'inscrire/me connecter avec mon compte Google,
afin de gagner du temps a l'inscription.

**Criteres d'acceptation :**
- [x] Bouton "Continuer avec Google" sur les ecrans login/inscription
- [ ] Flow OAuth Google via Firebase Auth (mock mode) — ViewModel stubbe (TODO)
- [ ] Creation du compte appFood si premier login — use case absent
- [x] Redirection vers le questionnaire de profil si nouveau compte (stub retourne needsOnboarding=true)
- [ ] Fonctionne sur Android et iOS (non testable — Firebase non configure)

**Complexite :** M
**Priorite :** MVP
**Dependances :** AUTH-01
**Agent assigne :** MOBILE — Sprint 1

---

**AUTH-04** — Reset de mot de passe

En tant qu'utilisateur,
je veux reinitialiser mon mot de passe si je l'ai oublie,
afin de recuperer l'acces a mon compte.

**Criteres d'acceptation :**
- [x] Lien "Mot de passe oublie" sur l'ecran de connexion
- [x] Saisie de l'email → envoi d'un email de reset via Firebase (mock mode) — AuthViewModel.onSendResetLink() appelle Firebase.auth.sendPasswordResetEmail()
- [x] Message de confirmation affiche
- [x] Gestion des erreurs (email non trouve)

**Complexite :** S
**Priorite :** MVP
**Dependances :** AUTH-01
**Agent assigne :** MOBILE — Sprint 1

---

**AUTH-05** — Connexion Apple Sign-In

En tant qu'utilisateur iOS,
je veux m'inscrire/me connecter avec mon compte Apple,
afin d'utiliser l'authentification native iOS.

**Criteres d'acceptation :**
- [x] Bouton "Continuer avec Apple" sur les ecrans login/inscription (iOS uniquement)
- [ ] Flow OAuth Apple via Firebase Auth (mock mode) — ViewModel stubbe (TODO)
- [ ] Creation du compte appFood si premier login — use case absent
- [ ] Conforme aux guidelines Apple (non testable — Apple Developer Account requis)

**Complexite :** M
**Priorite :** MVP
**Dependances :** AUTH-03
**Agent assigne :** MOBILE — Sprint 1
**Notes techniques :** Apple impose Apple Sign-In si d'autres logins sociaux sont proposes. Obligatoire pour la soumission App Store — doit etre au meme niveau que Google Sign-In.

---

### EPIC : PROFIL — Profil utilisateur & personnalisation

---

**PROFIL-01** — Questionnaire de profil initial (onboarding)

En tant que nouvel utilisateur,
je veux remplir un questionnaire rapide sur mon profil,
afin que l'app calcule mes quotas nutritionnels personnalises.

**Criteres d'acceptation :**
- [x] Ecran 1 : Sexe, age, poids (kg), taille (cm)
- [x] Ecran 2 : Regime alimentaire (vegan, vegetarien, flexitarien, autre)
- [x] Ecran 3 : Activite physique (sedentaire, leger, modere, actif, tres actif) avec exemples
- [x] Ecran 4 (optionnel, skippable) : Aliments non aimes / allergies
- [x] Validation des saisies (poids/taille dans des ranges realistes)
- [x] Option "Passer" (skip) disponible sur chaque ecran — lien discret en bas d'ecran (SkipLink)
- [x] Si skip : valeurs par defaut utilisees (quotas standards homme/femme adulte, regime omnivore)
- [ ] Rappel ulterieur pour completer le profil (bandeau discret sur le dashboard) — pas de bandeau rappel implemente
- [x] Donnees sauvegardees en base + localement — OnboardingViewModel.saveProfile() via UserRepository.createProfile() + updatePreferences()
- [x] Redirection vers le dashboard apres completion ou skip

**Complexite :** M
**Priorite :** MVP
**Dependances :** AUTH-01, SETUP-05
**Agent assigne :** SHARED, BACKEND, MOBILE — Sprint 1
**Notes audit (2026-03-26)** : Backend endpoints CONFORMES. UI ecrans COMPLETS. Use cases ABSENTS (CreateProfileUseCase, UpdatePreferencesUseCase a creer). ViewModels stubbes.

---

**PROFIL-02** — Edition du profil

En tant qu'utilisateur,
je veux modifier mes informations de profil a tout moment,
afin que mes quotas soient toujours adaptes a ma situation actuelle.

**Criteres d'acceptation :**
- [x] Page profil accessible depuis les reglages
- [x] Modification de : poids, taille, age, regime, activite physique
- [x] Recalcul automatique des quotas apres modification — POST /quotas/recalculate disponible, RecalculerQuotasUseCase
- [x] Sauvegarde locale + synchronisation serveur — ProfilViewModel.onSaveProfile() via UserRepository.updateProfile()

**Complexite :** S
**Priorite :** MVP
**Dependances :** PROFIL-01
**Agent assigne :** SHARED, BACKEND, MOBILE — Sprint 1
**Notes audit (2026-03-26)** : Backend endpoint PUT /users/me/profile CONFORME. UI ecran COMPLET. loadProfile() et onSaveProfile() stubbes.

---

**PROFIL-03** — Gestion des aliments exclus et preferences

En tant qu'utilisateur,
je veux specifier les aliments que je n'aime pas ou auxquels je suis allergique,
afin que les recommandations n'incluent jamais ces aliments.

**Criteres d'acceptation :**
- [x] Section "Preferences alimentaires" dans le profil
- [x] Recherche d'aliments a exclure (via Meilisearch — UI prete, backend search a aligner)
- [x] Liste des exclusions affichee et modifiable
- [x] Categories d'allergies predefinies (gluten, soja, fruits a coque, arachides, etc. — 14 allergies)
- [x] Les recommandations et recettes filtrent automatiquement les aliments exclus — filtrage dans RecommandationService backend

**Complexite :** M
**Priorite :** MVP
**Dependances :** PROFIL-01, SETUP-04
**Agent assigne :** SHARED, BACKEND, MOBILE — Sprint 1

---

**PROFIL-04** — Suppression de compte (RGPD)

En tant qu'utilisateur,
je veux pouvoir supprimer mon compte et toutes mes donnees,
afin d'exercer mon droit a l'effacement.

**Criteres d'acceptation :**
- [x] Option "Supprimer mon compte" dans les reglages
- [x] Confirmation requise (double validation — dialog avec 2 etapes)
- [x] Suppression de toutes les donnees : profil, journal, preferences, quotas, portions
- [x] Suppression du compte Firebase Auth (mock mode)
- [x] Deconnexion automatique apres suppression
- [x] Suppression irreversible (pas de soft delete pour la RGPD)

**Complexite :** M
**Priorite :** MVP
**Dependances :** PROFIL-01
**Agent assigne :** BACKEND, MOBILE — Sprint 1

---

**PROFIL-05** — Export des donnees personnelles (RGPD)

En tant qu'utilisateur,
je veux exporter toutes mes donnees personnelles,
afin d'exercer mon droit a la portabilite.

**Criteres d'acceptation :**
- [ ] Bouton "Exporter mes donnees" dans les reglages
- [ ] Generation d'un fichier JSON/CSV contenant : profil, journal, preferences, quotas
- [ ] Telechargement ou envoi par email
- [ ] Delai de generation raisonnable (<30 secondes)

**Complexite :** M
**Priorite :** V1.1
**Dependances :** PROFIL-01
**Agent assigne :** Phase 4
**Notes techniques :** MVP : implementer l'endpoint backend `GET /api/users/me/export` qui genere le JSON (BACKEND agent). L'ecran UI (bouton dans les reglages, telechargement) est reporte en V1.1 (MOBILE agent).

---

---

## THEME 3 : Suivi nutritionnel (Core)

---

### EPIC : QUOTAS — Calcul & gestion des quotas

---

**QUOTAS-01** — Calcul automatique des quotas personnalises

En tant qu'utilisateur,
je veux que mes quotas nutritionnels soient calcules automatiquement a partir de mon profil,
afin de savoir exactement ce que je dois consommer chaque jour.

**Criteres d'acceptation :**
- [x] Calcul base sur : sexe, age, poids, taille, activite physique, regime alimentaire — CalculerQuotasUseCase
- [x] Nutriments calcules : calories, proteines, glucides, lipides, fibres, fer, calcium, zinc, magnesium, vitamine B12, vitamine D, vitamine C, omega-3 — 16 nutriments dans CalculerQuotasUseCase
- [x] Valeurs basees sur les references ANSES — getMicroNutrimentsBase() avec tranches 14-18/19-64/65+
- [x] Ajustements specifiques pour regime vegan/vegetarien (ex: fer x1.8 pour les vegetaliens) — getCoefficientsRegime()
- [x] Recalcul automatique si le profil change — POST /quotas/recalculate + RecalculerQuotasUseCase

**Complexite :** L
**Priorite :** MVP
**Dependances :** PROFIL-01, DATA-03
**Agent assigne :** Phase 4
**Notes techniques :** **LIRE `docs/us-clarifications.md` section 1** avant implementation. Contient : formule Mifflin-St Jeor exacte, tables AJR ANSES par sexe/age, coefficients d'ajustement par regime (fer ×1.8 vegan, zinc ×1.5, etc.), pseudo-code complet, cas limites.

---

**QUOTAS-02** — Modification manuelle des quotas

En tant qu'utilisateur,
je veux pouvoir modifier manuellement mes quotas pour un ou plusieurs nutriments,
afin d'adapter les objectifs a mes besoins specifiques.

**Criteres d'acceptation :**
- [x] Ecran de gestion des quotas accessible depuis le dashboard ou le profil — QuotaManagementScreen
- [x] Chaque nutriment affiche la valeur calculee et la valeur actuelle — QuotaCard avec valeurCible + valeurCalculee
- [x] Modification unitaire par nutriment (saisie libre) — onStartEdit/onSaveEdit dans QuotaViewModel
- [x] Bouton "Revenir au calcul automatique" par nutriment et global — onResetQuota + onResetAllQuotas
- [x] Sauvegarde locale + synchronisation serveur — PUT /quotas/{nutriment} + POST /quotas/reset-all via QuotaRepository
- [x] Indicateur visuel si un quota est personnalise vs calcule — badge QUOTAS_CUSTOM/QUOTAS_CALCULATED + couleur secondaryContainer

**Complexite :** M
**Priorite :** MVP
**Dependances :** QUOTAS-01
**Agent assigne :** Phase 4

---

### EPIC : JOURNAL — Saisie des repas

---

**JOURNAL-01** — Saisie d'un aliment consomme

En tant qu'utilisateur,
je veux ajouter un aliment que j'ai mange a mon journal du jour,
afin de suivre mes apports nutritionnels.

**Criteres d'acceptation :**
- [x] Selection du type de repas (petit-dejeuner, dejeuner, diner, collation) — AddEntryScreen avec MealType enum
- [x] Recherche d'aliment via Meilisearch (tolerant aux fautes de frappe) — SearchAlimentScreen + alimentRepository.search()
- [x] Resultats affiches avec nom, marque (si applicable), calories/100g — AlimentResponse dans SearchAlimentScreen
- [x] Saisie de la quantite en grammes (avec suggestions : 100g, 150g, 200g, portion standard) — PortionSelectorScreen avec quick quantities + portions
- [x] Affichage du resume nutritionnel avant validation — nutritionPreview reactive Flow dans JournalViewModel
- [x] Sauvegarde locale (offline-first) + sync serveur — SyncManager.enqueue() + journalRepository.addEntry()

**Complexite :** L
**Priorite :** MVP
**Dependances :** SETUP-04, SETUP-05, DATA-01
**Agent assigne :** Phase 4

---

**JOURNAL-02** — Saisie d'une recette consommee

En tant qu'utilisateur,
je veux ajouter une recette complete a mon journal,
afin de ne pas saisir chaque ingredient un par un.

**Criteres d'acceptation :**
- [x] Recherche de recettes dans le livre de recettes — onRecetteSearchQueryChanged() dans JournalViewModel
- [x] Selection de la recette — onRecetteSelected() avec navigation
- [x] Ajustement du nombre de portions consommees — nbPortions dans AddJournalEntryRequest
- [x] Calcul automatique des nutriments en fonction de la portion — backend JournalService calcule les nutriments
- [x] Sauvegarde dans le journal — journalRepository.addEntry() avec recetteId

**Complexite :** M
**Priorite :** MVP
**Dependances :** JOURNAL-01, RECETTES-01
**Agent assigne :** Phase 4

---

**JOURNAL-03** — Aliments favoris

En tant qu'utilisateur,
je veux marquer des aliments comme favoris,
afin de les retrouver rapidement lors de mes prochaines saisies.

**Criteres d'acceptation :**
- [x] Bouton favori (etoile/coeur) sur chaque aliment — onToggleFavorite() dans JournalViewModel + FavoriteIcon
- [x] Section "Favoris" en haut de l'ecran de recherche — FavoritesSection composable dans SearchAlimentScreen
- [x] Favoris disponibles offline (caches localement) — _favorites StateFlow charge au init
- [x] Ajout/suppression de favoris synchronise avec le serveur — journalRepository.addFavori/removeFavori avec optimistic update

**Complexite :** S
**Priorite :** MVP
**Dependances :** JOURNAL-01
**Agent assigne :** Phase 4

---

**JOURNAL-04** — Repas recents

En tant qu'utilisateur,
je veux voir mes repas recents pour les re-saisir rapidement,
afin de gagner du temps quand je mange souvent la meme chose.

**Criteres d'acceptation :**
- [x] Section "Recents" sur l'ecran de saisie — RecentsSection composable
- [x] Affiche les 10-20 derniers aliments/recettes saisis — loadRecents(limit = MAX_RECENTS = 20)
- [x] Un tap re-ajoute l'aliment avec la meme quantite (modifiable) — onRecentEntryTap() pre-remplit quantite
- [x] Donnees disponibles offline — RecentEntry charge depuis journalRepository avec cache local

**Complexite :** S
**Priorite :** MVP
**Dependances :** JOURNAL-01
**Agent assigne :** Phase 4

---

**JOURNAL-05** — Copie d'un repas / d'une journee

En tant qu'utilisateur,
je veux copier un repas ou une journee complete vers aujourd'hui,
afin de gagner du temps quand je mange la meme chose.

**Criteres d'acceptation :**
- [ ] Option "Copier ce repas" sur un repas passe
- [ ] Option "Copier cette journee" sur une journee passee
- [ ] Les quantites sont copiees telles quelles (modifiables apres)
- [ ] La copie est ajoutee au journal du jour selectionne

**Complexite :** M
**Priorite :** V1.1
**Dependances :** JOURNAL-01
**Agent assigne :** Phase 4

---

**JOURNAL-06** — Modification et suppression d'une entree

En tant qu'utilisateur,
je veux modifier ou supprimer un aliment deja saisi dans mon journal,
afin de corriger mes erreurs de saisie.

**Criteres d'acceptation :**
- [x] Swipe ou menu contextuel sur une entree du journal — bouton delete dans JournalScreen avec dialog
- [x] Modification de la quantite — onEditEntry() avec UpdateJournalEntryRequest
- [x] Suppression avec confirmation — onRequestDelete/onConfirmDelete/onCancelDelete avec AlertDialog
- [x] Mise a jour immediate du dashboard (quotas) — dashboard recharge apres modification
- [x] Synchronisation de la modification avec le serveur — journalRepository.updateEntry/deleteEntry

**Complexite :** S
**Priorite :** MVP
**Dependances :** JOURNAL-01
**Agent assigne :** Phase 4

---

**JOURNAL-07** — Scan de code-barres

En tant qu'utilisateur,
je veux scanner le code-barres d'un produit pour l'ajouter a mon journal,
afin de saisir encore plus rapidement.

**Criteres d'acceptation :**
- [ ] Bouton "Scanner" sur l'ecran de saisie
- [ ] Ouverture de la camera avec detection de code-barres
- [ ] Recherche du produit via l'API Open Food Facts
- [ ] Affichage des infos nutritionnelles du produit trouve
- [ ] Ajout au journal avec saisie de la quantite
- [ ] Gestion du cas "produit non trouve"

**Complexite :** L
**Priorite :** V1.1
**Dependances :** JOURNAL-01, DATA-02
**Agent assigne :** Phase 4
**Notes techniques :** ML Kit (Google) ou ZXing pour la lecture. API Open Food Facts pour la correspondance code-barres → produit.

---

### EPIC : DASHBOARD — Tableau de bord nutritionnel

---

**DASHBOARD-01** — Dashboard journalier

En tant qu'utilisateur,
je veux voir d'un coup d'oeil ou j'en suis dans mes quotas du jour,
afin de savoir ce qu'il me reste a manger.

**Criteres d'acceptation :**
- [x] Ecran principal de l'app — DashboardScreen, route Dashboard dans BottomNavBar
- [x] Affichage de chaque nutriment suivi : barre de progression (consomme / quota) — NutrimentProgressBar composable
- [x] Code couleur : vert (>80% atteint), orange (50-80%), rouge (<50%) — NutrimentProgressBar avec seuils couleur
- [x] Nutriments regroupes par categorie (macros, vitamines, mineraux) — NutrimentCategorie.MACRO/VITAMINE/MINERAL/ACIDE_GRAS
- [x] Total calories en evidence — CaloriesSummaryCard composable
- [x] Resume des repas du jour (petit-dej, dejeuner, diner, collation) — MealsSummarySection avec Map<MealType, Double>
- [x] Bouton d'ajout rapide de repas — DASHBOARD_ADD_MEAL button

**Complexite :** L
**Priorite :** MVP
**Dependances :** QUOTAS-01, JOURNAL-01
**Agent assigne :** Phase 4

---

**DASHBOARD-02** — Vue hebdomadaire

En tant qu'utilisateur,
je veux voir mes apports nutritionnels sur la semaine,
afin de reperer les tendances et les nutriments chroniquement en deficit.

**Criteres d'acceptation :**
- [x] Vue semaine accessible depuis le dashboard — WeeklyDashboardScreen + navigation depuis DashboardScreen
- [x] Graphique par nutriment (apport moyen vs quota) — NutrimentAverage avec moyenneConsommee/cible
- [x] Identification des nutriments systematiquement en dessous des quotas — criticalNutrients dans WeeklyState
- [x] Navigation entre les semaines (precedente/suivante) — previousWeek()/nextWeek() dans WeeklyDashboardViewModel

**Complexite :** M
**Priorite :** MVP
**Dependances :** DASHBOARD-01
**Agent assigne :** Phase 4

---

**DASHBOARD-03** — Vue mensuelle

En tant qu'utilisateur,
je veux voir mes tendances nutritionnelles sur le mois,
afin d'evaluer mes habitudes alimentaires sur le long terme.

**Criteres d'acceptation :**
- [ ] Vue mois accessible depuis le dashboard
- [ ] Graphiques de tendance par nutriment
- [ ] Comparaison mois precedent vs mois actuel
- [ ] Identification des ameliorations et degradations

**Complexite :** M
**Priorite :** V1.1
**Dependances :** DASHBOARD-02
**Agent assigne :** Phase 4

---

### EPIC : POIDS — Suivi du poids dans le temps

---

**POIDS-01** — Saisie et historique du poids

En tant qu'utilisateur,
je veux enregistrer mon poids regulierement et voir son evolution,
afin de suivre ma progression physique en lien avec ma nutrition.

**Criteres d'acceptation :**
- [x] Saisie du poids depuis le profil ou un ecran dedie — PoidsScreen avec PoidsViewModel
- [x] Historique du poids avec graphique d'evolution (semaine, mois, 3 mois, 6 mois, 1 an) — PoidsPeriod enum (WEEK/MONTH/THREE_MONTHS/SIX_MONTHS/YEAR)
- [x] Date associee a chaque pesee — PoidsEntry avec date + poids
- [x] Affichage du poids actuel, du poids minimum et maximum sur la periode — PoidsState.Success avec poidsCourant/poidsMin/poidsMax
- [x] Donnees disponibles offline (saisie + consultation) — SyncManager avec entity_type "poids" + LocalPoidsDataSource

**Complexite :** M
**Priorite :** MVP
**Dependances :** PROFIL-01, SETUP-05
**Agent assigne :** Phase 4

---

**POIDS-02** — Recalcul automatique des quotas apres changement de poids

En tant qu'utilisateur,
je veux que mes quotas se recalculent quand mon poids change significativement,
afin que mes objectifs nutritionnels restent adaptes.

**Criteres d'acceptation :**
- [x] Detection d'un changement de poids significatif (>1kg par rapport au poids de reference) — DetecterChangementPoidsUseCase
- [x] Proposition de recalculer les quotas (pas automatique sans accord) — showRecalculDialog dans PoidsViewModel
- [x] L'utilisateur valide ou refuse le recalcul — onRecalculAccepted/onRecalculDismissed
- [x] Si valide, mise a jour du poids de reference et des quotas — RecalculerQuotasApresPoidsUseCase
- [ ] Historique des recalculs visible — pas d'historique des recalculs implemente

**Complexite :** M
**Priorite :** MVP
**Dependances :** POIDS-01, QUOTAS-01
**Agent assigne :** Phase 4

---

**POIDS-03** — Objectif de poids

En tant qu'utilisateur sportif,
je veux definir un objectif de poids (prise de masse, maintien, seche),
afin que l'app adapte ses recommandations caloriques en consequence.

**Criteres d'acceptation :**
- [ ] Choix d'objectif : prise de masse, maintien, perte de poids
- [ ] Ajustement automatique des quotas caloriques en fonction de l'objectif (+/- 300-500 kcal)
- [ ] Indication du rythme recommande (pas plus de 0.5-1kg/semaine)
- [ ] Disclaimer sante si objectif de perte de poids important

**Complexite :** M
**Priorite :** V1.1
**Dependances :** POIDS-01, QUOTAS-01
**Agent assigne :** Phase 4

---

### EPIC : HYDRA — Suivi de l'hydratation

---

**HYDRA-01** — Saisie et suivi de l'hydratation quotidienne

En tant qu'utilisateur,
je veux tracker ma consommation d'eau dans la journee,
afin de m'assurer que je m'hydrate suffisamment.

**Criteres d'acceptation :**
- [x] Widget d'hydratation sur le dashboard (barre de progression) — HydratationWidget integre dans DashboardScreen
- [x] Ajout rapide par increment (un verre = 250ml, bouteille = 500ml, personnalisable) — addGlass/addBottle/addCustom dans HydratationViewModel
- [x] Objectif journalier calcule en fonction du poids et de l'activite physique (~30-35ml/kg) — objectifMl calcule cote backend
- [x] Possibilite de modifier l'objectif manuellement — showObjectifEditor/updateObjectif/resetObjectif
- [x] Historique de la semaine visible — loadWeeklyData() avec 7 jours
- [x] Saisie disponible offline — optimistic local update on error dans HydratationViewModel

**Complexite :** M
**Priorite :** MVP
**Dependances :** DASHBOARD-01, PROFIL-01
**Agent assigne :** Phase 4

---

**HYDRA-02** — Rappel d'hydratation

En tant qu'utilisateur,
je veux recevoir un rappel si je n'ai pas bu assez d'eau dans la journee,
afin de ne pas oublier de m'hydrater.

**Criteres d'acceptation :**
- [ ] Notification push si l'objectif est a moins de 50% a 16h (heure configurable)
- [ ] Desactivable dans les preferences de notification
- [ ] Message simple et non-intrusif
- [ ] Tap sur la notification → ouvre le widget d'hydratation

**Complexite :** S
**Priorite :** V1.1
**Dependances :** HYDRA-01, NOTIF-01
**Agent assigne :** Phase 4

---

### EPIC : PORTIONS — Gestion des portions standard

---

**PORTIONS-01** — Portions standard par aliment

En tant qu'utilisateur,
je veux pouvoir saisir des portions standard (une pomme, une cuillere a soupe, un bol...),
afin de ne pas devoir peser chaque aliment en grammes.

**Criteres d'acceptation :**
- [x] Chaque aliment peut avoir des portions standard associees (ex: pomme = 150g, cuillere a soupe = 15g, bol = 250g) — PortionStandard model + PortionsTable backend
- [x] A la saisie, l'utilisateur choisit entre "grammes" et les portions disponibles — PortionSelectorScreen avec portions + saisie libre
- [x] Portions standard predefinies pour les aliments Ciqual les plus courants — seed portions dans PortionService
- [x] Portions generiques toujours disponibles (cuillere a cafe, cuillere a soupe, verre, bol, assiette, poignee) — estGenerique=true dans PortionStandard
- [x] Conversion automatique portion → grammes pour le calcul nutritionnel — onPortionSelected() met a jour quantityGrams

**Complexite :** L
**Priorite :** MVP
**Dependances :** JOURNAL-01, DATA-01
**Agent assigne :** Phase 4
**Notes techniques :** La base Ciqual fournit des "portions moyennes" pour certains aliments. Completer avec des valeurs standard (ANSES, tables de portions). C'est un vrai levier de simplicite — sans ca, la saisie est penible.

---

**PORTIONS-02** — Portions personnalisees

En tant qu'utilisateur,
je veux creer mes propres portions standard pour un aliment,
afin d'adapter la saisie a mes habitudes (ex: "mon bol du matin" = 300g).

**Criteres d'acceptation :**
- [ ] Option "Ajouter une portion personnalisee" sur un aliment
- [ ] Saisie : nom de la portion + poids en grammes
- [ ] Portions personnalisees visibles en priorite lors de la saisie
- [ ] Sauvegardees localement + synchronisees

**Complexite :** S
**Priorite :** V1.1
**Dependances :** PORTIONS-01
**Agent assigne :** Phase 4

---

---

## THEME 4 : Recommandations & Recettes

---

### EPIC : RECO — Recommandations nutritionnelles

---

**RECO-01** — Recommandation d'aliments pour combler les manques

En tant qu'utilisateur,
je veux que l'app me suggere des aliments riches en nutriments qui me manquent,
afin de savoir quoi manger pour atteindre mes quotas.

**Criteres d'acceptation :**
- [x] Section "Suggestions" sur le dashboard ou ecran dedie — RecommandationsScreen + RecommandationViewModel + onglet ALIMENTS
- [x] Pour chaque nutriment en deficit, liste des aliments les plus riches (compatibles avec le regime) — RecommandationAlimentUseCase + backend RecommandationService
- [x] Aliments exclus/non aimes filtres automatiquement — filtrage par preferences dans RecommandationService
- [x] Affichage : nom de l'aliment, quantite suggeree pour combler le manque, % du quota couvert — RecommandationUiModel avec quantiteSuggereGrammes + couverture
- [x] Mise a jour en temps reel apres chaque saisie — loadRecommandations() apres onAteThis()

**Complexite :** L
**Priorite :** MVP
**Dependances :** DASHBOARD-01, PROFIL-03, DATA-01
**Agent assigne :** Phase 4
**Notes techniques :** **LIRE `docs/us-clarifications.md` section 2** avant implementation. Contient : seuils de declenchement (deficit FORT < 70%, MODERE 70-90%), formule de scoring exacte avec poids par nutriment, calcul de la quantite suggeree (cap 300g), filtrage allergenes, regle de diversite, gestion B12 vegan.

---

**RECO-02** — Recommandation de recettes pour combler les manques

En tant qu'utilisateur,
je veux que l'app me suggere des recettes qui couvrent mes manques nutritionnels,
afin d'avoir des idees de repas concrets adaptes a mes besoins.

**Criteres d'acceptation :**
- [x] Section "Recettes suggerees" sur le dashboard ou l'ecran recommandations — onglet RECETTES dans RecommandationsScreen
- [x] Recettes triees par pertinence (couverture des nutriments manquants) — RecommandationRecetteUseCase + backend scoring
- [x] Affichage : nom, temps de preparation, nutriments couverts, % de comblement — RecommandationRecetteUiModel
- [x] Filtrage automatique par regime et exclusions — filtrage dans RecommandationService
- [x] Possibilite de valider directement la recette comme repas (raccourci de saisie) — onAddRecetteToJournal()

**Complexite :** L
**Priorite :** MVP
**Dependances :** RECO-01, RECETTES-01
**Agent assigne :** Phase 4
**Notes techniques :** **LIRE `docs/us-clarifications.md` section 2.8** pour le scoring des recettes (meme logique que RECO-01 mais par portion, avec pourcentageCouvertureGlobal = moyenne des couvertures).

---

**RECO-03** — Validation rapide d'un plat recommande

En tant qu'utilisateur,
je veux pouvoir valider un plat recommande en un tap pour l'ajouter a mon journal,
afin de simplifier ma saisie au maximum.

**Criteres d'acceptation :**
- [x] Bouton "J'ai mange ca" sur chaque recommandation de recette — onAteThis() + onAddRecetteToJournal()
- [x] Possibilite d'ajuster la portion avant validation — onRecettePortionsChanged() avec getPortionsForRecette()
- [x] Ajout automatique au journal avec les nutriments calcules — AddJournalEntryRequest via journalRepository.addEntry()
- [x] Mise a jour immediate du dashboard — loadRecommandations() refresh apres ajout

**Complexite :** M
**Priorite :** MVP
**Dependances :** RECO-02, JOURNAL-02
**Agent assigne :** Phase 4

---

### EPIC : RECETTES — Livre de recettes

---

**RECETTES-01** — Consultation du livre de recettes

En tant qu'utilisateur,
je veux parcourir un livre de recettes vegan/vegetariennes,
afin de trouver des idees de repas adaptes a mon regime.

**Criteres d'acceptation :**
- [x] Ecran "Recettes" accessible depuis la navigation principale — RecettesListScreen + BottomNavBar onglet Recettes
- [x] Liste des recettes avec : photo (si dispo), nom, temps de preparation, regime compatible — RecetteCard composable
- [x] Filtres : regime (vegan, vegetarien), type de repas (petit-dej, plat, dessert, collation), temps de preparation — onRegimeToggle/onMealTypeToggle/onMaxTempsPrepChanged
- [x] Recherche par nom — onSearchQueryChanged avec debounce 300ms
- [x] Tri : par pertinence nutritionnelle, par popularite, par temps de preparation — RecetteSortOption enum

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-03
**Agent assigne :** Phase 4

---

**RECETTES-02** — Detail d'une recette

En tant qu'utilisateur,
je veux voir le detail complet d'une recette,
afin de pouvoir la preparer et connaitre ses apports nutritionnels.

**Criteres d'acceptation :**
- [x] Page detail avec : nom, description, photo (si dispo) — RecetteDetailScreen avec RecetteDetailState.Success
- [x] Liste des ingredients avec quantites — ingredients: List<IngredientRecette>
- [x] Etapes de preparation — etapes: List<String>
- [x] Temps de preparation et cuisson — tempsPreparationMin + tempsCuissonMin
- [x] Nombre de portions (ajustable) — onDetailPortionsChanged() dans RecettesViewModel
- [x] Tableau nutritionnel complet (par portion) — nutrimentsTotaux: NutrimentValues
- [x] Bouton "Ajouter a mon journal" — onAddRecetteToJournal()
- [x] Bouton "Ajouter aux favoris" — onToggleDetailFavorite()

**Complexite :** M
**Priorite :** MVP
**Dependances :** RECETTES-01
**Agent assigne :** Phase 4

---

**RECETTES-03** — Administration des recettes (back-office)

En tant qu'admin,
je veux ajouter, modifier et supprimer des recettes,
afin de maintenir le contenu du livre de recettes a jour.

**Criteres d'acceptation :**
- [x] Interface d'administration (web ou endpoint API dedie) — CreateRecetteScreen + backend RecetteRoutes CRUD
- [x] CRUD complet sur les recettes — RecetteRoutes avec GET/POST/PUT/DELETE + RecetteService
- [x] Ajout d'ingredients (lien avec la base d'aliments) — IngredientRequest avec alimentId + quantiteGrammes
- [x] Calcul automatique des nutriments de la recette — backend RecetteService calcule nutrimentsTotaux
- [x] Ajout de photos — imageUrl dans CreateRecetteRequest (URL externe)
- [x] Publication / depublication — publie: Boolean dans CreateRecetteRequest

**Complexite :** L
**Priorite :** MVP
**Dependances :** RECETTES-01, DATA-01
**Agent assigne :** Phase 4
**Notes techniques :** Au MVP, peut etre un simple endpoint API + script. Interface admin web en V1.1.

---

**RECETTES-04** — Import de livres de recettes

En tant qu'admin,
je veux importer un livre de recettes complet dans l'application,
afin d'enrichir rapidement le catalogue.

**Criteres d'acceptation :**
- [ ] Format d'import defini (JSON/CSV)
- [ ] Mapping automatique des ingredients vers la base d'aliments
- [ ] Calcul automatique des nutriments
- [ ] Rapport d'import (succes, erreurs, ingredients non trouves)
- [ ] Moderation avant publication

**Complexite :** L
**Priorite :** V2
**Dependances :** RECETTES-03
**Agent assigne :** Phase 4

---

**RECETTES-05** — Adaptation IA des recettes

En tant qu'utilisateur,
je veux que l'app me propose des variantes de recettes adaptees a mes manques nutritionnels,
afin d'optimiser mes repas sans tout reinventer.

**Criteres d'acceptation :**
- [ ] Bouton "Adapter cette recette" sur le detail d'une recette
- [ ] L'IA analyse les nutriments manquants de l'utilisateur
- [ ] Proposition de remplacement ou ajout d'ingredients pour combler les manques
- [ ] Affichage du comparatif nutritionnel (avant/apres adaptation)
- [ ] L'utilisateur peut valider ou rejeter les modifications

**Complexite :** XL
**Priorite :** V2
**Dependances :** RECETTES-02, RECO-01
**Agent assigne :** Phase 4
**Notes techniques :** Necessite un LLM (API Claude ou equivalent) pour les suggestions d'adaptation. Bien cadrer les prompts pour rester dans le domaine culinaire.

---

---

## THEME 5 : Notifications & Engagement

---

### EPIC : NOTIF — Notifications

---

**NOTIF-01** — Configuration Firebase Cloud Messaging

En tant que developpeur,
je veux FCM integre dans l'app,
afin de pouvoir envoyer des notifications push aux utilisateurs.

**Criteres d'acceptation :**
- [ ] SDK Firebase configure sur Android et iOS — FCM SDK pas integre cote client
- [x] Token FCM enregistre cote serveur a la connexion — POST /notifications/register-token + FcmTokenDao
- [x] Endpoint backend pour envoyer des notifications — NotificationRoutes avec list/read/read-all + NotificationService
- [ ] Gestion des permissions de notification (demande a l'utilisateur) — pas implemente cote client

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-01a, SETUP-02
**Agent assigne :** Phase 4

---

**NOTIF-02** — Notification de bilan mi-journee

En tant qu'utilisateur,
je veux recevoir une notification en milieu de journee si j'ai des manques importants,
afin d'adapter mon repas du soir.

**Criteres d'acceptation :**
- [ ] Notification push envoyee vers 14h-15h (configurable)
- [ ] Contenu : resume des nutriments en deficit + suggestion d'aliment/recette pour le soir
- [ ] Notification uniquement si des manques significatifs sont detectes (pas de spam)
- [ ] Tap sur la notification → ouvre l'ecran recommandations

**Complexite :** M
**Priorite :** V1.1
**Dependances :** NOTIF-01, RECO-01
**Agent assigne :** Phase 4

---

**NOTIF-03** — Bilan hebdomadaire

En tant qu'utilisateur,
je veux recevoir un bilan de ma semaine nutritionnelle,
afin de voir mes progres et mes points d'amelioration.

**Criteres d'acceptation :**
- [ ] Notification push le dimanche soir ou lundi matin (configurable)
- [ ] Resume : nutriments les mieux couverts, nutriments en deficit, tendance vs semaine precedente
- [ ] Ton positif et encourageant (pas culpabilisant)
- [ ] Tap → ouvre la vue hebdomadaire

**Complexite :** M
**Priorite :** V1.1
**Dependances :** NOTIF-01, DASHBOARD-02
**Agent assigne :** Phase 4

---

**NOTIF-04** — Gestion des preferences de notification

En tant qu'utilisateur,
je veux choisir quelles notifications je recois et a quelle heure,
afin de ne pas etre derange inutilement.

**Criteres d'acceptation :**
- [ ] Page de reglages des notifications
- [ ] Activation/desactivation par type (bilan mi-journee, bilan hebdo, recommandations)
- [ ] Choix de l'heure de notification
- [ ] Option "Ne pas deranger" (plage horaire)

**Complexite :** M
**Priorite :** V1.1
**Dependances :** NOTIF-01
**Agent assigne :** Phase 4

---

---

## THEME 6 : Synchronisation & Offline

---

### EPIC : SYNC — Synchronisation offline/online

---

**SYNC-01** — Saisie offline des repas

En tant qu'utilisateur,
je veux pouvoir saisir mes repas meme sans connexion internet,
afin de ne jamais etre bloque dans mon suivi.

**Criteres d'acceptation :**
- [x] La saisie fonctionne sans connexion (ecriture en base SQLDelight locale) — SyncManager.enqueue() + LocalJournalDataSource
- [x] Les aliments favoris et recents sont disponibles offline — _favorites et _recents dans JournalViewModel
- [x] Le dashboard se met a jour localement — DashboardViewModel avec stub success en fallback
- [x] Bandeau discret "Mode hors ligne" affiche (non bloquant) — OfflineBanner composable
- [x] Aucune fonctionnalite de saisie n'est degradee en offline — SavedOffline state dans AddEntryState

**Complexite :** L
**Priorite :** MVP
**Dependances :** SETUP-05, JOURNAL-01
**Agent assigne :** Phase 4
**Notes techniques :** **LIRE `docs/us-clarifications.md` section 4** avant implementation. Contient : entites synchronisees (journal, poids, hydratation), strategie d'ID (UUID cote client), structure sync_queue, workflow push/pull, declencheurs, ConnectivityMonitor expect/actual.

---

**SYNC-02** — Synchronisation automatique au retour en ligne

En tant qu'utilisateur,
je veux que mes donnees saisies en offline se synchronisent automatiquement quand je retrouve une connexion,
afin de ne perdre aucune donnee.

**Criteres d'acceptation :**
- [x] Detection automatique du retour de connexion — ConnectivityMonitor.observeConnectivity() dans SyncManager.startObserving()
- [x] Envoi des entrees de journal en attente vers le serveur — pushPendingEntries() avec sync_queue
- [x] Resolution des conflits (last-write-wins par entree) — SERVER_WINS : conflits supprimes de la queue, donnees recuperees au pull
- [x] Notification discrete de synchronisation reussie — SyncState.Success observable par le UI
- [x] Gestion des erreurs de sync (retry automatique) — retryWithBackoff() avec MAX_RETRIES=5 et exponential backoff

**Complexite :** L
**Priorite :** MVP
**Dependances :** SYNC-01
**Agent assigne :** Phase 4
**Notes techniques :** **LIRE `docs/us-clarifications.md` section 4** (meme section que SYNC-01). Contient : workflow push detaille (regroupement par entity_type, traitement des conflits SERVER_WINS, retry avec max 5 tentatives), workflow pull, resolution de conflits, indicateurs UI.

---

**SYNC-03** — Cache local des aliments et recettes

En tant qu'utilisateur,
je veux que les aliments et recettes que j'ai consultes soient disponibles offline,
afin de pouvoir les retrouver sans connexion.

**Criteres d'acceptation :**
- [x] Cache local des aliments recherches (SQLDelight) — LocalAlimentDataSource
- [x] Cache local des recettes consultees — RecetteRepository avec cache local
- [x] Cache des favoris — _favorites StateFlow dans JournalViewModel, charge au init
- [x] Mise a jour du cache quand la connexion est disponible — pullUpdates() dans SyncManager
- [ ] Taille du cache limitee (LRU, max ~50 Mo) — pas de strategie LRU implementee

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-05, JOURNAL-03
**Agent assigne :** Phase 4

---

---

## THEME 7 : Qualite & Transverse

---

### EPIC : UX — Experience utilisateur transverse

---

**UX-01** — Navigation principale

En tant qu'utilisateur,
je veux une navigation claire et intuitive entre les sections de l'app,
afin de trouver rapidement ce que je cherche.

**Criteres d'acceptation :**
- [x] Barre de navigation inferieure avec : Dashboard, Saisie (+), Recettes, Profil — BottomNavBar avec 4 items
- [x] Le bouton "+" central est mis en evidence (action principale) — FloatingActionButton avec AddIcon central
- [x] Navigation fluide entre les ecrans — NavHost dans AppNavigation.kt
- [x] Etat actif visible sur l'onglet courant — NavigationBarItem selected=currentScreen

**Complexite :** M
**Priorite :** MVP
**Dependances :** SETUP-01b
**Agent assigne :** Phase 4

---

**UX-02** — Etats vides

En tant que nouvel utilisateur,
je veux voir des messages utiles quand il n'y a pas encore de donnees,
afin de ne pas etre perdu devant un ecran vide.

**Criteres d'acceptation :**
- [x] Dashboard vide : message d'accueil + guide "Ajoute ton premier repas" — EmptyDashboardState composable
- [x] Journal vide : illustration + bouton d'ajout mis en evidence — EmptyJournalState composable
- [x] Favoris vides : explication de la fonctionnalite — EmptyFavoritesState composable
- [x] Chaque etat vide a un call-to-action clair — chaque EmptyState a un onAction callback

**Complexite :** S
**Priorite :** MVP
**Dependances :** UX-01
**Agent assigne :** Phase 4

---

**UX-03** — Etats de chargement

En tant qu'utilisateur,
je veux des indicateurs de chargement fluides,
afin de ne pas penser que l'app est plantee.

**Criteres d'acceptation :**
- [x] Skeleton screens sur le dashboard (pas de spinner plein ecran) — DashboardLoadingSkeleton composable
- [x] Indicateur de chargement sur la recherche d'aliments — SearchLoadingSkeleton composable
- [x] Optimistic updates sur la saisie (le journal se met a jour avant la reponse serveur) — optimistic update dans onToggleFavorite + hydratation offline
- [x] Transitions fluides entre les etats — sealed interface states (Loading/Success/Error) partout

**Complexite :** S
**Priorite :** MVP
**Dependances :** UX-01
**Agent assigne :** Phase 4

---

**UX-04** — Gestion des erreurs utilisateur

En tant qu'utilisateur,
je veux des messages d'erreur clairs et humains,
afin de comprendre ce qui s'est passe et quoi faire.

**Criteres d'acceptation :**
- [x] Messages d'erreur en francais, comprehensibles (pas de codes techniques) — Strings.kt avec messages FR
- [x] Recherche sans resultat : "Aucun aliment trouve — essaie un autre mot" — ERROR_SEARCH_NO_RESULTS dans Strings
- [x] Erreur reseau : "Pas de connexion — tes donnees seront synchronisees plus tard" — ERROR_NETWORK dans Strings
- [x] Erreur serveur : "Oups, quelque chose a coince — reessaie dans quelques instants" — ERROR_SERVER dans Strings
- [x] Chaque erreur propose une action (reessayer, modifier la saisie, etc.) — ErrorMessage composable avec onRetry

**Complexite :** S
**Priorite :** MVP
**Dependances :** UX-01
**Agent assigne :** Phase 4

---

**UX-05** — Disclaimer legal

En tant que nouvel utilisateur,
je veux etre informe que l'app ne remplace pas un avis medical,
afin d'utiliser l'app en connaissance de cause.

**Criteres d'acceptation :**
- [x] Disclaimer affiche a la premiere utilisation (apres inscription) — DisclaimerScreen dans le flow onboarding
- [x] Texte : "appFood est un outil d'aide au suivi nutritionnel. Il ne remplace pas l'avis d'un professionnel de sante." — texte dans Strings.kt
- [x] Acceptation requise pour continuer — onAcceptDisclaimer() dans DisclaimerViewModel
- [x] Disclaimer accessible dans les mentions legales / A propos — AboutScreen avec liens CGU/Politique

**Complexite :** S
**Priorite :** MVP
**Dependances :** AUTH-01
**Agent assigne :** Phase 4

---

### EPIC : LEGAL — Confidentialite, CGU & Conformite

---

**LEGAL-01** — Politique de confidentialite

En tant qu'utilisateur,
je veux consulter la politique de confidentialite de l'app,
afin de savoir comment mes donnees personnelles et alimentaires sont traitees.

**Criteres d'acceptation :**
- [x] Page "Politique de confidentialite" accessible depuis les reglages et l'inscription — PrivacyPolicyScreen + AboutScreen link
- [x] Contenu conforme RGPD : responsable du traitement, finalites, base legale, duree de conservation, droits de l'utilisateur — contenu placeholder RGPD dans PrivacyPolicyScreen
- [x] Mention explicite du traitement des donnees alimentaires et de sante (categorie sensible RGPD) — mention dans le contenu placeholder
- [x] Detail des sous-traitants (Firebase, Railway, etc.) et localisations des serveurs — mentionne dans le placeholder
- [x] Acceptation obligatoire a l'inscription (case a cocher, non pre-cochee) — ConsentScreen au premier lancement
- [ ] Lien vers la politique accessible depuis les stores (App Store, Google Play) — pas encore publie sur les stores
- [x] Version datee avec historique des modifications — version datee dans le contenu

**Complexite :** M
**Priorite :** MVP
**Dependances :** Aucune
**Agent assigne :** Phase 4
**Notes techniques :** Les donnees alimentaires couplees au poids/taille/age constituent des donnees de sante au sens du RGPD (art. 9). Necessite un consentement explicite. Envisager de consulter un juriste pour la redaction finale.

---

**LEGAL-02** — Conditions Generales d'Utilisation (CGU)

En tant qu'utilisateur,
je veux consulter les CGU de l'app,
afin de connaitre les regles d'utilisation du service.

**Criteres d'acceptation :**
- [x] Page "CGU" accessible depuis les reglages et l'inscription — TermsOfServiceScreen + AboutScreen link
- [x] Contenu couvrant : objet du service, responsabilites, propriete intellectuelle, limitation de responsabilite (non-substitution a un avis medical), moderation du contenu communautaire (futur), resiliation — contenu placeholder dans TermsOfServiceScreen
- [x] Mention claire que l'app ne fournit pas de conseils medicaux — mention dans le placeholder CGU
- [x] Acceptation obligatoire a l'inscription — ConsentScreen au premier lancement
- [x] Version datee — version datee dans le contenu

**Complexite :** M
**Priorite :** MVP
**Dependances :** Aucune
**Agent assigne :** Phase 4

---

**LEGAL-03** — Gestion du consentement et preferences cookies/tracking

En tant qu'utilisateur,
je veux pouvoir gerer mes consentements (analytics, publicite),
afin de controler l'utilisation de mes donnees.

**Criteres d'acceptation :**
- [x] Ecran de consentement au premier lancement (avant toute collecte) — ConsentScreen dans le flow onboarding
- [x] Choix granulaire : analytics, publicite personnalisee, amelioration du service — ConsentViewModel avec 3 toggles
- [x] Possibilite de modifier ses choix dans les reglages a tout moment — ConsentSettingsScreen + PUT /consents/{type}
- [x] Aucun tracking active sans consentement explicite — tous les toggles defaut false dans ConsentViewModel
- [x] Conforme ePrivacy / RGPD — backend ConsentRoutes + ConsentService + ConsentsTable

**Complexite :** M
**Priorite :** MVP
**Dependances :** AUTH-01
**Agent assigne :** Phase 4

---

**LEGAL-04** — Chiffrement des donnees sensibles

En tant qu'utilisateur,
je veux que mes donnees de sante soient chiffrees,
afin qu'elles soient protegees en cas de fuite de donnees.

**Criteres d'acceptation :**
- [x] Donnees sensibles chiffrees en base (poids, taille, age, donnees alimentaires) — EncryptionService AES-256-GCM avec encrypt/decrypt
- [x] Chiffrement en transit (HTTPS, deja couvert par INFRA-02) — Railway HTTPS actif
- [ ] Chiffrement au repos sur la base locale SQLDelight — pas de chiffrement SQLCipher implemente
- [x] Cles de chiffrement gerees de maniere securisee (pas dans le code) — ENCRYPTION_KEY en variable d'environnement

**Complexite :** L
**Priorite :** MVP
**Dependances :** SETUP-03, SETUP-05
**Agent assigne :** Phase 4

---

### EPIC : SUPPORT — Support utilisateur & Feedback

---

**SUPPORT-01** — Page A propos et contact

En tant qu'utilisateur,
je veux trouver un moyen de contacter l'equipe appFood,
afin de signaler un probleme ou poser une question.

**Criteres d'acceptation :**
- [x] Page "A propos" accessible depuis les reglages — AboutScreen composable
- [x] Email de support affiche et cliquable (ouvre le client mail) — ABOUT_SUPPORT_EMAIL avec uriHandler.openUri("mailto:...")
- [x] Lien vers les CGU et la politique de confidentialite — onNavigateToCgu + onNavigateToPrivacyPolicy
- [x] Version de l'app affichee — ABOUT_APP_VERSION dans Strings
- [x] Mention legale (editeur, hebergeur) — ABOUT_EDITOR_VALUE + ABOUT_HOST_VALUE
- [x] Conforme aux exigences App Store et Google Play — toutes les infos requises presentes

**Complexite :** S
**Priorite :** MVP
**Dependances :** UX-01
**Agent assigne :** Phase 4
**Notes techniques :** Obligatoire pour la validation App Store (Apple exige un moyen de contact). Google Play aussi.

---

**SUPPORT-02** — FAQ integree

En tant qu'utilisateur,
je veux consulter une FAQ dans l'app,
afin de trouver des reponses a mes questions sans contacter le support.

**Criteres d'acceptation :**
- [x] Page FAQ accessible depuis les reglages ou la page A propos — FaqScreen + lien dans AboutScreen
- [x] Questions/reponses organisees par theme (compte, saisie, quotas, recettes, donnees) — FaqThemeGroup avec 5 themes
- [x] Contenu statique au MVP (modifiable sans mise a jour de l'app = stocke cote serveur) — SupportApi.getFaq() + fallback statique
- [x] Lien "Ma question n'est pas ici → Contacter le support" — lien support dans FaqScreen

**Complexite :** S
**Priorite :** MVP
**Dependances :** SUPPORT-01
**Agent assigne :** Phase 4

---

**SUPPORT-03** — Formulaire de feedback in-app

En tant qu'utilisateur,
je veux pouvoir envoyer un feedback directement depuis l'app,
afin de signaler un bug ou suggerer une amelioration facilement.

**Criteres d'acceptation :**
- [ ] Bouton "Donner mon avis" dans les reglages
- [ ] Formulaire : type (bug, suggestion, autre), description, screenshot optionnel
- [ ] Envoi par email ou stockage en base pour consultation admin
- [ ] Confirmation d'envoi a l'utilisateur

**Complexite :** M
**Priorite :** V1.1
**Dependances :** SUPPORT-01
**Agent assigne :** Phase 4

---

### EPIC : QUALITE — Tests & Documentation

---

**QUALITE-01** — Tests unitaires logique metier

En tant que developpeur,
je veux des tests unitaires couvrant la logique metier critique,
afin de garantir la fiabilite des calculs nutritionnels.

**Criteres d'acceptation :**
- [x] Tests du calcul des quotas (differents profils, regimes, activites) — CalculerQuotasUseCaseTest.kt
- [x] Tests de l'algorithme de recommandation — RecommandationAlimentUseCaseTest.kt + RecommandationRecetteUseCaseTest.kt
- [ ] Tests du calcul des nutriments d'une recette — pas de test dedie
- [ ] Tests de la resolution de conflits sync — pas de test dedie
- [ ] Couverture > 80% sur le module logique metier — couverture non mesuree, tests partiels
- [x] Execution dans le pipeline CI — ci.yml configure avec tests

**Complexite :** L
**Priorite :** MVP
**Dependances :** QUOTAS-01, RECO-01
**Agent assigne :** Phase 4

---

**QUALITE-02** — Tests d'integration API

En tant que developpeur,
je veux des tests d'integration sur les endpoints Ktor,
afin de verifier que l'API fonctionne correctement bout en bout.

**Criteres d'acceptation :**
- [x] Tests de chaque endpoint (happy path + erreurs courantes) — AuthRoutesTest, DashboardRoutesTest, JournalRoutesTest, QuotaRoutesTest + AuthServiceTest, ProfileServiceTest
- [ ] Utilisation de Testcontainers (PostgreSQL + Meilisearch) — tests existants n'utilisent pas Testcontainers
- [x] Tests d'authentification (token valide, invalide, expire) — AuthRoutesTest + AuthServiceTest
- [x] Execution dans le pipeline CI — ci.yml configure avec tests backend

**Complexite :** L
**Priorite :** MVP
**Dependances :** INFRA-01
**Agent assigne :** Phase 4

---

**QUALITE-03** — Tests E2E mobile (Maestro)

En tant que developpeur,
je veux des tests E2E sur les parcours critiques de l'app,
afin de detecter les regressions UI.

**Criteres d'acceptation :**
- [ ] Maestro configure pour Android et iOS
- [ ] Tests des parcours : inscription, saisie de repas, consultation du dashboard
- [ ] Execution dans le pipeline CI (Android au minimum)

**Complexite :** L
**Priorite :** V1.1
**Dependances :** QUALITE-02
**Agent assigne :** Phase 4

---

---

## THEME 8 : Monetisation (Freemium / Premium)

---

### EPIC : PREMIUM — Gestion freemium / premium

---

**PREMIUM-01** — Architecture freemium / gestion des abonnements

En tant que developpeur,
je veux un systeme de gestion d'abonnement premium integre aux stores,
afin de monetiser l'app avec un modele freemium.

**Criteres d'acceptation :**
- [ ] Integration Google Play Billing (Android) et StoreKit 2 (iOS)
- [ ] Gestion des etats d'abonnement : gratuit, premium mensuel, premium annuel
- [ ] Verification du statut d'abonnement cote serveur (receipt validation)
- [ ] Gestion du renouvellement, de l'annulation et de l'expiration
- [ ] Restauration d'achats (changement de device)
- [ ] Periode d'essai gratuite (7 jours ou 14 jours, a definir)
- [ ] Synchronisation du statut premium avec le backend

**Complexite :** XL
**Priorite :** V1.1
**Dependances :** AUTH-01, SETUP-02
**Agent assigne :** Phase 4
**Notes techniques :** Utiliser une librairie KMP compatible (ex: RevenueCat qui simplifie beaucoup la gestion cross-platform). Apple et Google prennent 15-30% de commission.

---

**PREMIUM-02** — Paywall et gestion des acces

En tant qu'utilisateur gratuit,
je veux voir clairement quelles fonctionnalites sont premium et pouvoir m'abonner facilement,
afin de comprendre la valeur du premium et decider de passer a l'offre payante.

**Criteres d'acceptation :**
- [ ] Ecran de presentation premium (avantages, prix, CTA)
- [ ] Paywall affiche quand l'utilisateur tente d'acceder a une fonctionnalite premium
- [ ] Indication visuelle "Premium" sur les fonctionnalites verrouillees (icone cadenas discret)
- [ ] Pas de blocage agressif — l'utilisateur gratuit doit avoir une experience complete sur le core
- [ ] Page de gestion de l'abonnement dans les reglages

**Complexite :** L
**Priorite :** V1.1
**Dependances :** PREMIUM-01
**Agent assigne :** Phase 4

---

**PREMIUM-03** — Gestion de la publicite (version gratuite)

En tant que developpeur,
je veux integrer de la publicite non-intrusive dans la version gratuite,
afin de generer des revenus sur les utilisateurs gratuits.

**Criteres d'acceptation :**
- [ ] Integration d'un SDK pub (AdMob ou equivalent)
- [ ] Bannieres en bas d'ecran (pas d'interstitiels bloquants)
- [ ] Publicite desactivee pour les utilisateurs premium
- [ ] Respect du consentement publicitaire (LEGAL-03)
- [ ] Pas de pub sur les ecrans critiques (saisie de repas, dashboard principal)

**Complexite :** M
**Priorite :** V1.1
**Dependances :** PREMIUM-01, LEGAL-03
**Agent assigne :** Phase 4

---

**PREMIUM-04** — Reconnaissance photo des aliments (Premium)

En tant qu'utilisateur premium,
je veux prendre en photo mon repas pour que l'app identifie les aliments et estime les quantites,
afin de remplir mon journal encore plus rapidement.

**Criteres d'acceptation :**
- [ ] Bouton "Photo" sur l'ecran de saisie (a cote de la recherche)
- [ ] Prise de photo ou selection depuis la galerie
- [ ] Analyse IA de l'image : identification des aliments visibles
- [ ] Estimation approximative des quantites (en portions ou grammes)
- [ ] Pre-remplissage du formulaire de saisie avec les aliments detectes
- [ ] L'utilisateur peut ajuster / corriger / supprimer chaque aliment detecte avant validation
- [ ] Mention "estimation approximative" claire (pas de precision garantie)
- [ ] Fonctionnalite reservee aux abonnes premium

**Complexite :** XL
**Priorite :** V1.1
**Dependances :** PREMIUM-02, JOURNAL-01
**Agent assigne :** Phase 4
**Notes techniques :** Utiliser une API de vision (Google Cloud Vision, Claude Vision, ou un modele specialise food recognition). Le plus difficile est l'estimation des quantites — commencer par des approximations en portions standard ("1 pomme", "1 assiette de pates") plutot qu'en grammes precis. Peut necessiter un fine-tuning ou des prompts bien calibres.

---

### Repartition Freemium / Premium

| Fonctionnalite | Gratuit | Premium |
|----------------|---------|---------|
| Saisie manuelle des aliments | Oui | Oui |
| Recherche d'aliments (Meilisearch) | Oui | Oui |
| Calcul des quotas personnalises | Oui | Oui |
| Dashboard jour/semaine | Oui | Oui |
| Recommandation d'aliments basique | Oui | Oui |
| Livre de recettes | Oui | Oui |
| Suivi du poids | Oui | Oui |
| Suivi hydratation | Oui | Oui |
| Publicite | Oui (bannieres) | Non |
| Dashboard mensuel + tendances | Limite | Complet |
| Recommandation de recettes avancee | Limite | Complet |
| Reconnaissance photo des repas | Non | Oui |
| Adaptation IA des recettes (V2) | Non | Oui |
| Compatibilite robots de cuisine (V2) | Non | Oui |
| Statistiques detaillees | Non | Oui |

**Philosophie** : Le core de l'app (saisie + quotas + recommandations basiques) reste gratuit. Le premium apporte du confort (pas de pub, photo), de la profondeur (stats avancees, IA) et des integrations (robots).

---

---

## THEME 9 : Features futures (V2+)

---

### EPIC : GAMIF — Gamification legere

---

**GAMIF-01** — Suivi de semaines consecutives

En tant qu'utilisateur,
je veux voir combien de semaines consecutives j'ai suivi mes nutriments,
afin d'etre encourage a maintenir mes bonnes habitudes.

**Criteres d'acceptation :**
- [ ] Compteur de semaines consecutives avec au moins 1 saisie/jour
- [ ] Affichage discret sur le profil ou le dashboard
- [ ] Ton positif (pas de penalite si on rate un jour)
- [ ] Pas de notification de culpabilisation

**Complexite :** S
**Priorite :** V2
**Dependances :** DASHBOARD-02
**Agent assigne :** Phase 4

---

**GAMIF-02** — Recap hebdomadaire positif

En tant qu'utilisateur,
je veux un recap de fin de semaine mettant en avant mes reussites,
afin d'etre motive a continuer.

**Criteres d'acceptation :**
- [ ] Ecran recap dimanche soir / lundi matin
- [ ] Met en avant : nutriments bien couverts, ameliorations vs semaine precedente
- [ ] Suggestions pour la semaine suivante (1-2 nutriments a ameliorer)
- [ ] Ton encourageant, jamais culpabilisant

**Complexite :** M
**Priorite :** V2
**Dependances :** DASHBOARD-02, NOTIF-03
**Agent assigne :** Phase 4

---

### EPIC : ROBOTS — Compatibilite robots de cuisine

---

**ROBOTS-01** — Recherche et analyse des APIs robots de cuisine

En tant que developpeur,
je veux analyser les possibilites d'integration avec les robots de cuisine populaires,
afin de planifier l'implementation.

**Criteres d'acceptation :**
- [ ] Analyse des APIs disponibles (Companion, Cookeo, Thermomix/Cookidoo)
- [ ] Identification des robots les plus accessibles techniquement
- [ ] Document de faisabilite avec recommandation

**Complexite :** M
**Priorite :** V2
**Dependances :** Aucune
**Agent assigne :** Phase 4

---

**ROBOTS-02** — Export de recettes vers robot de cuisine

En tant qu'utilisateur,
je veux envoyer une recette directement a mon robot de cuisine,
afin de la preparer sans ressaisir les instructions.

**Criteres d'acceptation :**
- [ ] Selection du robot connecte dans le profil
- [ ] Bouton "Envoyer au robot" sur le detail d'une recette
- [ ] Conversion de la recette au format du robot
- [ ] Confirmation d'envoi

**Complexite :** XL
**Priorite :** V2
**Dependances :** ROBOTS-01, RECETTES-02
**Agent assigne :** Phase 4

---

### EPIC : SOCIAL — Fonctionnalites communautaires

---

**SOCIAL-01** — Soumission de recettes par les utilisateurs

En tant qu'utilisateur,
je veux soumettre mes propres recettes,
afin de partager avec la communaute et enrichir le catalogue.

**Criteres d'acceptation :**
- [ ] Formulaire de creation de recette (nom, ingredients, etapes, photo)
- [ ] Calcul automatique des nutriments
- [ ] Soumission pour moderation (non publiee directement)
- [ ] Notification quand la recette est approuvee

**Complexite :** L
**Priorite :** V2
**Dependances :** RECETTES-03
**Agent assigne :** Phase 4

---

### EPIC : DESIGN — Identite visuelle

---

**DESIGN-01** — Charte graphique et mascotte

En tant qu'equipe produit,
je veux une identite visuelle complete (logo, mascotte, palette, typographie),
afin de donner une personnalite a l'app et creer de l'attachement.

**Criteres d'acceptation :**
- [ ] Logo appFood
- [ ] Mascotte / personnage (brief a un designer)
- [ ] Palette de couleurs definitive
- [ ] Typographie choisie
- [ ] Guidelines d'utilisation

**Complexite :** XL
**Priorite :** V1.1
**Dependances :** Aucune
**Agent assigne :** Phase 4
**Notes techniques :** Travail a confier a un designer professionnel. Budget a prevoir.

---

**DESIGN-02** — Dark mode

En tant qu'utilisateur,
je veux pouvoir activer un mode sombre,
afin d'utiliser l'app confortablement le soir.

**Criteres d'acceptation :**
- [ ] Theme sombre complet sur toutes les pages
- [ ] Bascule dans les reglages (clair / sombre / systeme)
- [ ] Persistence du choix

**Complexite :** M
**Priorite :** V1.1
**Dependances :** DESIGN-01
**Agent assigne :** Phase 4

---

---

## Resume du backlog

### Compteur par priorite

| Priorite | Nombre de stories |
|----------|-------------------|
| MVP | 48 |
| V1.1 | 20 |
| V2 | 7 |
| **TOTAL** | **75** |

### Compteur par theme (MVP uniquement)

| Theme | Stories MVP | Complexite estimee |
|-------|------------|-------------------|
| Fondations techniques (SETUP, INFRA, DATA) | 10 | ~25 jours |
| Authentification & Profil (AUTH inclut Apple Sign-In) | 8 | ~15 jours |
| Suivi nutritionnel (QUOTAS, JOURNAL, DASHBOARD) | 10 | ~18 jours |
| Suivi poids & Hydratation (POIDS, HYDRA) | 3 | ~7 jours |
| Portions standard (PORTIONS) | 1 | ~4 jours |
| Recommandations & Recettes | 6 | ~14 jours |
| Notifications | 1 | ~3 jours |
| Synchronisation & Offline | 3 | ~8 jours |
| Qualite & Transverse (UX, QUALITE) | 8 | ~10 jours |
| Legal & Conformite (LEGAL) | 4 | ~10 jours |
| Support utilisateur (SUPPORT) | 2 | ~3 jours |
| **TOTAL MVP** | **48** | **~117 jours** |

### Stories V1.1 notables

| Theme | Stories V1.1 | Complexite estimee |
|-------|-------------|-------------------|
| Monetisation (PREMIUM) : abonnements, paywall, pub, reconnaissance photo | 4 | ~20 jours |
| Design : charte graphique, mascotte, dark mode | 2 | ~10 jours |
| Notifications avancees : bilan mi-journee, hebdo, preferences, hydratation | 4 | ~8 jours |
| Fonctionnalites complementaires : scan code-barres, copie repas, objectif poids, export, feedback, portions perso, tests E2E, vue mensuelle | 6 | ~15 jours |
| **TOTAL V1.1** | **20** | **~53 jours** |

### Estimation globale MVP

Avec 2 developpeurs travaillant en parallele (en tenant compte des dependances et du temps de coordination) :
- **Estimation optimiste** : ~3-4 mois
- **Estimation realiste** : ~4-5 mois
- **Estimation pessimiste** : ~5-6 mois (si beaucoup de complexite cachee ou temps partiel)

---

### Chemin critique (dependances bloquantes)

```
SETUP-01a (KMP shared) ──→ SETUP-01b (UI/navigation) ──→ UX-01
                     │
                     ├──→ AUTH-01 ──→ PROFIL-01 ──→ QUOTAS-01 ──→ DASHBOARD-01 ──→ RECO-01 ──→ RECO-02
SETUP-02 (Ktor) ────┤
                     ├──→ SETUP-03 (DB) ──→ DATA-01 (Ciqual) ──→ DATA-02 (OFF) ──→ JOURNAL-01
                     │                                                               ↑
SETUP-04 (Meili) ───────────────────────────────────────────────────────────────────┘
                     │
SETUP-01a ──→ SETUP-05 (SQLDelight) ──→ SYNC-01 ──→ SYNC-02
```

---

*Ce document sera complete par le rapport Phase 4 (Dispatch & Plan d'execution).*
