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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConsentRoutesTest {
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
    fun `should return unauthorized when accessing consents without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/consents")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when posting consents without token`() =
        withTestApp {
            val client = createJsonClient()
            val response =
                client.post("/api/v1/consents/initial") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"analytics":true,"publicite":false,"ameliorationService":true,"versionPolitique":"1.0"}""",
                    )
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // --- POST /api/v1/consents/initial ---

    @Test
    fun `should submit initial consents successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/consents/initial") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"analytics":true,"publicite":false,"ameliorationService":true,"versionPolitique":"1.0"}""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(3, data.size)

            // Verify types
            val types = data.map { it.jsonObject["type"]!!.jsonPrimitive.content }.toSet()
            assertTrue(types.contains("ANALYTICS"))
            assertTrue(types.contains("PUBLICITE"))
            assertTrue(types.contains("AMELIORATION_SERVICE"))

            // Verify values
            val analyticsConsent = data.first { it.jsonObject["type"]!!.jsonPrimitive.content == "ANALYTICS" }.jsonObject
            assertEquals("true", analyticsConsent["accepte"]!!.jsonPrimitive.content)
            assertEquals("1.0", analyticsConsent["versionPolitique"]!!.jsonPrimitive.content)

            val pubConsent = data.first { it.jsonObject["type"]!!.jsonPrimitive.content == "PUBLICITE" }.jsonObject
            assertEquals("false", pubConsent["accepte"]!!.jsonPrimitive.content)
        }

    // --- GET /api/v1/consents ---

    @Test
    fun `should get user consents successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Submit initial consents first
            client.post("/api/v1/consents/initial") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(
                    """{"analytics":true,"publicite":true,"ameliorationService":false,"versionPolitique":"1.0"}""",
                )
            }

            // When
            val response =
                client.get("/api/v1/consents") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(3, data.size)
        }

    @Test
    fun `should return empty list when no consents exist`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/consents") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(0, data.size)
        }

    // --- PUT /api/v1/consents/{type} ---

    @Test
    fun `should update specific consent successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Submit initial consents
            client.post("/api/v1/consents/initial") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(
                    """{"analytics":true,"publicite":false,"ameliorationService":true,"versionPolitique":"1.0"}""",
                )
            }

            // When — update PUBLICITE to true with new version
            val response =
                client.put("/api/v1/consents/PUBLICITE") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"accepte":true,"versionPolitique":"1.1"}""")
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("PUBLICITE", body["type"]!!.jsonPrimitive.content)
            assertEquals("true", body["accepte"]!!.jsonPrimitive.content)
            assertEquals("1.1", body["versionPolitique"]!!.jsonPrimitive.content)
            assertNotNull(body["dateConsentement"])
        }

    @Test
    fun `should create consent via update when it does not exist`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — update a consent that was never created (upsert)
            val response =
                client.put("/api/v1/consents/ANALYTICS") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"accepte":true,"versionPolitique":"2.0"}""")
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("ANALYTICS", body["type"]!!.jsonPrimitive.content)
            assertEquals("true", body["accepte"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return error when updating consent with invalid type`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.put("/api/v1/consents/INVALID_TYPE") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"accepte":true,"versionPolitique":"1.0"}""")
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // --- Full lifecycle ---

    @Test
    fun `should complete full consent lifecycle`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Step 1: Verify no consents initially
            val getResponse1 =
                client.get("/api/v1/consents") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, getResponse1.status)
            val data1 = json.parseToJsonElement(getResponse1.bodyAsText()).jsonObject["data"]!!.jsonArray
            assertEquals(0, data1.size)

            // Step 2: Submit initial consents
            val initResponse =
                client.post("/api/v1/consents/initial") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"analytics":true,"publicite":false,"ameliorationService":true,"versionPolitique":"1.0"}""",
                    )
                }
            assertEquals(HttpStatusCode.Created, initResponse.status)

            // Step 3: Verify consents are set
            val getResponse2 =
                client.get("/api/v1/consents") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, getResponse2.status)
            val data2 = json.parseToJsonElement(getResponse2.bodyAsText()).jsonObject["data"]!!.jsonArray
            assertEquals(3, data2.size)

            // Step 4: Update a consent
            val updateResponse =
                client.put("/api/v1/consents/PUBLICITE") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"accepte":true,"versionPolitique":"1.1"}""")
                }
            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // Step 5: Verify the update persisted
            val getResponse3 =
                client.get("/api/v1/consents") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, getResponse3.status)
            val data3 = json.parseToJsonElement(getResponse3.bodyAsText()).jsonObject["data"]!!.jsonArray
            val pubConsent = data3.first { it.jsonObject["type"]!!.jsonPrimitive.content == "PUBLICITE" }.jsonObject
            assertEquals("true", pubConsent["accepte"]!!.jsonPrimitive.content)
        }
}
