package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.database.tables.UsersTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AlimentRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun seedTestUser(
        userId: String = TEST_USER_ID,
        email: String = TEST_USER_EMAIL,
    ) {
        val now = Clock.System.now()
        transaction {
            UsersTable.insert {
                it[UsersTable.id] = userId
                it[UsersTable.email] = email
                it[UsersTable.nom] = "Test"
                it[UsersTable.prenom] = "User"
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
        }
    }

    private fun seedTestAliment(
        alimentId: String = "aliment-001",
        nom: String = "Tofu ferme",
        codeBarres: String? = null,
    ) {
        transaction {
            AlimentsTable.insert {
                it[AlimentsTable.id] = alimentId
                it[AlimentsTable.nom] = nom
                it[AlimentsTable.marque] = null
                it[AlimentsTable.sourceAliment] = SourceAliment.CIQUAL
                it[AlimentsTable.sourceId] = "ciqual-$alimentId"
                it[AlimentsTable.codeBarres] = codeBarres
                it[AlimentsTable.categorie] = "Proteines vegetales"
                it[AlimentsTable.regimesCompatibles] = "VEGAN,VEGETARIEN"
                it[AlimentsTable.calories] = 144.0
                it[AlimentsTable.proteines] = 15.0
                it[AlimentsTable.glucides] = 2.0
                it[AlimentsTable.lipides] = 8.5
                it[AlimentsTable.fibres] = 1.2
                it[AlimentsTable.sel] = 0.05
                it[AlimentsTable.sucres] = 0.5
                it[AlimentsTable.fer] = 2.7
                it[AlimentsTable.calcium] = 350.0
                it[AlimentsTable.zinc] = 1.5
                it[AlimentsTable.magnesium] = 60.0
                it[AlimentsTable.vitamineB12] = 0.0
                it[AlimentsTable.vitamineD] = 0.0
                it[AlimentsTable.vitamineC] = 0.0
                it[AlimentsTable.omega3] = 0.5
                it[AlimentsTable.omega6] = 3.0
            }
        }
    }

    // --- Unauthorized access ---

    @Test
    fun `should return unauthorized when accessing aliments without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/aliments/search?q=tofu")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when getting aliment by id without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/aliments/aliment-001")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // --- GET /api/v1/aliments/{id} ---

    @Test
    fun `should get aliment by id successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            seedTestAliment()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/aliments/aliment-001") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("aliment-001", body["id"]!!.jsonPrimitive.content)
            assertEquals("Tofu ferme", body["nom"]!!.jsonPrimitive.content)
            assertEquals("Proteines vegetales", body["categorie"]!!.jsonPrimitive.content)
            assertNotNull(body["nutrimentsPour100g"])
            val nutrients = body["nutrimentsPour100g"]!!.jsonObject
            assertNotNull(nutrients["calories"])
            assertNotNull(nutrients["proteines"])
        }

    @Test
    fun `should return not found for non-existent aliment`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/aliments/non-existent-id") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    // --- GET /api/v1/aliments/barcode/{code} ---

    @Test
    fun `should get aliment by barcode successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            seedTestAliment(alimentId = "aliment-barcode", nom = "Lait de soja", codeBarres = "3250390000001")
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/aliments/barcode/3250390000001") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("Lait de soja", body["nom"]!!.jsonPrimitive.content)
            assertEquals("3250390000001", body["codeBarres"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return not found for non-existent barcode`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — barcode not in DB, and Open Food Facts is not available in tests
            val response =
                client.get("/api/v1/aliments/barcode/0000000000000") {
                    bearerAuth(token)
                }

            // Then — should return 404 since neither DB nor OFF has it
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    // --- GET /api/v1/aliments/search?q=... ---
    // Note: Search uses Meilisearch which is not available in test environment.
    // We test the validation logic (missing q, q too short) which runs before Meilisearch.

    @Test
    fun `should return error when search query is missing`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — missing 'q' parameter
            val response =
                client.get("/api/v1/aliments/search") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `should return error when search query is too short`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — q has only 1 character
            val response =
                client.get("/api/v1/aliments/search?q=a") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
