package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.SourceAliment
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test e2e du parcours utilisateur complet :
 * Register → Create profile → Search aliment → Add journal entry → Check dashboard
 *
 * Si ce test passe, l'app fonctionne de bout en bout.
 */
class UserJourneyE2ETest {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Seed un aliment réaliste (Tofu) dans la base de test.
     * En prod, c'est l'import Ciqual qui remplit la base.
     * La recherche Meilisearch n'est pas disponible en test (H2),
     * donc on teste via l'ID direct.
     */
    private fun seedAliments() {
        transaction {
            AlimentsTable.insert {
                it[id] = "ciqual-tofu-001"
                it[nom] = "Tofu ferme nature"
                it[marque] = null
                it[sourceAliment] = SourceAliment.CIQUAL
                it[sourceId] = "20904"
                it[codeBarres] = null
                it[categorie] = "Legumineuses et produits derives"
                it[regimesCompatibles] = "VEGAN,VEGETARIEN"
                it[calories] = 144.0
                it[proteines] = 15.0
                it[glucides] = 2.0
                it[lipides] = 8.5
                it[fibres] = 1.2
                it[sel] = 0.05
                it[sucres] = 0.5
                it[fer] = 2.7
                it[calcium] = 350.0
                it[zinc] = 1.5
                it[magnesium] = 60.0
                it[vitamineB12] = 0.0
                it[vitamineD] = 0.0
                it[vitamineC] = 0.0
                it[omega3] = 0.5
                it[omega6] = 3.0
            }
            AlimentsTable.insert {
                it[id] = "ciqual-lentilles-002"
                it[nom] = "Lentilles corail cuites"
                it[marque] = null
                it[sourceAliment] = SourceAliment.CIQUAL
                it[sourceId] = "20505"
                it[codeBarres] = null
                it[categorie] = "Legumineuses et produits derives"
                it[regimesCompatibles] = "VEGAN,VEGETARIEN"
                it[calories] = 116.0
                it[proteines] = 9.0
                it[glucides] = 20.0
                it[lipides] = 0.4
                it[fibres] = 1.8
                it[sel] = 0.01
                it[sucres] = 1.5
                it[fer] = 3.3
                it[calcium] = 25.0
                it[zinc] = 1.3
                it[magnesium] = 36.0
                it[vitamineB12] = 0.0
                it[vitamineD] = 0.0
                it[vitamineC] = 1.5
                it[omega3] = 0.1
                it[omega6] = 0.1
            }
        }
    }

    @Test
    fun `complete user journey - register to dashboard`() = withTestApp {
        seedAliments()
        val client = createJsonClient()

        // ===== ETAPE 1 : Register =====
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firebaseToken":"e2e-journey:journey@test.com","email":"journey@test.com","nom":"Marie","prenom":"Dupont"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status, "Register devrait retourner 201")
        val authBody = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
        val token = authBody["token"]!!.jsonPrimitive.content
        val userId = authBody["user"]!!.jsonObject["id"]!!.jsonPrimitive.content
        assertTrue(token.isNotBlank(), "Le token JWT ne devrait pas etre vide")

        // ===== ETAPE 2 : Creer le profil (onboarding) =====
        val profileResponse = client.post("/api/v1/users/me/profile") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"sexe":"FEMME","age":28,"poidsKg":60.0,"tailleCm":165,"regimeAlimentaire":"VEGETARIEN","niveauActivite":"ACTIF"}""")
        }
        assertEquals(HttpStatusCode.Created, profileResponse.status, "Create profile devrait retourner 201")
        val profileBody = json.parseToJsonElement(profileResponse.bodyAsText()).jsonObject
        assertEquals("FEMME", profileBody["sexe"]!!.jsonPrimitive.content)
        assertEquals(28, profileBody["age"]!!.jsonPrimitive.content.toInt())

        // ===== ETAPE 3 : Verifier le profil complet =====
        val meResponse = client.get("/api/v1/users/me") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, meResponse.status)
        val meBody = json.parseToJsonElement(meResponse.bodyAsText()).jsonObject
        assertEquals("journey@test.com", meBody["user"]!!.jsonObject["email"]!!.jsonPrimitive.content)
        assertTrue(meBody["profile"] != null, "Le profil devrait exister apres l'onboarding")

        // ===== ETAPE 4 : Recuperer un aliment par ID =====
        val alimentResponse = client.get("/api/v1/aliments/ciqual-tofu-001") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, alimentResponse.status, "Get aliment devrait retourner 200")
        val alimentBody = json.parseToJsonElement(alimentResponse.bodyAsText()).jsonObject
        assertEquals("Tofu ferme nature", alimentBody["nom"]!!.jsonPrimitive.content)
        val nutriments100g = alimentBody["nutrimentsPour100g"]!!.jsonObject
        assertTrue(nutriments100g["calories"]!!.jsonPrimitive.double > 0, "L'aliment devrait avoir des calories")

        // ===== ETAPE 5 : Ajouter un repas au journal =====
        val addJournalResponse = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-09","mealType":"DEJEUNER","alimentId":"ciqual-tofu-001","quantiteGrammes":200.0}""")
        }
        assertEquals(HttpStatusCode.Created, addJournalResponse.status, "Ajout journal devrait retourner 201")
        val journalBody = json.parseToJsonElement(addJournalResponse.bodyAsText()).jsonObject
        assertEquals("DEJEUNER", journalBody["mealType"]!!.jsonPrimitive.content)
        assertEquals("Tofu ferme nature", journalBody["nom"]!!.jsonPrimitive.content)
        // Nutriments calculés = 200g / 100g * valeurs
        val nutriments = journalBody["nutrimentsCalcules"]!!.jsonObject
        assertEquals(288.0, nutriments["calories"]!!.jsonPrimitive.double, "200g de tofu = 288 kcal")
        assertEquals(30.0, nutriments["proteines"]!!.jsonPrimitive.double, "200g de tofu = 30g proteines")

        // ===== ETAPE 6 : Ajouter un 2e aliment au meme repas =====
        val addJournal2 = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-09","mealType":"DEJEUNER","alimentId":"ciqual-lentilles-002","quantiteGrammes":150.0}""")
        }
        assertEquals(HttpStatusCode.Created, addJournal2.status)

        // ===== ETAPE 7 : Verifier le journal du jour =====
        val journalListResponse = client.get("/api/v1/journal?date=2026-04-09") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, journalListResponse.status)
        val journalList = json.parseToJsonElement(journalListResponse.bodyAsText()).jsonObject
        val entries = journalList["data"]!!.jsonArray
        assertEquals(2, entries.size, "Le journal devrait contenir 2 entrees")

        // ===== ETAPE 8 : Calculer les quotas =====
        val recalcResponse = client.post("/api/v1/quotas/recalculate") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, recalcResponse.status, "Recalcul quotas devrait retourner 200")
        val quotas = json.parseToJsonElement(recalcResponse.bodyAsText()).jsonObject["data"]!!.jsonArray
        assertTrue(quotas.size >= 10, "Il devrait y avoir au moins 10 nutriments dans les quotas")

        // ===== ETAPE 9 : Verifier le dashboard =====
        val dashboardResponse = client.get("/api/v1/dashboard?date=2026-04-09") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, dashboardResponse.status, "Dashboard devrait retourner 200")
        val dashboard = json.parseToJsonElement(dashboardResponse.bodyAsText()).jsonObject
        assertEquals("2026-04-09", dashboard["date"]!!.jsonPrimitive.content)
        assertTrue(dashboard.containsKey("quotasStatus"), "Dashboard devrait contenir quotasStatus")
        assertTrue(dashboard.containsKey("journalDuJour"), "Dashboard devrait contenir journalDuJour")
        val dashJournal = dashboard["journalDuJour"]!!.jsonArray
        assertEquals(2, dashJournal.size, "Dashboard devrait afficher les 2 repas du jour")
    }

    @Test
    fun `second login after register should work`() = withTestApp {
        val client = createJsonClient()

        // Register
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firebaseToken":"e2e-relogin:relogin@test.com","email":"relogin@test.com"}""")
        }

        // Login avec le meme token
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"firebaseToken":"e2e-relogin:relogin@test.com"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status, "Login apres register devrait marcher")
        val body = json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject
        assertTrue(body.containsKey("token"), "Login devrait retourner un token")
        assertTrue(body.containsKey("user"), "Login devrait retourner les infos user")
    }

    @Test
    fun `login without register should fail with clear message`() = withTestApp {
        val client = createJsonClient()

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"firebaseToken":"unknown-uid:unknown@test.com"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val error = body["error"]!!.jsonObject
        assertTrue(error["message"]!!.jsonPrimitive.content.contains("inscrire", ignoreCase = true),
            "Le message d'erreur devrait guider l'utilisateur vers l'inscription")
    }
}
