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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests de regression pour l'incident prod P0 :
 * - POST /journal qui restait bloque cote client (timeout infini)
 * - GET /dashboard en timeout a cause de Meilisearch OOM
 *
 * Ces tests verifient que ces endpoints restent rapides et degradent
 * gracieusement meme sans services externes.
 */
class JournalPerformanceE2ETest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun seedAliments() {
        transaction {
            AlimentsTable.insert {
                it[id] = "ciqual-tofu-perf"
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
        }
    }

    private suspend fun registerAndProfile(
        client: io.ktor.client.HttpClient,
        email: String,
    ): String {
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"firebaseToken":"perf-$email:$email","email":"$email","nom":"Perf","prenom":"Test"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val token = json.parseToJsonElement(registerResponse.bodyAsText())
            .jsonObject["token"]!!.jsonPrimitive.content

        val profileResponse = client.post("/api/v1/users/me/profile") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"sexe":"HOMME","age":30,"poidsKg":70.0,"tailleCm":175,"regimeAlimentaire":"VEGAN","niveauActivite":"ACTIF"}""")
        }
        assertEquals(HttpStatusCode.Created, profileResponse.status)
        return token
    }

    @Test
    fun `POST journal should respond quickly even without external services`() = withTestApp {
        seedAliments()
        val client = createJsonClient()
        val token = registerAndProfile(client, "journal-perf@test.com")

        val start = System.currentTimeMillis()
        val response = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-09","mealType":"DEJEUNER","alimentId":"ciqual-tofu-perf","quantiteGrammes":200.0}""")
        }
        val elapsedMs = System.currentTimeMillis() - start

        assertEquals(HttpStatusCode.Created, response.status, "POST /journal devrait retourner 201")
        assertTrue(
            elapsedMs < 2000,
            "POST /journal devrait repondre en < 2000ms (observe: ${elapsedMs}ms) — ne doit JAMAIS dependre de Meilisearch/RecommandationService",
        )
    }

    @Test
    fun `GET dashboard should degrade gracefully without Meilisearch`() = withTestApp {
        seedAliments()
        val client = createJsonClient()
        val token = registerAndProfile(client, "dashboard-degrade@test.com")

        client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-09","mealType":"DEJEUNER","alimentId":"ciqual-tofu-perf","quantiteGrammes":150.0}""")
        }

        val start = System.currentTimeMillis()
        val response = client.get("/api/v1/dashboard?date=2026-04-09") {
            bearerAuth(token)
        }
        val elapsedMs = System.currentTimeMillis() - start

        assertEquals(
            HttpStatusCode.OK,
            response.status,
            "Dashboard doit repondre 200 meme si les recommandations echouent",
        )
        assertTrue(
            elapsedMs < 3000,
            "GET /dashboard devrait repondre en < 3000ms (observe: ${elapsedMs}ms) — degradation gracieuse requise",
        )

        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(
            body.containsKey("recommandationsAliments"),
            "Dashboard doit contenir le champ recommandationsAliments meme vide",
        )
        assertNotNull(body["recommandationsAliments"]?.jsonArray)
    }

    @Test
    fun `GET dashboard with journal entries should return quotas status and journal`() = withTestApp {
        seedAliments()
        val client = createJsonClient()
        val token = registerAndProfile(client, "dashboard-quotas@test.com")

        val add1 = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-09","mealType":"PETIT_DEJEUNER","alimentId":"ciqual-tofu-perf","quantiteGrammes":100.0}""")
        }
        assertEquals(HttpStatusCode.Created, add1.status)

        val add2 = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-09","mealType":"DINER","alimentId":"ciqual-tofu-perf","quantiteGrammes":250.0}""")
        }
        assertEquals(HttpStatusCode.Created, add2.status)

        val recalcResponse = client.post("/api/v1/quotas/recalculate") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, recalcResponse.status)

        val response = client.get("/api/v1/dashboard?date=2026-04-09") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("2026-04-09", body["date"]!!.jsonPrimitive.content)

        val quotasStatus = body["quotasStatus"]!!.jsonArray
        assertTrue(
            quotasStatus.isNotEmpty(),
            "quotasStatus devrait etre non vide apres POST /quotas/recalculate",
        )

        val journalDuJour = body["journalDuJour"]!!.jsonArray
        assertEquals(
            2,
            journalDuJour.size,
            "journalDuJour devrait contenir les 2 entrees ajoutees",
        )

        assertTrue(
            body.containsKey("poidsCourant"),
            "Dashboard doit contenir le champ poidsCourant (null ou valeur)",
        )
    }
}
