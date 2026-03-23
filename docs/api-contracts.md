# appFood — Contrats API

> Source de verite pour tous les endpoints REST.
> Les agents BACKEND et SHARED/MOBILE doivent suivre exactement ces contrats.
> Tout endpoint non documente ici ne doit PAS etre implemente.

---

## Conventions generales

### Base URL

```
{BASE_URL}/api/v1
```

- Dev local : `http://localhost:8080/api/v1`
- Staging : `https://appfood-staging.up.railway.app/api/v1`
- Prod : `https://api.appfood.fr/api/v1`

### Authentification

Tous les endpoints (sauf `/api/health`, `/api/v1/auth/*` et `/api/v1/support/faq`) necessitent un header :

```
Authorization: Bearer {firebase_id_token}
```

Le backend verifie le token via Firebase Admin SDK et extrait le `userId` (= Firebase UID).

**Regle** : Le `userId` est TOUJOURS extrait du token JWT. Aucun endpoint n'accepte un `userId` en parametre d'URL ou de query. Cela garantit qu'un utilisateur ne peut acceder qu'a SES donnees.

### Format des reponses

**Succes** :
```json
{
  "data": { ... }
}
```

**Succes avec liste** :
```json
{
  "data": [ ... ],
  "total": 42
}
```

**Erreur** :
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Le poids doit etre superieur a 0"
  }
}
```

### Codes d'erreur standards

| Code HTTP | Code erreur | Description |
|-----------|-------------|-------------|
| 400 | `VALIDATION_ERROR` | Donnees invalides |
| 401 | `UNAUTHORIZED` | Token manquant ou invalide |
| 403 | `FORBIDDEN` | Acces refuse (ex: admin-only) |
| 404 | `NOT_FOUND` | Ressource introuvable |
| 409 | `CONFLICT` | Conflit (ex: email deja utilise) |
| 429 | `RATE_LIMITED` | Trop de requetes |
| 500 | `INTERNAL_ERROR` | Erreur serveur |

### Types communs

```kotlin
// Pagination (utilise dans les endpoints de liste)
// Query params : ?page=1&size=20
// page : 1-indexed, default 1
// size : default 20, max 100
```

### PUT avec semantique PATCH

Par pragmatisme, les endpoints `PUT` de l'API utilisent une **semantique PATCH** : seuls les champs fournis (non-null) sont mis a jour. C'est un choix delibere pour simplifier le client mobile (pas besoin d'envoyer tous les champs a chaque update). Les endpoints concernes : `PUT /users/me/profile`, `PUT /users/me/preferences`, `PUT /quotas/{nutriment}`, `PUT /hydratation/objectif`, `PUT /portions/{id}`, `PUT /recettes/{id}`.

### Format des dates

- **Instant** (timestamps) : ISO-8601 complet `"2026-03-23T14:30:00Z"`
- **LocalDate** (dates sans heure, ex: date du journal, date de pesee) : ISO-8601 court `"YYYY-MM-DD"` (ex: `"2026-03-23"`)
- Les query params `date`, `dateFrom`, `dateTo`, `weekOf` utilisent toujours le format `YYYY-MM-DD`

### Gating premium (V1.1)

Au MVP, tous les endpoints sont accessibles a tous les utilisateurs. En V1.1, certains endpoints seront gates par la verification d'abonnement premium (via RevenueCat). Les endpoints concernes seront documentes a ce moment. Les candidats identifies : vue mensuelle dashboard, recommandations avancees, statistiques detaillees.

### Note sur `onboardingComplete`

Le champ `onboardingComplete` est present a la fois dans `UserResponse` et `ProfileResponse`. C'est intentionnel : `UserResponse` est retourne meme quand le profil n'existe pas encore (inscription sans onboarding), et le client a besoin de savoir si l'onboarding est fait sans devoir parser `ProfileResponse?` nullable.

---

## 1. Health

### `GET /health`

**Auth** : Non
**Description** : Verification que le serveur est operationnel.
**Note** : Cet endpoint est monte a la racine `/api/health`, PAS sous `/api/v1/health`. Il n'est pas versionne — il doit rester stable independamment des versions de l'API.

**Response 200** :
```json
{
  "status": "ok",
  "version": "1.0.0"
}
```

---

## 2. Auth — `/auth`

### `POST /auth/register`

**Auth** : Non
**Description** : Enregistre un nouvel utilisateur dans la base appFood apres creation du compte Firebase.
**Note** : La creation du compte Firebase est faite cote client (SDK Firebase). Cet endpoint enregistre l'utilisateur dans PostgreSQL.

**Request** :
```kotlin
@Serializable
data class RegisterRequest(
    val firebaseToken: String,     // ID token Firebase (pour verification serveur)
    val email: String,
    val nom: String?,
    val prenom: String?
)
```

**Response 201** :
```kotlin
@Serializable
data class AuthResponse(
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val onboardingComplete: Boolean,
    val createdAt: String           // ISO-8601
)
```

**Erreurs** : `409 CONFLICT` si l'email existe deja.

---

### `POST /auth/login`

**Auth** : Non
**Description** : Verifie le token Firebase et retourne les infos utilisateur.

**Request** :
```kotlin
@Serializable
data class LoginRequest(
    val firebaseToken: String
)
```

**Response 200** : `AuthResponse` (meme structure que register)

**Erreurs** : `401 UNAUTHORIZED` si le token est invalide.

---

### `DELETE /auth/account`

**Auth** : Oui
**Description** : Supprime le compte et toutes les donnees associees (RGPD droit a l'effacement). Suppression definitive, pas de soft delete.

**Request** : Aucun body.

**Response 204** : No content.

---

### Note sur le logout

La deconnexion est geree cote client via le SDK Firebase (suppression du token local). Il n'y a pas d'endpoint `POST /auth/logout` au MVP — Firebase gere l'invalidation des tokens. Un mecanisme de revocation cote serveur (blacklist de tokens) est prevu en V1.1 (AUTH-06).

---

## 3. Users & Profile — `/users`

### `GET /users/me`

**Auth** : Oui
**Description** : Retourne le profil complet de l'utilisateur connecte.

**Response 200** :
```kotlin
@Serializable
data class UserProfileResponse(
    val user: UserResponse,
    val profile: ProfileResponse?,     // null si onboarding non complete
    val preferences: PreferencesResponse?
)

@Serializable
data class ProfileResponse(
    val sexe: String,                  // "HOMME" | "FEMME"
    val age: Int,
    val poidsKg: Double,
    val tailleCm: Int,
    val regimeAlimentaire: String,     // enum RegimeAlimentaire
    val niveauActivite: String,        // enum NiveauActivite
    val onboardingComplete: Boolean,
    val objectifPoids: String?,        // V1.1 — enum ObjectifPoids, nullable
    val updatedAt: String
)

@Serializable
data class PreferencesResponse(
    val alimentsExclus: List<String>,
    val allergies: List<String>,
    val alimentsFavoris: List<String>,
    val updatedAt: String
)
```

---

### `POST /users/me/profile`

**Auth** : Oui
**Description** : Cree le profil utilisateur (onboarding).

**Request** :
```kotlin
@Serializable
data class CreateProfileRequest(
    val sexe: String,                  // "HOMME" | "FEMME"
    val age: Int,                      // 1-120
    val poidsKg: Double,              // 20.0-500.0
    val tailleCm: Int,                // 50-300
    val regimeAlimentaire: String,     // enum RegimeAlimentaire
    val niveauActivite: String         // enum NiveauActivite
)
```

**Response 201** : `ProfileResponse`

**Validation** :
- `age` : 1-120
- `poidsKg` : 20.0-500.0
- `tailleCm` : 50-300
- `regimeAlimentaire` : valeur valide de l'enum
- `niveauActivite` : valeur valide de l'enum

---

### `PUT /users/me/profile`

**Auth** : Oui
**Description** : Met a jour le profil utilisateur.

**Request** :
```kotlin
@Serializable
data class UpdateProfileRequest(
    val sexe: String?,
    val age: Int?,
    val poidsKg: Double?,
    val tailleCm: Int?,
    val regimeAlimentaire: String?,
    val niveauActivite: String?,
    val objectifPoids: String?         // V1.1
)
```

**Response 200** : `ProfileResponse`

**Note** : Tous les champs sont optionnels (patch partiel). Seuls les champs fournis sont mis a jour.

---

### `PUT /users/me/preferences`

**Auth** : Oui
**Description** : Met a jour les preferences alimentaires.

**Request** :
```kotlin
@Serializable
data class UpdatePreferencesRequest(
    val alimentsExclus: List<String>?,    // liste d'aliment IDs
    val allergies: List<String>?,         // ex: ["gluten", "soja"]
    val alimentsFavoris: List<String>?    // liste d'aliment IDs
)
```

**Response 200** : `PreferencesResponse`

---

### `GET /users/me/export`

**Auth** : Oui
**Description** : Exporte toutes les donnees de l'utilisateur (RGPD portabilite).
**Note** : Endpoint implemente au MVP (conformite RGPD), mais le bouton UI correspondant est en V1.1 (PROFIL-05). L'agent BACKEND implemente l'endpoint, l'agent MOBILE n'ajoute pas le bouton au MVP.

**Response 200** :
```kotlin
@Serializable
data class UserExportResponse(
    val user: UserResponse,
    val profile: ProfileResponse?,
    val preferences: PreferencesResponse?,
    val journalEntries: List<JournalEntryResponse>,
    val quotas: List<QuotaResponse>,
    val poidsHistory: List<PoidsResponse>,
    val hydratation: List<HydratationResponse>,
    val consentements: List<ConsentResponse>,
    val exportedAt: String
)
```

---

## 4. Aliments — `/aliments`

### `GET /aliments/search?q={query}&regime={regime}&categorie={categorie}&page={page}&size={size}`

**Auth** : Oui
**Description** : Recherche d'aliments via Meilisearch. Tolerant aux fautes de frappe.

**Query params** :
- `q` (requis) : Texte de recherche (min 2 caracteres)
- `regime` (optionnel) : Filtrer par regime compatible (ex: `VEGAN`)
- `categorie` (optionnel) : Filtrer par categorie
- `page` (optionnel) : Page, default 1
- `size` (optionnel) : Taille de page, default 20

**Response 200** :
```kotlin
@Serializable
data class SearchAlimentResponse(
    val data: List<AlimentResponse>,
    val total: Int,
    val query: String
)

@Serializable
data class AlimentResponse(
    val id: String,
    val nom: String,
    val marque: String?,               // null = aliment generique (Ciqual), non-null = produit industriel
    val source: String,                // enum SourceAliment
    val sourceId: String?,             // ID dans la base source (code Ciqual ou identifiant OFF)
    val codeBarres: String?,           // code-barres EAN, null pour les aliments generiques
    val categorie: String,
    val regimesCompatibles: List<String>,
    val nutrimentsPour100g: NutrimentValuesResponse,
    val portionsStandard: List<PortionResponse>
)

@Serializable
data class NutrimentValuesResponse(
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val fibres: Double,
    val sel: Double,
    val sucres: Double,
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double
)
```

---

### `GET /aliments/{id}`

**Auth** : Oui
**Description** : Detail d'un aliment par ID.

> **IMPORTANT pour l'agent BACKEND** : Dans `AlimentRoutes.kt`, declarer `GET /aliments/search` et `GET /aliments/barcode/{code}` **AVANT** `GET /aliments/{id}`. Ktor resout les routes dans l'ordre de declaration — sinon `/aliments/barcode/...` sera capture par `{id}` avec la valeur `"barcode"`.

**Response 200** : `AlimentResponse`

**Erreurs** : `404 NOT_FOUND`

---

### `GET /aliments/barcode/{code}`

**Auth** : Oui
**Description** : Recherche d'un aliment par code-barres. Cherche d'abord en base, puis appelle Open Food Facts si non trouve.

**Response 200** : `AlimentResponse`

**Erreurs** : `404 NOT_FOUND` si le produit n'existe ni en base ni sur Open Food Facts.

---

## 5. Portions — `/portions`

### `GET /portions?alimentId={id}`

**Auth** : Oui
**Description** : Retourne les portions standard d'un aliment + les portions generiques + les portions personnalisees de l'utilisateur.

**Query params** :
- `alimentId` (optionnel) : Si fourni, retourne les portions specifiques a cet aliment + generiques. Si absent, retourne uniquement les generiques + personnalisees.

**Response 200** :
```kotlin
@Serializable
data class PortionListResponse(
    val data: List<PortionResponse>,
    val total: Int
)

@Serializable
data class PortionResponse(
    val id: String,
    val alimentId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val estGenerique: Boolean,
    val estPersonnalise: Boolean
)
```

---

### `POST /portions`

**Auth** : Oui
**Description** : Cree une portion personnalisee pour l'utilisateur.

**Request** :
```kotlin
@Serializable
data class CreatePortionRequest(
    val alimentId: String?,        // null = portion generique personnalisee
    val nom: String,               // ex: "Mon bol du matin"
    val quantiteGrammes: Double    // > 0
)
```

**Response 201** : `PortionResponse`

**Validation** : `quantiteGrammes` > 0, `nom` non vide.

---

### `PUT /portions/{id}`

**Auth** : Oui
**Description** : Modifie une portion personnalisee. Seules les portions de l'utilisateur sont modifiables.

**Request** :
```kotlin
@Serializable
data class UpdatePortionRequest(
    val nom: String?,
    val quantiteGrammes: Double?
)
```

**Response 200** : `PortionResponse`

**Erreurs** : `403 FORBIDDEN` si la portion n'appartient pas a l'utilisateur ou n'est pas personnalisee.

---

### `DELETE /portions/{id}`

**Auth** : Oui
**Description** : Supprime une portion personnalisee.

**Response 204** : No content.

**Erreurs** : `403 FORBIDDEN` si non personnalisee.

---

## 6. Recettes — `/recettes`

### `GET /recettes?regime={regime}&typeRepas={type}&page={page}&size={size}&sort={sort}`

**Auth** : Oui
**Description** : Liste des recettes publiees, avec filtres.

**Query params** :
- `regime` (optionnel) : Filtrer par regime (`VEGAN`, `VEGETARIEN`)
- `typeRepas` (optionnel) : Filtrer par type (`PETIT_DEJEUNER`, `DEJEUNER`, `DINER`, `COLLATION`)
- `sort` (optionnel) : Tri — `pertinence` (defaut, base sur les manques nutritionnels de l'utilisateur), `temps_preparation`, `nom`
- `q` (optionnel) : Recherche par nom
- `page`, `size` : Pagination

**Response 200** :
```kotlin
@Serializable
data class RecetteListResponse(
    val data: List<RecetteSummaryResponse>,
    val total: Int
)

@Serializable
data class RecetteSummaryResponse(
    val id: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: List<String>,
    val source: String,                // enum SourceRecette
    val typeRepas: List<String>,
    val imageUrl: String?,
    val nutrimentsParPortion: NutrimentValuesResponse  // calcule = nutrimentsTotaux / nbPortions, jamais stocke
)
```

---

### `GET /recettes/{id}`

**Auth** : Oui
**Description** : Detail complet d'une recette.

**Response 200** :
```kotlin
@Serializable
data class RecetteDetailResponse(
    val id: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: List<String>,
    val typeRepas: List<String>,
    val ingredients: List<IngredientResponse>,
    val etapes: List<String>,
    val nutrimentsTotaux: NutrimentValuesResponse,
    val nutrimentsParPortion: NutrimentValuesResponse, // calcule = nutrimentsTotaux / nbPortions, jamais stocke
    val source: String,                // enum SourceRecette
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class IngredientResponse(
    val alimentId: String,
    val alimentNom: String,
    val quantiteGrammes: Double
)
```

---

### `POST /recettes` (ADMIN ONLY)

**Auth** : Oui (role admin)
**Description** : Cree une nouvelle recette. Reserve aux administrateurs au MVP.

**Request** :
```kotlin
@Serializable
data class CreateRecetteRequest(
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,                      // >= 1
    val regimesCompatibles: List<String>,
    val typeRepas: List<String>,
    val ingredients: List<IngredientRequest>,
    val etapes: List<String>,
    val imageUrl: String?,
    val publie: Boolean
)

@Serializable
data class IngredientRequest(
    val alimentId: String,
    val quantiteGrammes: Double              // > 0
)
```

**Response 201** : `RecetteDetailResponse`

**Note** : Les nutriments totaux sont calcules automatiquement a partir des ingredients. Le serveur fait la somme des nutriments de chaque ingredient * quantite / 100.

---

### `PUT /recettes/{id}` (ADMIN ONLY)

**Auth** : Oui (role admin)
**Description** : Met a jour une recette.

**Request** :
```kotlin
@Serializable
data class UpdateRecetteRequest(
    val nom: String?,
    val description: String?,
    val tempsPreparationMin: Int?,
    val tempsCuissonMin: Int?,
    val nbPortions: Int?,
    val regimesCompatibles: List<String>?,
    val typeRepas: List<String>?,
    val ingredients: List<IngredientRequest>?,  // si fourni, remplace TOUS les ingredients
    val etapes: List<String>?,
    val imageUrl: String?,
    val publie: Boolean?
)
```

**Response 200** : `RecetteDetailResponse`

---

### `DELETE /recettes/{id}` (ADMIN ONLY)

**Auth** : Oui (role admin)
**Response 204** : No content.

---

## 7. Journal — `/journal`

### `GET /journal?date={date}&dateFrom={from}&dateTo={to}&mealType={type}`

**Auth** : Oui
**Description** : Liste les entrees du journal de l'utilisateur.

**Query params** :
- `date` (optionnel) : Date precise (ISO-8601 `YYYY-MM-DD`). Si fourni, ignore `dateFrom`/`dateTo`.
- `dateFrom` (optionnel) : Date de debut (incluse)
- `dateTo` (optionnel) : Date de fin (incluse)
- `mealType` (optionnel) : Filtrer par type de repas
- Si aucun param : retourne les entrees du jour courant

**Response 200** :
```kotlin
@Serializable
data class JournalListResponse(
    val data: List<JournalEntryResponse>,
    val total: Int
)

@Serializable
data class JournalEntryResponse(
    val id: String,
    val date: String,                  // YYYY-MM-DD
    val mealType: String,              // enum MealType
    val alimentId: String?,
    val recetteId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val nbPortions: Double?,
    val nutrimentsCalcules: NutrimentValuesResponse,
    val createdAt: String,
    val updatedAt: String
)
```

---

### `POST /journal`

**Auth** : Oui
**Description** : Ajoute une entree au journal.

**Request** :
```kotlin
@Serializable
data class AddJournalEntryRequest(
    val id: String?,                   // UUID genere cote client (pour la sync offline). Si null, genere cote serveur.
    val date: String,                  // YYYY-MM-DD
    val mealType: String,              // enum MealType
    val alimentId: String?,            // alimentId OU recetteId, pas les deux
    val recetteId: String?,
    val quantiteGrammes: Double?,      // requis si alimentId
    val nbPortions: Double?            // requis si recetteId
)
```

**Response 201** : `JournalEntryResponse`

**Validation** :
- `alimentId` XOR `recetteId` (exactement un des deux)
- `quantiteGrammes` > 0 (requis si `alimentId`)
- `nbPortions` > 0 (requis si `recetteId`)
- `date` : format ISO-8601 valide
- `mealType` : valeur valide de l'enum

**Note** : Le serveur calcule les nutriments automatiquement a partir de l'aliment/recette et de la quantite.

---

### `PUT /journal/{id}`

**Auth** : Oui
**Description** : Modifie une entree du journal.

**Request** :
```kotlin
@Serializable
data class UpdateJournalEntryRequest(
    val quantiteGrammes: Double?,
    val nbPortions: Double?,
    val mealType: String?
)
```

**Response 200** : `JournalEntryResponse`

**Note** : On ne peut pas changer l'aliment/recette — supprimer et recreer dans ce cas.

---

### `DELETE /journal/{id}`

**Auth** : Oui
**Response 204** : No content.
**Erreurs** : `404 NOT_FOUND`, `403 FORBIDDEN` si l'entree n'appartient pas a l'utilisateur.

---

### `POST /journal/copy` — V1.1 (NON IMPLEMENTE AU MVP)

**Auth** : Oui
**Description** : Copie un repas ou une journee complete vers une autre date.
**Note** : Cet endpoint est documente pour reference mais ne sera PAS implemente au MVP (JOURNAL-05 = V1.1). Le contrat est fige pour que l'implementation V1.1 soit coherente.

**Request** :
```kotlin
@Serializable
data class CopyJournalDayRequest(
    val sourceDate: String,            // YYYY-MM-DD
    val targetDate: String,            // YYYY-MM-DD
    val mealType: String?,             // si fourni, copie uniquement ce repas. Si null, copie toute la journee.
)
```

**Response 201** : `JournalListResponse` (les nouvelles entrees creees)

**Note** : V1.1 — le use case `CopierRepasUseCase` existe dans le module shared mais l'endpoint n'est pas implemente au MVP.

---

### `GET /journal/summary?date={date}`

**Auth** : Oui
**Description** : Resume nutritionnel d'une journee (total des nutriments consommes).

**Query params** :
- `date` (optionnel) : Default = aujourd'hui

**Response 200** :
```kotlin
@Serializable
data class DailySummaryResponse(
    val date: String,
    val totalNutriments: NutrimentValuesResponse,
    val parRepas: Map<String, NutrimentValuesResponse>,  // cle = MealType
    val nbEntrees: Int
)
```

---

### `GET /journal/summary/weekly?weekOf={date}`

**Auth** : Oui
**Description** : Resume nutritionnel de la semaine (moyenne quotidienne).

**Query params** :
- `weekOf` (optionnel) : N'importe quelle date de la semaine voulue. Default = semaine courante.

**Response 200** :
```kotlin
@Serializable
data class WeeklySummaryResponse(
    val dateFrom: String,              // lundi
    val dateTo: String,                // dimanche
    val moyenneJournaliere: NutrimentValuesResponse,
    val parJour: Map<String, NutrimentValuesResponse>,  // cle = YYYY-MM-DD
    val joursAvecSaisie: Int
)
```

---

## 8. Quotas — `/quotas`

### `GET /quotas`

**Auth** : Oui
**Description** : Retourne tous les quotas de l'utilisateur.

**Response 200** :
```kotlin
@Serializable
data class QuotaListResponse(
    val data: List<QuotaResponse>
)

@Serializable
data class QuotaResponse(
    val nutriment: String,             // enum NutrimentType
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,        // valeur d'origine (pour "revenir au calcul auto")
    val unite: String                  // "kcal", "g", "mg", "µg"
)
```

---

### `GET /quotas/status?date={date}`

**Auth** : Oui
**Description** : Retourne l'etat des quotas vs ce qui a ete consomme (= le dashboard).

**Query params** :
- `date` (optionnel) : Default = aujourd'hui

**Response 200** :
```kotlin
@Serializable
data class QuotaStatusListResponse(
    val date: String,
    val data: List<QuotaStatusResponse>
)

@Serializable
data class QuotaStatusResponse(
    val nutriment: String,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,           // consommee / cible * 100
    val unite: String
)
```

---

### `PUT /quotas/{nutriment}`

**Auth** : Oui
**Description** : Met a jour un quota pour un nutriment (personnalisation manuelle).

**Request** :
```kotlin
@Serializable
data class UpdateQuotaRequest(
    val valeurCible: Double            // > 0
)
```

**Response 200** : `QuotaResponse`

---

### `POST /quotas/{nutriment}/reset`

**Auth** : Oui
**Description** : Remet un quota a sa valeur calculee automatiquement.

**Request** : Aucun body.

**Response 200** : `QuotaResponse` (avec `estPersonnalise = false`)

---

### `POST /quotas/reset-all`

**Auth** : Oui
**Description** : Remet TOUS les quotas a leurs valeurs calculees.

**Request** : Aucun body.

**Response 200** : `QuotaListResponse`

---

### `POST /quotas/recalculate`

**Auth** : Oui
**Description** : Force un recalcul de tous les quotas a partir du profil actuel (utile apres changement de poids).

**Request** : Aucun body.

**Response 200** : `QuotaListResponse`

---

## 9. Poids — `/poids`

### `GET /poids?dateFrom={from}&dateTo={to}`

**Auth** : Oui
**Description** : Historique du poids de l'utilisateur.

**Query params** :
- `dateFrom` (optionnel) : Default = il y a 6 mois
- `dateTo` (optionnel) : Default = aujourd'hui

**Response 200** :
```kotlin
@Serializable
data class PoidsListResponse(
    val data: List<PoidsResponse>,
    val total: Int,
    val poidsCourant: Double?,         // dernier poids enregistre
    val poidsMin: Double?,             // min sur la periode
    val poidsMax: Double?              // max sur la periode
)

@Serializable
data class PoidsResponse(
    val id: String,
    val date: String,                  // YYYY-MM-DD
    val poidsKg: Double,
    val estReference: Boolean,
    val createdAt: String
)
```

---

### `POST /poids`

**Auth** : Oui
**Description** : Enregistre une nouvelle pesee.

**Request** :
```kotlin
@Serializable
data class AddPoidsRequest(
    val id: String?,                   // UUID genere cote client (sync offline). Si null, genere serveur.
    val date: String,                  // YYYY-MM-DD
    val poidsKg: Double               // 20.0-500.0
)
```

**Response 201** :
```kotlin
@Serializable
data class AddPoidsResponse(
    val poids: PoidsResponse,
    val changementSignificatif: Boolean,    // true si ecart > 1kg vs poids de reference
    val messageRecalcul: String?            // ex: "Votre poids a change de +2.3kg. Voulez-vous recalculer vos quotas ?"
)
```

**Erreurs** : `400 VALIDATION_ERROR` si le profil utilisateur n'existe pas (onboarding incomplet — le poids de reference necessite un profil).

---

### `DELETE /poids/{id}`

**Auth** : Oui
**Response 204** : No content.

---

## 10. Hydratation — `/hydratation`

### `GET /hydratation?date={date}`

**Auth** : Oui
**Description** : Retourne les donnees d'hydratation d'une journee.

**Query params** :
- `date` (optionnel) : Default = aujourd'hui

**Response 200** :
```kotlin
@Serializable
data class HydratationResponse(
    val id: String,
    val date: String,
    val quantiteMl: Int,
    val objectifMl: Int,
    val estObjectifPersonnalise: Boolean,
    val pourcentage: Double,           // quantiteMl / objectifMl * 100
    val entrees: List<HydratationEntryResponse>
)

@Serializable
data class HydratationEntryResponse(
    val id: String,
    val heure: String,                 // ISO-8601
    val quantiteMl: Int
)
```

---

### `GET /hydratation/weekly?weekOf={date}`

**Auth** : Oui
**Description** : Resume d'hydratation sur la semaine.

**Response 200** :
```kotlin
@Serializable
data class HydratationWeeklyResponse(
    val dateFrom: String,
    val dateTo: String,
    val moyenneJournaliereMl: Int,
    val objectifMl: Int,
    val parJour: Map<String, HydratationDaySummary>
)

@Serializable
data class HydratationDaySummary(
    val quantiteMl: Int,
    val objectifMl: Int,
    val pourcentage: Double
)
```

---

### `POST /hydratation`

**Auth** : Oui
**Description** : Ajoute une entree d'hydratation. Chaque appel cree une `HydratationEntry` individuelle et incremente le cumul du jour (`quantiteMl`). Si aucun enregistrement `HydratationJournaliere` n'existe pour cette date, il est cree automatiquement.

**Request** :
```kotlin
@Serializable
data class AddHydratationRequest(
    val id: String?,                   // UUID genere cote client (pour la sync offline). Si null, genere cote serveur.
    val date: String,                  // YYYY-MM-DD
    val quantiteMl: Int                // > 0, ex: 250 (verre), 500 (bouteille)
)
```

**Response 201** : `HydratationResponse` (etat mis a jour du jour, cumul incremente)

---

### `PUT /hydratation/objectif`

**Auth** : Oui
**Description** : Personnalise l'objectif d'hydratation.

**Request** :
```kotlin
@Serializable
data class UpdateHydratationObjectifRequest(
    val objectifMl: Int                // > 0
)
```

**Response 200** : `HydratationResponse`

---

### `POST /hydratation/objectif/reset`

**Auth** : Oui
**Description** : Remet l'objectif d'hydratation au calcul automatique (basé sur poids et activite).

**Request** : Aucun body.

**Response 200** : `HydratationResponse`

---

## 11. Recommandations — `/recommandations`

### `GET /recommandations/aliments?date={date}&limit={limit}`

**Auth** : Oui
**Description** : Retourne les aliments recommandes pour combler les manques nutritionnels du jour.

**Query params** :
- `date` (optionnel) : Default = aujourd'hui
- `limit` (optionnel) : Nombre max de suggestions, default 10

**Response 200** :
```kotlin
@Serializable
data class RecommandationAlimentListResponse(
    val date: String,
    val manquesIdentifies: List<String>,     // nutriments en deficit
    val data: List<RecommandationAlimentResponse>
)

@Serializable
data class RecommandationAlimentResponse(
    val aliment: AlimentResponse,
    val nutrimentsCibles: List<String>,
    val quantiteSuggereGrammes: Double,
    val pourcentageCouverture: Map<String, Double>  // cle = NutrimentType
)
```

**Note** : L'algorithme filtre automatiquement les aliments exclus et non compatibles avec le regime de l'utilisateur. Il priorise les aliments qui couvrent plusieurs manques a la fois.

---

### `GET /recommandations/recettes?date={date}&limit={limit}`

**Auth** : Oui
**Description** : Retourne les recettes recommandees pour combler les manques.

**Query params** :
- `date` (optionnel) : Default = aujourd'hui
- `limit` (optionnel) : Default 5

**Response 200** :
```kotlin
@Serializable
data class RecommandationRecetteListResponse(
    val date: String,
    val manquesIdentifies: List<String>,
    val data: List<RecommandationRecetteResponse>
)

@Serializable
data class RecommandationRecetteResponse(
    val recette: RecetteSummaryResponse,
    val nutrimentsCibles: List<String>,
    val pourcentageCouvertureGlobal: Double,
    val pourcentageCouverture: Map<String, Double>
)
```

---

## 12. Notifications — `/notifications`

### `GET /notifications?page={page}&size={size}&nonLuesUniquement={bool}`

**Auth** : Oui
**Description** : Liste les notifications de l'utilisateur.

**Query params** :
- `nonLuesUniquement` (optionnel) : `true` pour ne voir que les non lues. Default `false`.
- `page`, `size` : Pagination

**Response 200** :
```kotlin
@Serializable
data class NotificationListResponse(
    val data: List<NotificationResponse>,
    val total: Int,
    val nonLues: Int
)

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,                  // enum NotificationType
    val titre: String,
    val contenu: String,
    val dateEnvoi: String,
    val lue: Boolean
)
```

---

### `PUT /notifications/{id}/read`

**Auth** : Oui
**Description** : Marque une notification comme lue.

**Request** : Aucun body.

**Response 200** : `NotificationResponse`

---

### `POST /notifications/read-all`

**Auth** : Oui
**Description** : Marque toutes les notifications comme lues.

**Request** : Aucun body.

**Response 200** :
```json
{
  "data": {
    "marqueesCommeLues": 5
  }
}
```

---

### `POST /notifications/register-token`

**Auth** : Oui
**Description** : Enregistre le token FCM du device pour recevoir les notifications push.

**Request** :
```kotlin
@Serializable
data class RegisterFcmTokenRequest(
    val token: String,
    val platform: String               // "ANDROID" | "IOS"
)
```

**Response 200** :
```json
{
  "data": {
    "registered": true
  }
}
```

---

## 13. Consentements — `/consents`

### `GET /consents`

**Auth** : Oui
**Description** : Retourne les consentements de l'utilisateur.

**Response 200** :
```kotlin
@Serializable
data class ConsentListResponse(
    val data: List<ConsentResponse>
)

@Serializable
data class ConsentResponse(
    val type: String,                  // enum ConsentType
    val accepte: Boolean,
    val dateConsentement: String,
    val versionPolitique: String
)
```

---

### `PUT /consents/{type}`

**Auth** : Oui
**Description** : Met a jour un consentement.

**Request** :
```kotlin
@Serializable
data class UpdateConsentRequest(
    val accepte: Boolean,
    val versionPolitique: String       // version des CGU/politique acceptee
)
```

**Response 200** : `ConsentResponse`

**Validation** : `type` doit etre une valeur valide de `ConsentType`.

---

### `POST /consents/initial`

**Auth** : Oui
**Description** : Enregistre les consentements initiaux (onboarding). Cree les 3 types d'un coup.

**Request** :
```kotlin
@Serializable
data class InitialConsentRequest(
    val analytics: Boolean,
    val publicite: Boolean,
    val ameliorationService: Boolean,
    val versionPolitique: String
)
```

**Response 201** : `ConsentListResponse`

---

## 14. Support / FAQ — `/support`

### `GET /support/faq`

**Auth** : Non (accessible sans connexion)
**Description** : Retourne la FAQ, organisee par theme.

**Response 200** :
```kotlin
@Serializable
data class FaqListResponse(
    val data: List<FaqResponse>
)

@Serializable
data class FaqResponse(
    val id: String,
    val theme: String,
    val question: String,
    val reponse: String,
    val ordre: Int
)
```

---

## 15. Sync — `/sync`

### `POST /sync/push`

**Auth** : Oui
**Description** : Envoie les entrees creees/modifiees en mode offline vers le serveur. Le serveur applique les modifications et retourne les conflits eventuels.

**Request** :
```kotlin
@Serializable
data class SyncPushRequest(
    val journalEntries: List<AddJournalEntryRequest>,
    val poidsEntries: List<AddPoidsRequest>,
    val hydratationEntries: List<AddHydratationRequest>,
    val timestamp: String              // ISO-8601, moment de la sync
)
```

**Response 200** :
```kotlin
@Serializable
data class SyncPushResponse(
    val accepted: Int,                 // nombre d'entrees acceptees
    val conflicts: List<SyncConflict>, // entrees en conflit
    val errors: List<SyncError>        // entrees en erreur (validation)
)

@Serializable
data class SyncConflict(
    val entityType: String,            // "journal" | "poids" | "hydratation"
    val entityId: String,
    val clientVersion: String,         // timestamp client
    val serverVersion: String,         // timestamp serveur
    val resolution: String             // "SERVER_WINS" (last-write-wins)
)

@Serializable
data class SyncError(
    val entityType: String,
    val entityId: String,
    val error: String
)
```

---

### `GET /sync/pull?since={timestamp}`

**Auth** : Oui
**Description** : Recupere les modifications serveur depuis un timestamp donne. Utilise pour mettre a jour le cache local.

**Query params** :
- `since` (requis) : Timestamp ISO-8601 de la derniere sync reussie

**Response 200** :
```kotlin
@Serializable
data class SyncPullResponse(
    val journalEntries: List<JournalEntryResponse>,
    val poidsEntries: List<PoidsResponse>,            // liste vide si pas de changement
    val hydratationEntries: List<HydratationResponse>, // liste vide si pas de changement
    val quotas: List<QuotaResponse>,
    val profile: ProfileResponse?,     // null si pas de changement
    val preferences: PreferencesResponse?, // null si pas de changement
    val timestamp: String              // timestamp serveur a stocker pour le prochain pull
)
```

---

## 16. Dashboard — `/dashboard`

### `GET /dashboard?date={date}`

**Auth** : Oui
**Description** : Endpoint agrege pour le dashboard principal. Retourne tout ce dont l'ecran principal a besoin en un seul appel (evite les appels multiples).

**Query params** :
- `date` (optionnel) : Default = aujourd'hui

**Response 200** :
```kotlin
@Serializable
data class DashboardResponse(
    val date: String,
    val quotasStatus: List<QuotaStatusResponse>,
    val journalDuJour: List<JournalEntryResponse>,
    val hydratation: HydratationResponse?,
    val recommandationsAliments: List<RecommandationAlimentResponse>,  // top 5
    val recommandationsRecettes: List<RecommandationRecetteResponse>,  // top 3
    val poidsCourant: Double?
)
```

**Note** : Cet endpoint est un raccourci. Il combine les donnees de `/quotas/status`, `/journal`, `/hydratation`, `/recommandations` et `/poids`. Le client peut aussi appeler ces endpoints individuellement si necessaire.

**Note performance** : Les recommandations (top 5 aliments + top 3 recettes) declenchent l'algorithme de scoring a chaque appel. Pour eviter un cout excessif, le `DashboardService` doit cacher les recommandations avec un TTL de 30 minutes, invalide uniquement apres une nouvelle entree journal ou un changement de quotas.

---

### `GET /dashboard/weekly?weekOf={date}`

**Auth** : Oui
**Description** : Donnees du dashboard hebdomadaire.

**Response 200** :
```kotlin
@Serializable
data class WeeklyDashboardResponse(
    val dateFrom: String,
    val dateTo: String,
    val nutritionHebdo: WeeklySummaryResponse,
    val hydratationHebdo: HydratationWeeklyResponse,
    val nutrimentsCritiques: List<String>,  // nutriments systematiquement sous les quotas
    val ameliorations: List<String>,        // nutriments qui se sont ameliores vs semaine precedente
    val degradations: List<String>          // nutriments qui se sont degrades
)
```

---

## Resume des endpoints

| Methode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/health` | Non | Health check (hors `/api/v1`, monte a `/api/health`) |
| POST | `/auth/register` | Non | Inscription |
| POST | `/auth/login` | Non | Connexion |
| DELETE | `/auth/account` | Oui | Suppression compte |
| GET | `/users/me` | Oui | Profil complet |
| POST | `/users/me/profile` | Oui | Creation profil (onboarding) |
| PUT | `/users/me/profile` | Oui | Mise a jour profil |
| PUT | `/users/me/preferences` | Oui | Mise a jour preferences |
| GET | `/users/me/export` | Oui | Export RGPD |
| GET | `/aliments/search` | Oui | Recherche aliments |
| GET | `/aliments/{id}` | Oui | Detail aliment |
| GET | `/aliments/barcode/{code}` | Oui | Recherche par code-barres |
| GET | `/portions` | Oui | Liste portions |
| POST | `/portions` | Oui | Creer portion personnalisee |
| PUT | `/portions/{id}` | Oui | Modifier portion |
| DELETE | `/portions/{id}` | Oui | Supprimer portion |
| GET | `/recettes` | Oui | Liste recettes |
| GET | `/recettes/{id}` | Oui | Detail recette |
| POST | `/recettes` | Admin | Creer recette |
| PUT | `/recettes/{id}` | Admin | Modifier recette |
| DELETE | `/recettes/{id}` | Admin | Supprimer recette |
| GET | `/journal` | Oui | Liste entrees journal |
| POST | `/journal` | Oui | Ajouter entree |
| PUT | `/journal/{id}` | Oui | Modifier entree |
| DELETE | `/journal/{id}` | Oui | Supprimer entree |
| POST | `/journal/copy` | Oui | Copier repas/journee **(V1.1)** |
| GET | `/journal/summary` | Oui | Resume journalier |
| GET | `/journal/summary/weekly` | Oui | Resume hebdomadaire |
| GET | `/quotas` | Oui | Liste quotas |
| GET | `/quotas/status` | Oui | Statut quotas vs consommation |
| PUT | `/quotas/{nutriment}` | Oui | Modifier quota |
| POST | `/quotas/{nutriment}/reset` | Oui | Reset quota |
| POST | `/quotas/reset-all` | Oui | Reset tous les quotas |
| POST | `/quotas/recalculate` | Oui | Recalculer quotas |
| GET | `/poids` | Oui | Historique poids |
| POST | `/poids` | Oui | Ajouter pesee |
| DELETE | `/poids/{id}` | Oui | Supprimer pesee |
| GET | `/hydratation` | Oui | Hydratation du jour |
| GET | `/hydratation/weekly` | Oui | Hydratation semaine |
| POST | `/hydratation` | Oui | Ajouter hydratation |
| PUT | `/hydratation/objectif` | Oui | Personnaliser objectif |
| POST | `/hydratation/objectif/reset` | Oui | Reset objectif |
| GET | `/recommandations/aliments` | Oui | Recommandations aliments |
| GET | `/recommandations/recettes` | Oui | Recommandations recettes |
| GET | `/notifications` | Oui | Liste notifications |
| PUT | `/notifications/{id}/read` | Oui | Marquer comme lue |
| POST | `/notifications/read-all` | Oui | Tout marquer comme lu |
| POST | `/notifications/register-token` | Oui | Enregistrer token FCM |
| GET | `/consents` | Oui | Liste consentements |
| PUT | `/consents/{type}` | Oui | Modifier consentement |
| POST | `/consents/initial` | Oui | Consentements initiaux |
| GET | `/support/faq` | Non | FAQ |
| POST | `/sync/push` | Oui | Push offline → serveur |
| GET | `/sync/pull` | Oui | Pull serveur → local |
| GET | `/dashboard` | Oui | Dashboard agrege |
| GET | `/dashboard/weekly` | Oui | Dashboard hebdomadaire |

**Total : 50 endpoints**

---

*Ce document est la reference pour tous les contrats API. Tout endpoint non liste ici ne doit PAS etre implemente par les agents.*
