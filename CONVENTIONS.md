# appFood — Conventions de code

> Ce fichier est lu par TOUS les agents avant chaque tache.
> Toute violation de ces conventions sera detectee par l'agent REVIEW.

---

## 1. Langage & Formatting

### Kotlin

- **Version** : Kotlin 2.0+ (derniere stable)
- **Formatter** : ktlint (configuration par defaut, pas de customisation)
- **Indentation** : 4 espaces (pas de tabs)
- **Longueur de ligne max** : 120 caracteres
- **Trailing commas** : Oui, toujours sur les parametres multi-lignes
- **Imports** : Pas de wildcard imports (`import com.appfood.shared.model.*` interdit). Imports explicites uniquement.
- **Fichiers** : Un fichier par classe/interface principale. Les data classes petites et liees peuvent etre groupees (ex: `Enums.kt`, `AuthRequests.kt`).

### Nommage Kotlin

| Element | Convention | Exemple |
|---------|-----------|---------|
| Package | `lowercase.separated.by.dots` | `com.appfood.shared.model` |
| Classe / Interface | `PascalCase` | `UserRepository`, `AlimentResponse` |
| Fonction | `camelCase`, verbe d'action | `calculerQuotas()`, `ajouterEntree()` |
| Variable / Propriete | `camelCase` | `poidsKg`, `estPersonnalise` |
| Constante | `SCREAMING_SNAKE_CASE` | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| Enum value | `SCREAMING_SNAKE_CASE` | `PETIT_DEJEUNER`, `VITAMINE_B12` |
| Fichier | `PascalCase.kt` | `UserRepository.kt`, `CalculerQuotasUseCase.kt` |

### Nommage specifique appFood

| Element | Pattern | Exemple |
|---------|---------|---------|
| Use case | `{VerbeFR}{NomFR}UseCase` | `CalculerQuotasUseCase`, `AjouterEntreeUseCase` |

**Precision use cases** : Le verbe est en **francais** (domaine metier), le suffixe `UseCase` est toujours en **anglais** (pattern technique). Exemples : `CalculerQuotasUseCase`, `AjouterHydratationUseCase`, `DetecterChangementPoidsUseCase`.
| Repository interface | `{Nom}Repository` | `AlimentRepository`, `JournalRepository` |
| Repository impl | `{Nom}RepositoryImpl` | `AlimentRepositoryImpl` |
| ViewModel | `{Feature}ViewModel` | `DashboardViewModel`, `JournalViewModel` |
| Screen composable | `{Nom}Screen` | `DashboardScreen`, `LoginScreen` |
| Composant composable | `{Nom}{Type}` | `NutrimentProgressBar`, `RecommandationCard` |
| Table Exposed | `{Nom}Table` (objet) | `UsersTable`, `AlimentsTable` |
| DAO | `{Nom}Dao` | `UserDao`, `AlimentDao` |
| Service backend | `{Nom}Service` | `AlimentService`, `QuotaService` |
| Routes backend | `{Nom}Routes` | `AlimentRoutes`, `JournalRoutes` |
| API client | `{Nom}Api` | `AlimentApi`, `JournalApi` |
| Local data source | `Local{Nom}DataSource` | `LocalAlimentDataSource` |
| Request DTO | `{Verbe}{Nom}Request` | `AddJournalEntryRequest`, `UpdateProfileRequest` |
| Response DTO (detail) | `{Nom}Response` | `AlimentResponse`, `QuotaStatusResponse` |
| Response DTO (detail riche) | `{Nom}DetailResponse` | `RecetteDetailResponse` (quand un resume existe aussi) |
| Response DTO (liste) | `{Nom}ListResponse` | `JournalListResponse`, `QuotaListResponse` |
| Response DTO (recherche) | `Search{Nom}Response` | `SearchAlimentResponse` |

**Pourquoi les requests ont un verbe et pas les responses** : Les requests decrivent une **action** (`Add`, `Update`, `Create`, `Copy`), les responses decrivent une **entite** retournee. C'est intentionnel.

**Exception auth** : `RegisterRequest` et `LoginRequest` ne suivent pas le pattern `{Verbe}{Nom}Request` car ce sont des termes universels dans tout projet. C'est la seule exception autorisee.

---

## 2. Architecture & Patterns

### Pattern general : Clean Architecture simplifiee

```
UI (Compose) → ViewModel → UseCase → Repository → DataSource (Remote + Local)
```

- **UI** : Composables purs, aucune logique metier
- **ViewModel** : Gere l'etat UI, appelle les use cases
- **UseCase** : Logique metier pure, testable unitairement
- **Repository** : Interface — orchestre remote + local, gere la sync
- **DataSource** : Acces aux donnees (API distante ou SQLDelight local)

### Backend : Routes → Service → DAO

```
Routes (HTTP) → Service (logique metier) → DAO (base de donnees)
```

- **Routes** : Parse HTTP, serialise/deserialise, retourne les codes HTTP. Aucune logique metier.
- **Service** : Logique metier serveur. Appelle un ou plusieurs DAOs.
- **DAO** : Requetes Exposed. Aucune logique metier.

### Regles strictes

1. **Un ViewModel ne doit JAMAIS appeler un Repository directement** — toujours passer par un UseCase.
2. **Un UseCase ne doit JAMAIS avoir d'effets de bord UI** — il retourne un `Result<T>`.
3. **Un composable ne doit JAMAIS appeler un UseCase directement** — toujours passer par le ViewModel.
4. **Une Route backend ne doit JAMAIS appeler un DAO directement** — toujours passer par un Service.
5. **Les modeles `shared/model/` ne doivent JAMAIS importer de classes des autres couches** (pas d'import DAO, pas d'import UI).

---

## 3. Gestion des erreurs

### Cote shared (Kotlin Multiplatform)

Utiliser un type `Result` custom pour les use cases :

```kotlin
// shared/util/Result.kt
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(
        val code: String,
        val message: String,
        val cause: Throwable? = null,
    ) : AppResult<Nothing>()
}
```

**Regles** :
- Les use cases retournent `AppResult<T>`, jamais de throw
- Les repositories capturent les exceptions et retournent `AppResult.Error`
- Les ViewModels mappent `AppResult` vers l'etat UI

### Cote backend (Ktor)

Utiliser le plugin `StatusPages` pour la gestion globale :

```kotlin
// Exceptions metier
class NotFoundException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)
class ForbiddenException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)

// Dans StatusPages plugin — TOUS les handlers :
exception<NotFoundException> { call, cause ->
    call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorDetail("NOT_FOUND", cause.message ?: "Not found")))
}
exception<ValidationException> { call, cause ->
    call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorDetail("VALIDATION_ERROR", cause.message ?: "Validation error")))
}
exception<ForbiddenException> { call, cause ->
    call.respond(HttpStatusCode.Forbidden, ErrorResponse(ErrorDetail("FORBIDDEN", cause.message ?: "Forbidden")))
}
exception<ConflictException> { call, cause ->
    call.respond(HttpStatusCode.Conflict, ErrorResponse(ErrorDetail("CONFLICT", cause.message ?: "Conflict")))
}
exception<UnauthorizedException> { call, cause ->
    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(ErrorDetail("UNAUTHORIZED", cause.message ?: "Unauthorized")))
}
```

**Regles** :
- Les services throw des exceptions metier typees
- Les routes ne catchent PAS les exceptions — `StatusPages` s'en charge
- Le format d'erreur est toujours `{ "error": { "code": "...", "message": "..." } }`

---

### Regles sur les Request DTOs

- Les `Create*Request` ont des **champs obligatoires** (non-nullable)
- Les `Update*Request` ont des **champs optionnels** (nullable `?`) pour supporter les mises a jour partielles (PATCH semantics). Seuls les champs fournis (non-null) sont mis a jour.
- Les `Add*Request` qui supportent la sync offline ont un `val id: String?` — l'ID est genere cote client, le serveur l'utilise pour l'idempotence

---

## 4. Serialization

### kotlinx.serialization — Regles

- Toutes les data classes API (request/response) sont annotees `@Serializable`
- Les modeles du domain (`shared/model/`) sont aussi `@Serializable` (pour SQLDelight et le cache)
- Utiliser `@SerialName` si le nom JSON differe du nom Kotlin (eviter au maximum)
- Les **Instant** (timestamps) sont serialises en **String ISO-8601 complet** : `"2026-03-23T14:30:00Z"`
- Les **LocalDate** (dates sans heure) sont serialises en **String ISO-8601 court** : `"2026-03-23"`
- Les enums sont serialises par leur **nom** (pas par ordinal) : `"VEGAN"`, pas `0`
- Les noms de champs Kotlin (camelCase : `vitamineB12`) et les valeurs d'enum (SCREAMING_SNAKE_CASE : `VITAMINE_B12`) sont des choses differentes — pas de conflit de serialisation
- Les champs **locaux uniquement** (ex: `syncStatus`) doivent etre annotes `@Transient` avec une valeur par defaut — ils sont exclus de la serialisation JSON et n'existent qu'en SQLDelight

### JSON config

```kotlin
val json = Json {
    ignoreUnknownKeys = true        // tolerance aux champs inconnus (forward compat)
    isLenient = false               // pas de JSON malformed
    encodeDefaults = true           // inclure les valeurs par defaut
    prettyPrint = false             // compact en prod
}
```

---

## 5. Dependency Injection (Koin)

### Structure des modules

```kotlin
// shared/di/SharedModule.kt — use cases, repositories, data sources
val sharedModule = module {
    // Use cases
    factory { CalculerQuotasUseCase(get()) }

    // Repositories
    single<AlimentRepository> { AlimentRepositoryImpl(get(), get()) }

    // Data sources
    single { LocalAlimentDataSource(get()) }
}

// androidApp/di/AndroidModule.kt — platform-specific
val androidModule = module {
    single { DatabaseDriverFactory(get()) }
}

// backend/di/BackendModule.kt — services, DAOs
val backendModule = module {
    single { AlimentService(get(), get()) }
    single { AlimentDao() }
}
```

**Regles** :
- `single` pour les singletons (repositories, data sources, services, DAOs)
- `factory` pour les use cases (nouvelle instance a chaque injection)
- `viewModel` pour les ViewModels (lifecycle-aware)
- Pas de `get()` sans type explicite dans les constructeurs — toujours injecter par interface

---

## 6. Tests

### Conventions de nommage des tests

```kotlin
// Pattern : `should {resultat attendu} when {condition}`
@Test
fun `should return 2500 kcal when homme actif 80kg`() { ... }

@Test
fun `should return error when poids is negative`() { ... }

@Test
fun `should exclude aliments in user preferences`() { ... }
```

### Structure des tests

```kotlin
@Test
fun `should calculate correct quotas for vegan athlete`() {
    // Given
    val profile = UserProfile(
        sexe = Sexe.HOMME,
        age = 30,
        poidsKg = 80.0,
        // ...
    )

    // When
    val result = useCase.execute(profile)

    // Then
    assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
    assertEquals(2500.0, result.data.find { it.nutriment == NutrimentType.CALORIES }?.valeurCible)
}
```

### Regles

- Pattern **Given / When / Then** obligatoire
- Un assert principal par test (plusieurs asserts lies sont OK)
- Pas de `Thread.sleep` — utiliser `runTest` pour les coroutines
- Tests unitaires dans `commonTest` (shared) ou `test` (backend)
- Mocks : utiliser MockK pour les repositories/services

---

## 7. Compose Multiplatform — UI

### Regles generales

- **Imports** : Utiliser `org.jetbrains.compose.*`, PAS `androidx.compose.*`
- **Pas d'imports Android** : Aucun `android.*`, `androidx.activity.*`, `android.content.*` dans `shared/ui/`
- **State** : Utiliser `collectAsState()` pour observer les flows du ViewModel
- **Preview** : Pas de `@Preview` Android — utiliser les previews CMP si disponibles
- **Navigation** : Utiliser la navigation CMP (Voyager ou decompose, a definir au Sprint 0)

### Structure d'un ecran

```kotlin
// Pattern standard pour un ecran
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DashboardContent(
        state = state,
        onAddMeal = viewModel::onAddMeal,
        onRefresh = viewModel::onRefresh,
    )
}

// Contenu pur — testable, previewable
@Composable
private fun DashboardContent(
    state: DashboardState,
    onAddMeal: () -> Unit,
    onRefresh: () -> Unit,
) {
    // UI ici
}
```

**Regles** :
- Separer le composable "connecte" (avec ViewModel) du composable "pur" (avec state)
- Les callbacks sont des lambdas, pas des appels directs au ViewModel
- Un ViewModel par feature, pas par ecran
- Les etats sont modeles par une sealed class/interface :

```kotlin
sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(val data: DashboardData) : DashboardState
    data class Error(val message: String) : DashboardState
}
```

### Theme

- Material Design 3
- Les couleurs sont definies dans `shared/ui/theme/Color.kt`
- Utiliser `MaterialTheme.colorScheme.*` — jamais de couleurs hardcodees
- Les espacements standard : `4.dp`, `8.dp`, `12.dp`, `16.dp`, `24.dp`, `32.dp`
- Les tailles de texte : utiliser `MaterialTheme.typography.*`

### Strings / i18n

- Toutes les chaines visibles par l'utilisateur sont dans des constantes (pas de strings en dur dans les composables)
- Au MVP : constantes Kotlin dans un fichier `Strings.kt` dans `shared/ui/`
- V1.1 : Migration vers un systeme i18n CMP (moko-resources ou equivalent)

```kotlin
// shared/ui/Strings.kt
object Strings {
    const val DASHBOARD_TITLE = "Tableau de bord"
    const val ADD_MEAL = "Ajouter un repas"
    const val ERROR_NO_CONNECTION = "Pas de connexion — tes donnees seront synchronisees plus tard"
    // ...
}
```

---

## 8. Backend Ktor — Conventions specifiques

### Structure d'une route

### Regle CRITIQUE : ordre des routes Ktor

Ktor resout les routes dans l'ordre de declaration. Les routes avec texte fixe (`/search`, `/barcode/{code}`) doivent etre declarees **AVANT** les routes parametrees (`/{id}`). Sinon, `/aliments/barcode/123` sera capture par `/{id}` avec la valeur `"barcode"`.

```kotlin
// Pattern standard pour un fichier de routes
fun Route.alimentRoutes(alimentService: AlimentService) {
    route("/aliments") {
        // ⚠️ Routes specifiques AVANT les routes avec parametres
        get("/search") {
            val query = call.request.queryParameters["q"]
                ?: throw ValidationException("Le parametre 'q' est requis")
            val result = alimentService.search(query, /* ... */)
            call.respond(HttpStatusCode.OK, ApiResponse(data = result))
        }

        get("/barcode/{code}") {
            val code = call.parameters["code"]
                ?: throw ValidationException("Code-barres requis")
            val result = alimentService.findByBarcode(code)
                ?: throw NotFoundException("Produit non trouve")
            call.respond(HttpStatusCode.OK, ApiResponse(data = result))
        }

        // Routes avec parametres EN DERNIER
        get("/{id}") {
            val id = call.parameters["id"]
                ?: throw ValidationException("ID requis")
            val result = alimentService.findById(id)
                ?: throw NotFoundException("Aliment non trouve")
            call.respond(HttpStatusCode.OK, ApiResponse(data = result))
        }
    }
}
```

### Enveloppe de reponse

```kotlin
// Reponse standard — pour les entites unitaires UNIQUEMENT
@Serializable
data class ApiResponse<T>(
    val data: T,
)

// REGLE IMPORTANTE : Les DTOs qui ont deja un champ `data` (listes, search responses,
// dashboard) sont retournes DIRECTEMENT via call.respond(), sans wrapper ApiResponse<T>.
// Exemples : SearchAlimentResponse, JournalListResponse, QuotaListResponse,
// DashboardResponse, etc. — ils ont deja la structure { "data": [...], "total": ... }.
// ApiResponse<T> est reserve aux entites unitaires (ex: ApiResponse(data = alimentResponse)).

// Reponse liste avec total — utilise directement par les DTOs *ListResponse
@Serializable
data class ApiListResponse<T>(
    val data: List<T>,
    val total: Int,
)

// Reponse erreur
@Serializable
data class ErrorResponse(
    val error: ErrorDetail,
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
)
```

### Extraction du userId

```kotlin
// Dans chaque route authentifiee :
val userId = call.principal<JWTPrincipal>()
    ?.payload?.subject
    ?: throw UnauthorizedException("Token invalide")
```

### Validation des entrees

- Valider dans le **Service**, pas dans la Route
- Les routes parsent et transmettent, les services valident et traitent
- Utiliser des exceptions typees (`ValidationException`, `NotFoundException`)
- Pour les enums envoyes en `String` dans les requests, utiliser l'utilitaire suivant :

```kotlin
// backend/security/InputValidation.kt
inline fun <reified T : Enum<T>> String.toEnumOrThrow(fieldName: String): T {
    return try {
        enumValueOf<T>(this)
    } catch (e: IllegalArgumentException) {
        throw ValidationException("Valeur invalide pour '$fieldName': '$this'. Valeurs acceptees: ${enumValues<T>().joinToString()}")
    }
}

// Usage dans un Service :
val sexe = request.sexe.toEnumOrThrow<Sexe>("sexe")
val regime = request.regimeAlimentaire.toEnumOrThrow<RegimeAlimentaire>("regimeAlimentaire")
```

---

## 9. Base de donnees

### Exposed — Conventions

```kotlin
// Transactions : toujours wrapper dans dbQuery
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

// DAO pattern :
class AlimentDao {
    suspend fun findById(id: String): Aliment? = dbQuery {
        AlimentsTable
            .select { AlimentsTable.id eq id }
            .map { it.toAliment() }
            .singleOrNull()
    }

    // Extension function pour le mapping
    private fun ResultRow.toAliment(): Aliment = Aliment(
        id = this[AlimentsTable.id],
        nom = this[AlimentsTable.nom],
        // ...
    )
}
```

**Regles** :
- Toujours utiliser `dbQuery {}` pour les transactions
- Les fonctions de mapping sont des extension functions sur `ResultRow`
- Nommage des fonctions DAO : `findById`, `findByUserId`, `findAll`, `insert`, `update`, `delete`
- Pas de logique metier dans les DAOs

### SQLDelight — Conventions

```sql
-- Nommage des queries : verbNom
-- Exemples :
findById:
SELECT * FROM local_aliment WHERE id = ?;

findFavorites:
SELECT * FROM local_aliment WHERE est_favori = 1 ORDER BY derniere_utilisation DESC;

insertOrReplace:
INSERT OR REPLACE INTO local_aliment(id, nom, ...) VALUES (?, ?, ...);

deleteById:
DELETE FROM local_aliment WHERE id = ?;

countPending:
SELECT COUNT(*) FROM sync_queue WHERE entity_type = ?;
```

---

## 10. Git & Branches

### Convention de branches

```
feature/{US-ID}-description-courte
```

Exemples :
- `feature/SETUP-01-kmp-init`
- `feature/AUTH-01-inscription-email`
- `feature/JOURNAL-01-saisie-aliment`

### Convention de commits

```
{type}({scope}): {description}

Types : feat, fix, refactor, test, docs, chore, style
Scope : le module ou la feature concerne
```

Exemples :
- `feat(auth): implement email/password registration`
- `fix(journal): fix nutriment calculation for portions`
- `test(quotas): add unit tests for vegan profile`
- `chore(infra): configure GitHub Actions CI pipeline`

### Regles

- 1 branche par US
- Commits atomiques — un commit = un changement logique
- Pas de commits "WIP" ou "fix fix"
- Message en anglais (le code aussi)
- Pas de merge dans `main` sans review APPROVE

---

## 11. Langue

### Code source : Anglais

- Noms de variables, fonctions, classes : **anglais**
- Commentaires : **anglais**
- Messages de commit : **anglais**
- Documentation technique : **anglais**

### Exceptions — Francais

- **Strings UI** (affichees a l'utilisateur) : **francais** — l'app est francaise
- **Noms de domaine metier** qui n'ont pas de traduction naturelle : garder le francais si c'est plus clair
  - `NutrimentType` (pas `NutrientType`) — le domaine est francais
  - `Recette` (pas `Recipe`) — terme metier du domaine
  - `Aliment` (pas `Food`) — terme metier du domaine
  - `RegimeAlimentaire` (pas `DietType`) — terme metier
  - `MealType` est OK car c'est un terme generique

### Regle : les noms de domaine metier sont en francais, le reste en anglais

```kotlin
// OUI — domaine metier en francais
class CalculerQuotasUseCase
data class Aliment
data class Recette
enum class RegimeAlimentaire

// OUI — infrastructure/technique en anglais
class AlimentRepository
class LocalAlimentDataSource
fun Route.alimentRoutes()
data class ApiResponse<T>
```

---

## 12. Securite

### Regles absolues

1. **Pas de secrets dans le code** — utiliser les variables d'environnement
2. **Pas de `println` ou `print` en prod** — utiliser un logger structure
3. **Valider toutes les entrees utilisateur** dans les Services backend
4. **Ne jamais exposer les stack traces** dans les reponses API
5. **Verifier le userId sur chaque requete** — un utilisateur ne peut acceder qu'a SES donnees
6. **Chiffrer les donnees sensibles** (poids, taille, age, donnees alimentaires) en base
7. **Rate limiting** sur tous les endpoints authentifies
8. **CORS** configure pour les domaines autorises uniquement

### Verification d'acces (authorization)

```kotlin
// Pattern ownership — un utilisateur ne peut acceder qu'a SES donnees :
suspend fun getJournalEntry(userId: String, entryId: String): JournalEntry {
    val entry = journalDao.findById(entryId)
        ?: throw NotFoundException("Entree non trouvee")
    if (entry.userId != userId) {
        throw ForbiddenException("Acces refuse")
    }
    return entry
}

// Pattern admin — pour les endpoints ADMIN ONLY (ex: CRUD recettes) :
suspend fun createRecette(userId: String, request: CreateRecetteRequest): Recette {
    val user = userDao.findById(userId)
        ?: throw NotFoundException("Utilisateur non trouve")
    if (user.role != Role.ADMIN) {
        throw ForbiddenException("Acces reserve aux administrateurs")
    }
    // ... logique de creation
}
```

**Note** : Les roles sont geres en base (`UsersTable.role`). Au MVP, les admins sont crees manuellement en base (pas d'interface d'administration des roles). Voir `docs/TODO-HUMAIN.md`.

---

## 13. Logging

### Backend

```kotlin
// Utiliser le logger Ktor
private val logger = LoggerFactory.getLogger("AlimentService")

// Niveaux :
logger.info("Recherche aliment: query=$query, userId=$userId")    // operations normales
logger.warn("Aliment non trouve dans Ciqual: $sourceId")          // situations inattendues mais gerees
logger.error("Erreur Open Food Facts API", exception)             // erreurs
```

**Regles** :
- Ne JAMAIS logger les donnees sensibles (mot de passe, token, donnees de sante)
- Logger le `userId` pour la tracabilite
- Logger les appels aux APIs externes (Open Food Facts, Firebase)

### Mobile

- Pas de logs en release build
- Logs de debug uniquement en dev

---

## 14. Performance

### Backend

- Pagination obligatoire sur toutes les listes : `page=1` (1-indexed, defaut), `size=20` (defaut), `max=100`
- Index sur les colonnes de recherche frequente (userId, date)
- Cache des resultats Meilisearch si pertinent
- Pas de N+1 queries — utiliser des joins ou des batch queries

### Mobile

- Lazy loading des listes (LazyColumn)
- Images chargees avec un loader asynchrone (Coil ou equivalent CMP)
- Pas de calculs lourds sur le main thread — utiliser `Dispatchers.Default`
- Cache local agressif pour les aliments et recettes consultes

---

*Ce document est la reference pour toutes les conventions de code. Les agents REVIEW et PROJECT-MASTER verifient le respect de ces conventions.*
