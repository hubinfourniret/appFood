package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.IngredientsTable
import com.appfood.backend.database.tables.RecettesTable
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.database.tables.SourceRecette
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests E2E du flux ajout aliment -> dashboard.
 * Chaque test est independant (withTestApp cree une nouvelle DB H2).
 */
class JournalAddFlowE2ETest {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Seed un aliment avec des nutriments connus pour des assertions exactes.
     * calories=100, proteines=10, glucides=20, lipides=5 (pour 100g)
     */
    private fun seedTestAliment() {
        transaction {
            AlimentsTable.insert {
                it[id] = "test-aliment-001"
                it[nom] = "Aliment Test"
                it[marque] = null
                it[sourceAliment] = SourceAliment.CIQUAL
                it[sourceId] = "99999"
                it[codeBarres] = null
                it[categorie] = "Test"
                it[regimesCompatibles] = "VEGAN"
                it[calories] = 100.0
                it[proteines] = 10.0
                it[glucides] = 20.0
                it[lipides] = 5.0
                it[fibres] = 3.0
                it[sel] = 0.1
                it[sucres] = 2.0
                it[fer] = 1.0
                it[calcium] = 50.0
                it[zinc] = 0.5
                it[magnesium] = 30.0
                it[vitamineB12] = 0.0
                it[vitamineD] = 0.0
                it[vitamineC] = 10.0
                it[omega3] = 0.2
                it[omega6] = 0.5
            }
        }
    }

    /**
     * Seed une recette avec ingredients et etapes.
     */
    private fun seedTestRecette() {
        seedTestAliment()
        val now = Clock.System.now()
        transaction {
            RecettesTable.insert {
                it[id] = "test-recette-001"
                it[nom] = "Recette Test"
                it[description] = "Une recette de test"
                it[tempsPreparationMin] = 10
                it[tempsCuissonMin] = 20
                it[nbPortions] = 2
                it[regimesCompatibles] = "VEGAN"
                it[sourceRecette] = SourceRecette.MANUELLE
                it[typeRepas] = "DEJEUNER"
                it[etapes] = "Couper les legumes|||Faire cuire|||Servir"
                it[calories] = 400.0
                it[proteines] = 30.0
                it[glucides] = 50.0
                it[lipides] = 15.0
                it[fibres] = 8.0
                it[sel] = 0.5
                it[sucres] = 5.0
                it[fer] = 3.0
                it[calcium] = 100.0
                it[zinc] = 2.0
                it[magnesium] = 60.0
                it[vitamineB12] = 0.0
                it[vitamineD] = 0.0
                it[vitamineC] = 20.0
                it[omega3] = 1.0
                it[omega6] = 2.0
                it[imageUrl] = null
                it[publie] = true
                it[createdAt] = now
                it[updatedAt] = now
            }
            IngredientsTable.insert {
                it[id] = "test-ingredient-001"
                it[recetteId] = "test-recette-001"
                it[alimentId] = "test-aliment-001"
                it[alimentNom] = "Aliment Test"
                it[quantiteGrammes] = 200.0
            }
        }
    }

    /**
     * Helper : register + login, retourne le token JWT.
     */
    private suspend fun registerAndGetToken(
        client: io.ktor.client.HttpClient,
        email: String = "journal-test@example.com",
    ): String {
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firebaseToken":"e2e-journal:$email","email":"$email","nom":"Test","prenom":"User"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status, "Register devrait retourner 201")
        val authBody = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
        return authBody["token"]!!.jsonPrimitive.content
    }

    /**
     * Helper : cree un profil utilisateur.
     */
    private suspend fun createProfile(
        client: io.ktor.client.HttpClient,
        token: String,
    ) {
        val profileResponse = client.post("/api/v1/users/me/profile") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"sexe":"HOMME","age":30,"poidsKg":75.0,"tailleCm":180,"regimeAlimentaire":"VEGAN","niveauActivite":"ACTIF"}""")
        }
        assertEquals(HttpStatusCode.Created, profileResponse.status, "Create profile devrait retourner 201")
    }

    // ===== TEST 1 : POST /journal retourne 201 avec nutriments calcules corrects =====

    @Test
    fun `POST journal retourne 201 avec nutriments calcules corrects`() = withTestApp {
        seedTestAliment()
        val client = createJsonClient()
        val token = registerAndGetToken(client)
        createProfile(client, token)

        val response = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-10","mealType":"DEJEUNER","alimentId":"test-aliment-001","quantiteGrammes":200.0}""")
        }

        assertEquals(HttpStatusCode.Created, response.status, "POST /journal devrait retourner 201")
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("DEJEUNER", body["mealType"]!!.jsonPrimitive.content)
        assertEquals("Aliment Test", body["nom"]!!.jsonPrimitive.content)

        val nutriments = body["nutrimentsCalcules"]!!.jsonObject
        // 200g x 100kcal/100g = 200 kcal
        assertEquals(200.0, nutriments["calories"]!!.jsonPrimitive.double, "200g x 100kcal/100g = 200 kcal")
        // 200g x 10g/100g = 20g proteines
        assertEquals(20.0, nutriments["proteines"]!!.jsonPrimitive.double, "200g x 10g/100g = 20g proteines")
        // 200g x 20g/100g = 40g glucides
        assertEquals(40.0, nutriments["glucides"]!!.jsonPrimitive.double, "200g x 20g/100g = 40g glucides")
        // 200g x 5g/100g = 10g lipides
        assertEquals(10.0, nutriments["lipides"]!!.jsonPrimitive.double, "200g x 5g/100g = 10g lipides")
    }

    // ===== TEST 2 : GET /dashboard apres ajout retourne les entries =====

    @Test
    fun `GET dashboard apres ajout retourne les entries`() = withTestApp {
        seedTestAliment()
        val client = createJsonClient()
        val token = registerAndGetToken(client, "dashboard-test@example.com")
        createProfile(client, token)

        // Ajouter une entry
        val addResponse = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-10","mealType":"DEJEUNER","alimentId":"test-aliment-001","quantiteGrammes":150.0}""")
        }
        assertEquals(HttpStatusCode.Created, addResponse.status)

        // Recalculer les quotas pour eviter un 500
        client.post("/api/v1/quotas/recalculate") {
            bearerAuth(token)
        }

        // Verifier le dashboard
        val dashResponse = client.get("/api/v1/dashboard?date=2026-04-10") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, dashResponse.status, "Dashboard devrait retourner 200")
        val dashboard = json.parseToJsonElement(dashResponse.bodyAsText()).jsonObject

        val journalDuJour = dashboard["journalDuJour"]!!.jsonArray
        assertEquals(1, journalDuJour.size, "Dashboard devrait contenir 1 entry")
        assertEquals("Aliment Test", journalDuJour[0].jsonObject["nom"]!!.jsonPrimitive.content)

        val nutriments = journalDuJour[0].jsonObject["nutrimentsCalcules"]!!.jsonObject
        assertTrue(nutriments["calories"]!!.jsonPrimitive.double > 0, "Les calories devraient etre > 0")
    }

    // ===== TEST 3 : GET /dashboard sans quota retourne quand meme le journal =====

    @Test
    fun `GET dashboard sans quota retourne quand meme le journal`() = withTestApp {
        seedTestAliment()
        val client = createJsonClient()
        val token = registerAndGetToken(client, "no-quota-test@example.com")
        createProfile(client, token)

        // Ajouter une entry SANS recalculer les quotas
        val addResponse = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-10","mealType":"DINER","alimentId":"test-aliment-001","quantiteGrammes":100.0}""")
        }
        assertEquals(HttpStatusCode.Created, addResponse.status)

        // Dashboard sans quotas prealables - ne devrait PAS retourner 500
        val dashResponse = client.get("/api/v1/dashboard?date=2026-04-10") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, dashResponse.status, "Dashboard sans quotas ne devrait PAS retourner 500")
        val dashboard = json.parseToJsonElement(dashResponse.bodyAsText()).jsonObject

        val journalDuJour = dashboard["journalDuJour"]!!.jsonArray
        assertEquals(1, journalDuJour.size, "Le journal devrait contenir 1 entry meme sans quotas")

        assertTrue(dashboard.containsKey("quotasStatus"), "quotasStatus devrait exister")
        val quotasStatus = dashboard["quotasStatus"]!!.jsonArray
        // quotasStatus peut etre vide ou rempli, mais ne doit pas causer d'erreur
        assertTrue(quotasStatus.size >= 0, "quotasStatus devrait etre un array")
    }

    // ===== TEST 4 : POST /journal avec aliment inexistant retourne 404 =====

    @Test
    fun `POST journal avec aliment inexistant retourne 404`() = withTestApp {
        val client = createJsonClient()
        val token = registerAndGetToken(client, "notfound-test@example.com")
        createProfile(client, token)

        val response = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-10","mealType":"DEJEUNER","alimentId":"nonexistent","quantiteGrammes":100.0}""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status, "Aliment inexistant devrait retourner 404")
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val error = body["error"]!!.jsonObject
        assertTrue(error["message"]!!.jsonPrimitive.content.contains("non trouve", ignoreCase = true),
            "Le message d'erreur devrait indiquer que l'aliment n'est pas trouve")
    }

    // ===== TEST 5 : POST /journal avec quantite invalide retourne 400 =====

    @Test
    fun `POST journal avec quantite negative retourne 400`() = withTestApp {
        seedTestAliment()
        val client = createJsonClient()
        val token = registerAndGetToken(client, "invalid-qty-test@example.com")
        createProfile(client, token)

        val response = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-10","mealType":"DEJEUNER","alimentId":"test-aliment-001","quantiteGrammes":-50.0}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status, "Quantite negative devrait retourner 400")
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val error = body["error"]!!.jsonObject
        assertTrue(error["message"]!!.jsonPrimitive.content.isNotBlank(), "Le message d'erreur ne devrait pas etre vide")
    }

    // ===== TEST 6 : GET /recettes/{id} retourne les ingredients et etapes =====

    @Test
    fun `GET recettes par id retourne ingredients et etapes`() = withTestApp {
        seedTestRecette()
        val client = createJsonClient()
        val token = registerAndGetToken(client, "recette-test@example.com")

        val response = client.get("/api/v1/recettes/test-recette-001") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status, "GET /recettes/{id} devrait retourner 200")
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verifier ingredients
        val ingredients = body["ingredients"]!!.jsonArray
        assertTrue(ingredients.size > 0, "La recette devrait avoir au moins 1 ingredient")
        val firstIngredient = ingredients[0].jsonObject
        assertTrue(firstIngredient["alimentNom"]!!.jsonPrimitive.content.isNotBlank(),
            "Le nom de l'aliment ingredient ne devrait pas etre vide")

        // Verifier etapes
        val etapes = body["etapes"]!!.jsonArray
        assertTrue(etapes.size > 0, "La recette devrait avoir au moins 1 etape")

        // Verifier nutriments totaux
        val nutrimentsTotaux = body["nutrimentsTotaux"]!!.jsonObject
        assertTrue(nutrimentsTotaux["calories"]!!.jsonPrimitive.double > 0, "Les calories totales devraient etre > 0")
        assertEquals(400.0, nutrimentsTotaux["calories"]!!.jsonPrimitive.double, "Calories totales = 400")
    }
}
