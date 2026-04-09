package com.appfood.backend

import com.appfood.backend.database.tables.UsersTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HydratationRoutesTest {
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
    fun `should return unauthorized when accessing hydratation without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/hydratation")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when posting hydratation without token`() =
        withTestApp {
            val client = createJsonClient()
            val response =
                client.post("/api/v1/hydratation") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","quantiteMl":250}""")
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // --- POST /api/v1/hydratation ---

    @Test
    fun `should add hydratation entry successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/hydratation") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","quantiteMl":250}""")
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("2025-03-15", body["date"]!!.jsonPrimitive.content)
            assertEquals(250, body["quantiteMl"]!!.jsonPrimitive.int)
            assertNotNull(body["objectifMl"])
            assertNotNull(body["pourcentage"])
            assertNotNull(body["entrees"])
        }

    @Test
    fun `should accumulate hydratation entries on same day`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — add two entries on the same day
            client.post("/api/v1/hydratation") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-15","quantiteMl":250}""")
            }

            val response =
                client.post("/api/v1/hydratation") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","quantiteMl":300}""")
                }

            // Then — total should be 550
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(550, body["quantiteMl"]!!.jsonPrimitive.int)
        }

    @Test
    fun `should return error when adding hydratation with zero quantity`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/hydratation") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"2025-03-15","quantiteMl":0}""")
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `should return error when adding hydratation with invalid date`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/hydratation") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"date":"not-a-date","quantiteMl":250}""")
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // --- GET /api/v1/hydratation?date=... ---

    @Test
    fun `should get daily hydratation for specific date`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Add an entry
            client.post("/api/v1/hydratation") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-15","quantiteMl":500}""")
            }

            // When
            val response =
                client.get("/api/v1/hydratation?date=2025-03-15") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("2025-03-15", body["date"]!!.jsonPrimitive.content)
            assertEquals(500, body["quantiteMl"]!!.jsonPrimitive.int)
        }

    @Test
    fun `should return empty day when no hydratation exists for date`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — query a date with no entries
            val response =
                client.get("/api/v1/hydratation?date=2025-01-01") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(0, body["quantiteMl"]!!.jsonPrimitive.int)
        }

    @Test
    fun `should return error when querying hydratation with invalid date`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/hydratation?date=invalid") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // --- PUT /api/v1/hydratation/objectif ---

    @Test
    fun `should update hydratation objective successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.put("/api/v1/hydratation/objectif") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"objectifMl":2500}""")
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(2500, body["objectifMl"]!!.jsonPrimitive.int)
            assertEquals("true", body["estObjectifPersonnalise"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return error when updating objective with zero value`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.put("/api/v1/hydratation/objectif") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"objectifMl":0}""")
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // --- GET /api/v1/hydratation/weekly?weekOf=... ---

    @Test
    fun `should get weekly hydratation summary`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Add entries on different days
            client.post("/api/v1/hydratation") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-10","quantiteMl":1500}""")
            }
            client.post("/api/v1/hydratation") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"date":"2025-03-11","quantiteMl":2000}""")
            }

            // When — 2025-03-10 is a Monday
            val response =
                client.get("/api/v1/hydratation/weekly?weekOf=2025-03-10") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertNotNull(body["dateFrom"])
            assertNotNull(body["dateTo"])
            assertNotNull(body["moyenneJournaliereMl"])
            assertNotNull(body["objectifMl"])
            assertNotNull(body["parJour"])
            val parJour = body["parJour"]!!.jsonObject
            assertTrue(parJour.size == 7, "Weekly response should have 7 days")
        }

    // --- POST /api/v1/hydratation/objectif/reset ---

    @Test
    fun `should reset hydratation objective to default`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // First set a custom objective
            client.put("/api/v1/hydratation/objectif") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody("""{"objectifMl":3000}""")
            }

            // When — reset to auto
            val response =
                client.post("/api/v1/hydratation/objectif/reset") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("false", body["estObjectifPersonnalise"]!!.jsonPrimitive.content)
        }
}
