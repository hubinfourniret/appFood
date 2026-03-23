# appFood — Phase 2 : Architecture & Conception

> Date : 2026-03-23
> Version : 1.0

---

## 1. Stack technique

### Vue d'ensemble

| Couche | Choix | Justification |
|--------|-------|---------------|
| Frontend mobile | **Kotlin Multiplatform (KMP) + Compose Multiplatform** | Cross-platform iOS/Android, capitalise sur les competences Kotlin de l'equipe |
| Backend API | **Ktor** | Leger, flexible, natif Kotlin, adapte a une API REST de taille moyenne |
| Base de donnees | **PostgreSQL** | Donnees relationnelles (aliments-nutriments-recettes), requetes complexes (calculs nutritionnels), support JSON natif pour donnees semi-structurees |
| ORM | **Exposed (JetBrains)** | ORM Kotlin natif, integration parfaite avec Ktor, type-safe |
| Moteur de recherche | **Meilisearch** | Tolerance aux fautes de frappe native, <50ms, filtres/facettes, open source, support francais |
| Base locale (offline) | **SQLDelight** | Compatible KMP, SQL type-safe, synchronisation offline-online |
| Authentification | **Firebase Auth** | Email/password + Google Sign-In, simple a integrer, gratuit jusqu'a 50k utilisateurs |
| Notifications push | **Firebase Cloud Messaging (FCM)** | Standard pour mobile, fonctionne iOS/Android via KMP |
| Hebergement | **Railway** (MVP) → **GCP/AWS** (scale) | Zero config DevOps, PostgreSQL manage, serveurs EU, cout faible au debut |
| CI/CD | **GitHub Actions** | Deja sur GitHub, integration native, gratuit pour repos publics |
| Scan code-barres (V1.1) | **ML Kit (Google)** ou **ZXing** | Lecture code-barres + appel API Open Food Facts |

### Budget infrastructure estime (MVP)

| Service | Cout mensuel estime |
|---------|-------------------|
| Railway (Ktor API) | ~5-10€ |
| Railway (PostgreSQL) | ~5-10€ |
| Railway (Meilisearch) | ~5-10€ |
| Firebase Auth | Gratuit (<50k users) |
| Firebase Cloud Messaging | Gratuit |
| **Total MVP** | **~15-30€/mois** |

Largement sous le plafond de 100€/mois. De la marge pour ajouter des services (monitoring, backups, etc.).

---

## 2. Architecture applicative

### Pattern : Monolithe modulaire, API-first

```
┌─────────────────────────────────────────────────┐
│                  CLIENTS                         │
│  ┌──────────────┐        ┌──────────────┐       │
│  │  Android App  │        │   iOS App     │       │
│  │  (Compose)    │        │  (Compose)    │       │
│  └──────┬───────┘        └──────┬───────┘       │
│         │                       │                │
│  ┌──────┴───────────────────────┴───────┐       │
│  │        Code partage KMP              │       │
│  │  - Modeles de donnees                │       │
│  │  - Logique metier (calcul quotas)    │       │
│  │  - Repository / Data layer           │       │
│  │  - SQLDelight (cache local)          │       │
│  └──────────────┬───────────────────────┘       │
└─────────────────┼───────────────────────────────┘
                  │ API REST (JSON)
┌─────────────────┼───────────────────────────────┐
│                 BACKEND                          │
│  ┌──────────────┴───────────────────────┐       │
│  │           Ktor API                    │       │
│  │  ┌─────────┐ ┌──────────┐ ┌────────┐│       │
│  │  │  Auth    │ │ Aliments │ │Recettes││       │
│  │  │ Module   │ │  Module  │ │ Module ││       │
│  │  └─────────┘ └──────────┘ └────────┘│       │
│  │  ┌─────────┐ ┌──────────┐ ┌────────┐│       │
│  │  │ Journal │ │  Quotas  │ │Recomm. ││       │
│  │  │ Module  │ │  Module  │ │ Module ││       │
│  │  └─────────┘ └──────────┘ └────────┘│       │
│  └──────────────┬──────────┬───────────┘       │
│                 │          │                     │
│  ┌──────────────┴──┐  ┌───┴────────────┐       │
│  │   PostgreSQL     │  │  Meilisearch   │       │
│  │   (donnees)      │  │  (recherche)   │       │
│  └─────────────────┘  └────────────────┘       │
└─────────────────────────────────────────────────┘
                  │
      ┌───────────┼────────────┐
      │           │            │
┌─────┴────┐ ┌───┴──────┐ ┌──┴──────────┐
│ Firebase │ │  Ciqual  │ │ Open Food   │
│ Auth/FCM │ │  (ANSES) │ │ Facts API   │
└──────────┘ └──────────┘ └─────────────┘
```

### Justification des choix architecturaux

- **Monolithe modulaire** : Pour un MVP avec 2 devs, les microservices seraient de la complexite prematuree. Un monolithe bien module (par domaine metier) est plus simple a developper, deployer et debugger. On pourra extraire des services plus tard si necessaire.
- **API-first** : Le backend expose une API REST consommee par les apps mobiles. Ca permet d'ajouter un client web plus tard sans toucher au backend.
- **Code partage KMP** : La logique metier (calcul des quotas, modeles de donnees, couche data) est partagee entre iOS et Android. Seule l'UI est specifique a chaque plateforme (via Compose Multiplatform, c'est en fait aussi partage).

### Modules backend

| Module | Responsabilite |
|--------|---------------|
| **Auth** | Verification tokens Firebase, gestion profil utilisateur |
| **Aliments** | CRUD aliments, synchronisation Ciqual/Open Food Facts, indexation Meilisearch |
| **Portions** | Gestion des portions standard et personnalisees par aliment |
| **Recettes** | CRUD recettes, liaison recettes-nutriments, filtrage par regime |
| **Journal** | Saisie des repas, historique, favoris, repas recents |
| **Quotas** | Calcul des AJR personnalises, comparaison apports vs quotas |
| **Poids** | Suivi du poids dans le temps, recalcul des quotas |
| **Hydratation** | Suivi de la consommation d'eau, objectif personnalise |
| **Recommandation** | Algorithme de suggestion d'aliments/recettes pour combler les manques |
| **Legal** | CGU, politique de confidentialite, gestion du consentement |
| **Support** | FAQ, contact, feedback utilisateur |

---

## 3. Modele de donnees

### Entites principales et relations

```
User
├── id, email, nom, prenom
├── poids, taille, age, sexe
├── regime_alimentaire (vegan, vegetarien, omnivore...)
├── activite_physique (type, frequence, intensite)
├── quotas_personnalises (override manuel)
└── preferences
    ├── aliments_exclus[]
    ├── allergies[]
    └── aliments_favoris[]

Aliment
├── id, nom, marque (nullable)
├── source (ciqual, open_food_facts)
├── source_id (id dans la base source)
├── code_barres (nullable)
├── categorie
├── regime_compatible[] (vegan, vegetarien...)
└── nutriments_pour_100g
    ├── calories, proteines, glucides, lipides
    ├── fibres, sel, sucres
    ├── fer, calcium, zinc, magnesium
    ├── vitamine_b12, vitamine_d, vitamine_c...
    └── omega_3, omega_6

Recette
├── id, nom, description
├── temps_preparation, temps_cuisson
├── nb_portions
├── regime_compatible[] (vegan, vegetarien...)
├── source (manuelle, import, communautaire)
├── ingredients[]
│   ├── aliment_id
│   └── quantite_grammes
└── nutriments_totaux (calcule)
    └── (meme structure que aliment)

JournalEntree
├── id, user_id
├── date, type_repas (petit_dej, dejeuner, diner, collation)
├── aliment_id OU recette_id
├── quantite_grammes
└── nutriments_calcules

QuotaJournalier
├── user_id
├── nutriment
├── valeur_cible (calculee ou personnalisee)
├── est_personnalise (boolean)
└── valeur_calculee_origine (pour pouvoir revenir au calcul auto)

HistoriquePoids
├── id, user_id
├── date
├── poids_kg
└── est_reference (boolean, utilise pour le calcul des quotas)

HydratationJournaliere
├── id, user_id
├── date
├── quantite_ml (cumul du jour)
├── objectif_ml (calcule ou personnalise)
└── entrees[]
    ├── heure
    └── quantite_ml

PortionStandard
├── id, aliment_id
├── nom (ex: "1 pomme", "1 cuillere a soupe", "1 bol")
├── quantite_grammes
├── est_generique (boolean, ex: "cuillere a soupe" s'applique a tout)
└── est_personnalise (boolean, cree par l'utilisateur)

Consentement
├── id, user_id
├── type (analytics, publicite, amelioration_service)
├── accepte (boolean)
├── date_consentement
└── version_politique (lien vers la version des CGU/politique acceptee)

Notification
├── id, user_id
├── type (rappel, recommandation, bilan, hydratation)
├── contenu
├── date_envoi
└── lue (boolean)
```

### Relations cles

- **User → JournalEntree** : 1-N (un utilisateur a plusieurs entrees de journal)
- **User → QuotaJournalier** : 1-N (un quota par nutriment par utilisateur)
- **Aliment → Nutriments** : 1-1 (chaque aliment a ses valeurs nutritionnelles)
- **Recette → Aliment** : N-N (une recette contient plusieurs aliments, un aliment peut etre dans plusieurs recettes)
- **JournalEntree → Aliment|Recette** : N-1 (chaque entree reference un aliment OU une recette)
- **User → HistoriquePoids** : 1-N (un utilisateur a plusieurs pesees)
- **User → HydratationJournaliere** : 1-N (une entree par jour)
- **Aliment → PortionStandard** : 1-N (un aliment peut avoir plusieurs portions standard)
- **User → Consentement** : 1-N (un consentement par type)

### Donnees sensibles

- **Donnees personnelles** : poids, taille, age, regime alimentaire, donnees alimentaires → donnees de sante au sens du RGPD (article 9, categorie speciale)
- **Mitigation** :
  - Consentement explicite a l'inscription (case non pre-cochee)
  - Chiffrement en base (PostgreSQL) et au repos sur le device (SQLDelight)
  - Cles de chiffrement gerees via les secrets Railway (pas dans le code)
  - Droit a l'effacement (suppression complete, pas de soft delete)
  - Export des donnees (portabilite RGPD, JSON/CSV)
  - Gestion du consentement granulaire (analytics, publicite, amelioration du service)
  - Politique de confidentialite detaillee avec mention des sous-traitants (Firebase, Railway)
  - CGU avec disclaimer medical
  - Envisager consultation d'un juriste pour la redaction finale

### Volumes estimes

| Entite | Volume initial | Volume a 1000 users |
|--------|---------------|---------------------|
| Aliments (Ciqual) | ~3 000 | ~3 000 |
| Aliments (Open Food Facts FR) | ~500 000 | ~500 000 |
| Recettes | ~100 (manuelles) | ~500+ |
| Journal (entrees/jour/user) | ~5-10 | ~5 000-10 000/jour |
| Historique poids (pesees/user) | ~2-4/mois | ~2 000-4 000/mois |
| Hydratation (entrees/jour/user) | ~3-5 | ~3 000-5 000/jour |
| Portions standard | ~500 (predefinies) | ~500 + personnalisees |
| Consentements | 3/user | ~3 000 |
| Utilisateurs | ~10 | ~1 000 |

---

## 4. Integrations externes

| Service | Usage | Priorite |
|---------|-------|----------|
| **Ciqual (ANSES)** | Base d'aliments officielle francaise, donnees nutritionnelles fiables | MVP |
| **Open Food Facts API** | Produits industriels (Leclerc, U, Intermarche...), scan code-barres | MVP (recherche), V1.1 (scan) |
| **Firebase Auth** | Authentification email/password + Google Sign-In | MVP |
| **Firebase Cloud Messaging** | Notifications push iOS/Android | MVP |
| **Meilisearch** | Recherche d'aliments tolerante aux fautes | MVP |
| **Apple Sign-In** | Authentification Apple (requis pour App Store si Google Sign-In present) | MVP |
| **RevenueCat** | Gestion des abonnements cross-platform (Google Play Billing + StoreKit 2) | V1.1 |
| **AdMob** | Publicite non-intrusive (version gratuite) | V1.1 |
| **API Vision (IA)** | Reconnaissance photo des repas (identification aliments + estimation quantites) | V1.1 |
| **APIs robots de cuisine** | Compatibilite Thermomix, Companion, Cookeo | V2 |
| **IA (LLM)** | Adaptation de recettes aux besoins nutritionnels | V2 |

### Import des donnees Ciqual

La base Ciqual est disponible en telechargement (CSV/XML) sur le site de l'ANSES. Strategie :
1. Import initial en base PostgreSQL
2. Indexation dans Meilisearch
3. Mise a jour annuelle (la base est mise a jour periodiquement par l'ANSES)

### Import Open Food Facts

Open Food Facts propose une API REST et un dump complet de la base. Strategie :
1. Appels API a la demande pour les recherches utilisateur
2. Cache des produits les plus consultes en base locale
3. Indexation dans Meilisearch des produits francais les plus populaires

---

## 5. Strategie offline / synchronisation

### Principe

L'app fonctionne **offline-first** pour la saisie quotidienne :

```
┌─────────────────────────────┐
│     App mobile              │
│  ┌───────────────────────┐  │
│  │  SQLDelight (local)   │  │
│  │  - Journal du jour     │  │
│  │  - Aliments favoris    │  │
│  │  - Recettes consultees │  │
│  │  - Profil utilisateur  │  │
│  │  - Quotas              │  │
│  └───────────┬───────────┘  │
│              │ sync         │
└──────────────┼──────────────┘
               │ quand connexion disponible
┌──────────────┼──────────────┐
│  Backend (PostgreSQL)       │
│  - Source de verite         │
│  - Historique complet       │
└─────────────────────────────┘
```

### Regles de synchronisation

- **Saisie** : L'utilisateur saisit toujours en local. Synchronisation avec le serveur quand la connexion revient.
- **Conflits** : Le timestamp le plus recent gagne (last-write-wins). Pour un journal alimentaire, les conflits sont rares.
- **Cache aliments** : Les aliments recherches et les favoris sont caches localement pour fonctionner offline.
- **Recettes** : Mises en cache au fur et a mesure de la consultation.

---

## 6. Parcours utilisateurs

### Onboarding (premiere ouverture)

1. Ecran de bienvenue avec presentation de la valeur ("Suis tes nutriments, atteins tes quotas")
2. Inscription (email/password ou Google)
3. **Acceptation CGU + Politique de confidentialite** (case a cocher, non pre-cochee)
4. **Gestion du consentement** (analytics, publicite — choix granulaire)
5. **Questionnaire de profil** (3-4 ecrans) :
   - Poids, taille, age, sexe
   - Regime alimentaire (vegan, vegetarien, autre)
   - Activite physique (type, frequence)
   - Aliments exclus / allergies (optionnel, peut etre fait plus tard)
   - **Option skip** : lien discret en bas de chaque ecran ("Passer"), pas un bouton principal. Si skip, valeurs par defaut appliquees (quotas standards homme/femme adulte, regime omnivore). Rappel discret sur le dashboard pour completer le profil plus tard.
6. **Disclaimer medical** : "appFood ne remplace pas l'avis d'un professionnel de sante" (acceptation requise)
7. **Calcul des quotas** → affichage du dashboard avec les objectifs personnalises
8. **"Aha moment"** : L'utilisateur voit ses quotas personnalises et comprend ce qu'il doit manger → c'est la ou la valeur devient concrete

Nombre d'etapes avant le "aha moment" : **~6-7 ecrans, <2-3 minutes** (les ecrans legaux sont rapides a valider)

### Core loop (usage quotidien)

```
Ouvrir l'app
    → Voir le dashboard du jour (quotas atteints / manquants + hydratation)
    → Saisir un repas
        → Rechercher un aliment (Meilisearch)
        → OU valider un plat recommande
        → Choisir portion standard (pomme, cuillere, bol...) OU saisir en grammes
    → Voir la mise a jour des quotas en temps reel
    → Tracker son hydratation (ajout rapide : verre, bouteille)
    → Consulter les recommandations ("Il te manque du fer → essaie cette recette")
    → (Optionnel) Explorer le livre de recettes
    → (Periodique) Saisir son poids
```

**Action principale repetee** : Saisir un repas et voir l'impact sur les quotas.

### Retention — Ce qui fait revenir

- **Recommandations proactives** : "Il te manque X, voici quoi manger" → valeur unique
- **Notifications intelligentes** (peu mais pertinentes) :
  - Bilan de mi-journee si des manques importants sont detectes
  - Recette du soir pour combler les manques
  - Bilan hebdomadaire des tendances
- **Donnees cumulees** : L'historique prend de la valeur avec le temps (tendances, progression)
- **Simplicite** : Validation d'un plat recommande comme raccourci de saisie

### Ecrans principaux

| Ecran | Objectif principal | Actions |
|-------|-------------------|---------|
| **Dashboard** | Voir les quotas du jour d'un coup d'oeil | Consulter les nutriments, hydratation, naviguer vers saisie |
| **Saisie repas** | Ajouter un aliment/recette consomme | Rechercher, choisir portion standard, ajuster quantites |
| **Recommandations** | Voir quoi manger pour combler les manques | Explorer les suggestions, valider un plat |
| **Livre de recettes** | Explorer les recettes disponibles | Filtrer par regime/nutriment, consulter details |
| **Detail recette** | Voir une recette complete | Ingredients, etapes, nutriments apportes |
| **Suivi poids** | Voir l'evolution du poids | Saisir une pesee, consulter le graphique |
| **Profil / Reglages** | Gerer son profil et preferences | Modifier quotas, exclusions, regime, activite, consentements |
| **Historique** | Voir les tendances jour/semaine/mois | Consulter les graphiques, analyser les tendances |
| **A propos / Support** | Contacter le support, consulter FAQ/CGU | Email, FAQ, CGU, politique de confidentialite |

### Etats souvent oublies

- **Etat vide (premier jour)** : Message encourageant + guide pour saisir le premier repas, pas un ecran vide deprimant
- **Etat d'erreur** : Messages humains ("Oups, on n'a pas trouve cet aliment — essaie un autre mot")
- **Chargement** : Skeleton screens sur le dashboard, pas de spinner bloquant
- **Pas de connexion** : Bandeau discret "Mode hors ligne — tes donnees seront synchronisees" (pas bloquant)

---

## 7. Design — Decisions MVP

### Direction MVP

- **Design system** : Material Design 3 (natif Compose, personnalise avec un theme appFood)
- **Palette de couleurs** : A definir, mais orientation nature/sante (verts, couleurs chaudes)
- **Typographie** : Standard Material 3 pour le MVP
- **Dark mode** : Prevu mais V1.1

### Point a traiter (V1.1)

- **Mascotte / personnage** : Element de branding important pour la retention et l'attachement emotionnel. Travail de design a confier a un designer professionnel.
- **Identite visuelle complete** : Logo, palette definitive, illustrations
- **Design system personnalise** : Composants propres a appFood

### Reference de design

- **Yazio** : Reference pour la structure de l'app (dashboard nutritionnel, saisie de repas)
- **Foodvisor** : Reference pour l'UX de tracking alimentaire
- Adaptation a faire : orientation "gestion des nutriments" plutot que "perte de poids"

---

## 8. Infrastructure & Operations

### Environnements

| Environnement | Usage | Deploiement |
|---------------|-------|-------------|
| **Dev** | Developpement local | Docker Compose (Ktor + PostgreSQL + Meilisearch) |
| **Staging** | Tests pre-production | Railway (auto sur merge dans `main`, deploy staging avant prod) |
| **Production** | Utilisateurs reels | Railway (manuel ou auto sur merge dans `main`) |

### Strategie Git

**GitHub Flow** (simple, adapte a une equipe de 2) :
- `main` : branche de production, toujours deployable
- `feature/*` : branches de feature, merge via Pull Request
- Pas de branche `develop` separee au debut — on pourra l'ajouter si necessaire

### Pipeline CI/CD (GitHub Actions)

```
Push sur feature/* :
  1. Lint & format (ktlint)
  2. Tests unitaires
  3. Build

Merge sur main :
  4. Tests d'integration
  5. Build Docker
  6. Deploy staging (auto)
  7. Deploy production (manuel → validation requise)
```

### Monitoring & Observabilite

| Aspect | Outil | Priorite |
|--------|-------|----------|
| Logging | **Railway logs** (integre) → Grafana Cloud (si besoin) | MVP |
| Erreurs applicatives | **Sentry** (gratuit jusqu'a 5k events/mois) | MVP |
| Uptime | **UptimeRobot** (gratuit) | MVP |
| APM / Metriques | Grafana Cloud ou Datadog | V1.1 |

### Securite

| Mesure | Detail |
|--------|--------|
| HTTPS | Obligatoire partout (gere par Railway) |
| Secrets | Variables d'environnement Railway, jamais dans le code |
| Auth | Firebase Auth (tokens JWT verifies cote serveur) |
| RGPD | Consentement explicite (art. 9 donnees de sante), droit a l'effacement, portabilite, chiffrement |
| Chiffrement au repos | Donnees sensibles chiffrees en base PostgreSQL et en local SQLDelight |
| Consentement | Gestion granulaire (analytics, publicite), modification dans les reglages |
| CGU / Politique | Accessibles dans l'app, acceptation obligatoire a l'inscription |
| Dependances | Dependabot active sur GitHub |
| Rate limiting | Middleware Ktor, par utilisateur authentifie |
| Backups PostgreSQL | Automatiques Railway (quotidiens), retention 7 jours |

### Scalabilite

Le systeme est concu pour scaler progressivement :

1. **0-100 users** : Instance unique Railway, largement suffisant
2. **100-1000 users** : Upgrade instance Railway, ajout de cache (Redis si necessaire)
3. **1000+ users** : Migration vers GCP/AWS, load balancer, replicas PostgreSQL

Les goulots d'etranglement previsibles :
- **Meilisearch** : Si la base d'aliments grossit (>1M produits), prevoir une instance dediee plus costaud
- **Calcul des recommandations** : Si l'algorithme devient complexe, prevoir du cache ou du calcul asynchrone

---

## 9. Strategie de tests

### Pyramide de tests

| Niveau | Outil | Quoi tester | Priorite |
|--------|-------|-------------|----------|
| **Unitaires** | JUnit / Kotest | Calcul des quotas, algorithme de recommandation, logique metier | MVP |
| **Integration** | Ktor test engine + Testcontainers | Endpoints API, interactions DB/Meilisearch | MVP |
| **E2E mobile** | Maestro | Parcours utilisateur complets sur Android/iOS | V1.1 |

### Couverture ciblee

- Logique metier (calculs nutritionnels, recommandations) : **>80%**
- Endpoints API : **tous les happy paths + erreurs courantes**
- UI : **tests manuels au MVP**, Maestro en V1.1

---

## 10. Decisions structurantes resumees

| Decision | Choix | Alternative ecartee | Raison |
|----------|-------|---------------------|--------|
| Architecture | Monolithe modulaire | Microservices | Trop complexe pour 2 devs au MVP |
| API | REST (JSON) | GraphQL | Plus simple, suffisant pour les besoins actuels |
| Recherche | Meilisearch | PostgreSQL full-text | Tolerance fautes de frappe native, UX superieure |
| Offline | SQLDelight + sync | Online-only | Essentiel pour un usage quotidien mobile |
| Auth | Firebase Auth | Auth custom | Gain de temps enorme, securite eprouvee |
| Hebergement | Railway | AWS/GCP | Zero DevOps, cout faible, migration facile plus tard |
| Design MVP | Material Design 3 | Design custom | Priorite a la valeur metier, design en V1.1 |
| Tests | Unitaires + Integration | E2E d'emblee | Valider la logique metier d'abord |

---

*Ce document sera complete par le rapport Phase 3 (Backlog & User Stories) et Phase 4 (Dispatch).*
