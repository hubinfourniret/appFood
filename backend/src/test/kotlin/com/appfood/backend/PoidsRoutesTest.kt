package com.appfood.backend

import com.appfood.backend.database.tables.UsersTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PoidsRoutesTest {
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

    // --- Unauthorized access ---

    @Test
    fun `should return unauthorized when accessing poids without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/poids")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when posting poids without token`() =
        withTestApp {
            val client = createJsonClient()
            val response =
                client.post("/api/v1/poids") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","poidsKg":72.5}""")
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // --- POST /api/v1/poids ---

    @Test
    fun `should add weight entry successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/poids") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","poidsKg":72.5}""")
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val poids = body["poids"]!!.jsonObject
            assertEquals("2025-03-15", poids["date"]!!.jsonPrimitive.content)
            assertNotNull(poids["id"])
            assertNotNull(poids["createdAt"])
            assertNotNull(body["changementSignificatif"])
        }

    @Test
    fun `should mark first weight as reference`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/poids") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","poidsKg":72.5}""")
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val poids = body["poids"]!!.jsonObject
            assertEquals("true", poids["estReference"]!!.jsonPrimitive.content)
            assertEquals("false", body["changementSignificatif"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should detect significant weight change`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Add reference weight
            client.post("/api/v1/poids") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-10","poidsKg":72.0}""")
            }

            // When — add weight with >1kg difference
            val response =
                client.post("/api/v1/poids") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","poidsKg":74.5}""")
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("true", body["changementSignificatif"]!!.jsonPrimitive.content)
            assertNotNull(body["messageRecalcul"])
        }

    @Test
    fun `should return error when adding weight with zero value`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/poids") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","poidsKg":0.0}""")
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `should return error when adding weight with invalid date`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/poids") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"invalid-date","poidsKg":72.5}""")
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // --- GET /api/v1/poids ---

    @Test
    fun `should get weight history`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Add two entries
            client.post("/api/v1/poids") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-10","poidsKg":72.0}""")
            }
            client.post("/api/v1/poids") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-15","poidsKg":71.5}""")
            }

            // When
            val response =
                client.get("/api/v1/poids") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(2, data.size)
            assertEquals(2, body["total"]!!.jsonPrimitive.int)
            assertNotNull(body["poidsCourant"])
            assertNotNull(body["poidsMin"])
            assertNotNull(body["poidsMax"])
        }

    @Test
    fun `should get weight history filtered by date range`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Add entries on different dates
            client.post("/api/v1/poids") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-01-01","poidsKg":75.0}""")
            }
            client.post("/api/v1/poids") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-15","poidsKg":72.0}""")
            }
            client.post("/api/v1/poids") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-06-01","poidsKg":70.0}""")
            }

            // When — filter March only
            val response =
                client.get("/api/v1/poids?dateFrom=2025-03-01&dateTo=2025-03-31") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(1, data.size)
        }

    @Test
    fun `should return empty history when no weights exist`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/poids") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(0, data.size)
            assertEquals(0, body["total"]!!.jsonPrimitive.int)
        }

    // --- DELETE /api/v1/poids/{id} ---

    @Test
    fun `should delete weight entry successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Add an entry and get its ID
            val addResponse =
                client.post("/api/v1/poids") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","poidsKg":72.5}""")
                }
            val addBody = json.parseToJsonElement(addResponse.bodyAsText()).jsonObject
            val poidsId = addBody["poids"]!!.jsonObject["id"]!!.jsonPrimitive.content

            // When
            val response =
                client.delete("/api/v1/poids/$poidsId") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.NoContent, response.status)

            // Verify it is gone
            val historyResponse =
                client.get("/api/v1/poids") {
                    bearerAuth(token)
                }
            val histBody = json.parseToJsonElement(historyResponse.bodyAsText()).jsonObject
            assertEquals(0, histBody["total"]!!.jsonPrimitive.int)
        }

    @Test
    fun `should return not found when deleting non-existent weight`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.delete("/api/v1/poids/non-existent-id") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
}
