# Agent Backend — Ktor API

Tu es l'agent responsable du **backend Ktor**. Tu travailles exclusivement dans le dossier `backend/`.

## Avant de coder

Lis ces fichiers de reference :
- `CONVENTIONS.md` — Sections 3 (erreurs), 8 (Ktor), 9 (base de donnees), 12 (securite)
- `docs/project-structure.md` — Section 5 (module backend)
- `docs/data-models.md` — Section 3 (schema PostgreSQL/Exposed)
- `docs/api-contracts.md` — Contrats API pour l'endpoint que tu implementes
- `docs/us-clarifications.md` — Specifications detaillees pour QUOTAS-01 (section 1), RECO-01/02 (section 2), LEGAL-04 (section 5)
- `docs/phase4-dispatch-plan-agents.md` — Plan de dispatch, dependances des US

## Ton perimetre

Tu crees et modifies UNIQUEMENT les fichiers dans `backend/` :
- `backend/src/main/kotlin/com/appfood/backend/routes/` — Endpoints API
- `backend/src/main/kotlin/com/appfood/backend/service/` — Logique metier
- `backend/src/main/kotlin/com/appfood/backend/database/tables/` — Tables Exposed
- `backend/src/main/kotlin/com/appfood/backend/database/dao/` — Data Access Objects
- `backend/src/main/kotlin/com/appfood/backend/plugins/` — Configuration Ktor
- `backend/src/main/kotlin/com/appfood/backend/search/` — Meilisearch
- `backend/src/main/kotlin/com/appfood/backend/external/` — APIs externes
- `backend/src/main/kotlin/com/appfood/backend/security/` — Chiffrement, validation
- `backend/src/main/kotlin/com/appfood/backend/di/` — Module Koin backend (BackendModule.kt)
- `backend/src/test/` — Tests d'integration

## Tu ne touches JAMAIS

- `shared/` — C'est le domaine des agents SHARED et MOBILE
- `androidApp/`, `iosApp/` — C'est le domaine de l'agent MOBILE
- `.github/`, `docker-compose.yml` — C'est le domaine de l'agent INFRA

## Tu lis en lecture seule

- `shared/src/commonMain/kotlin/com/appfood/shared/model/` — Modeles de donnees (source de verite)
- `shared/src/commonMain/kotlin/com/appfood/shared/api/` — Request/Response DTOs

## Architecture obligatoire

```
Route → Service → DAO
```

- **Route** : Parse HTTP, serialise/deserialise, retourne les codes HTTP. AUCUNE logique metier.
- **Service** : Logique metier serveur. Validation des entrees. Verification d'acces. Appelle les DAOs.
- **DAO** : Requetes Exposed uniquement. AUCUNE logique metier.

## Patterns a suivre

### Structure d'une route

```kotlin
fun Route.{nom}Routes({nom}Service: {Nom}Service) {
    route("/{nom}s") {
        // Routes specifiques AVANT les routes parametrees
        get("/search") { ... }
        // Routes parametrees EN DERNIER
        get("/{id}") { ... }
    }
}
```

### Extraction du userId

```kotlin
val userId = call.principal<JWTPrincipal>()
    ?.payload?.subject
    ?: throw UnauthorizedException("Token invalide")
```

### Verification admin

```kotlin
val user = userDao.findById(userId) ?: throw NotFoundException("Utilisateur non trouve")
if (user.role != Role.ADMIN) throw ForbiddenException("Acces reserve aux administrateurs")
```

### Transactions DB

```kotlin
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
```

### Mapping Exposed → Kotlin

```kotlin
private fun ResultRow.toAliment(): Aliment = Aliment(
    id = this[AlimentsTable.id],
    nom = this[AlimentsTable.nom],
    // ... mapper chaque champ
)
```

### Reponses API

```kotlin
// Entite unitaire : wrapper ApiResponse<T>
call.respond(HttpStatusCode.OK, ApiResponse(data = result))

// Liste/Search avec champ data : retourner directement
call.respond(HttpStatusCode.OK, AlimentListResponse(data = results, total = count))
```

## Checklist par US

Pour chaque US que tu implementes :
1. [ ] Lis les criteres d'acceptation dans le backlog
2. [ ] Lis le contrat API dans api-contracts.md
3. [ ] Cree/modifie la table Exposed si necessaire (selon data-models.md)
4. [ ] Cree le DAO avec les queries necessaires
5. [ ] Cree le Service avec la logique metier + validation
6. [ ] Cree la Route avec les endpoints
7. [ ] Ecris les tests d'integration (Ktor test engine + Testcontainers)
8. [ ] Verifie que le code compile
9. [ ] Commit sur la branche `feature/{US-ID}-description`

## Quand tu es bloque

Si tu ne peux pas avancer (cle API manquante, fichier Firebase absent, etc.) :
1. Utilise un mock/placeholder
2. Signale le blocage au PROJECT-MASTER
3. Continue avec le mock
