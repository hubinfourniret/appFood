package com.appfood.backend

import com.appfood.backend.database.tables.UsersTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
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

class ProfileRoutesTest {
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
    fun `should return unauthorized when accessing profile without token`() =
        withTestApp {
            val client = createJsonClient()
            val response = client.get("/api/v1/users/me")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when creating profile without token`() =
        withTestApp {
            val client = createJsonClient()
            val response =
                client.post("/api/v1/users/me/profile") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"sexe":"HOMME","age":30,"poidsKg":75.0,"tailleCm":180,"regimeAlimentaire":"VEGAN","niveauActivite":"ACTIF"}""",
                    )
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    // --- GET /api/v1/users/me ---

    @Test
    fun `should get user profile after registration`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/users/me") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val user = body["user"]!!.jsonObject
            assertEquals(TEST_USER_EMAIL, user["email"]!!.jsonPrimitive.content)
            assertEquals("Test", user["nom"]!!.jsonPrimitive.content)
            assertEquals("User", user["prenom"]!!.jsonPrimitive.content)
            assertEquals("false", user["onboardingComplete"]!!.jsonPrimitive.content)
        }

    // --- POST /api/v1/users/me/profile ---

    @Test
    fun `should create profile successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/users/me/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"sexe":"HOMME","age":30,"poidsKg":75.0,"tailleCm":180,"regimeAlimentaire":"VEGAN","niveauActivite":"ACTIF"}""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("HOMME", body["sexe"]!!.jsonPrimitive.content)
            assertEquals("30", body["age"]!!.jsonPrimitive.content)
            assertEquals("VEGAN", body["regimeAlimentaire"]!!.jsonPrimitive.content)
            assertEquals("ACTIF", body["niveauActivite"]!!.jsonPrimitive.content)
            assertEquals("true", body["onboardingComplete"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return error when creating profile with invalid sexe`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/users/me/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"sexe":"INVALIDE","age":30,"poidsKg":75.0,"tailleCm":180,"regimeAlimentaire":"VEGAN","niveauActivite":"ACTIF"}""",
                    )
                }

            // Then
            assertTrue(response.status.value in 400..499)
        }

    @Test
    fun `should return error when creating profile with missing fields`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When — missing required fields
            val response =
                client.post("/api/v1/users/me/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"sexe":"HOMME"}""")
                }

            // Then
            assertTrue(response.status.value in 400..499)
        }

    // --- PUT /api/v1/users/me/profile ---

    @Test
    fun `should update profile successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Create profile first
            client.post("/api/v1/users/me/profile") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(
                    """{"sexe":"HOMME","age":30,"poidsKg":75.0,"tailleCm":180,"regimeAlimentaire":"VEGAN","niveauActivite":"ACTIF"}""",
                )
            }

            // When — update age and poids
            val response =
                client.put("/api/v1/users/me/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"age":31,"poidsKg":73.5,"objectifPoids":"PERTE_DE_POIDS"}""")
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("31", body["age"]!!.jsonPrimitive.content)
            assertEquals("PERTE_DE_POIDS", body["objectifPoids"]!!.jsonPrimitive.content)
        }

    // --- PUT /api/v1/users/me/preferences ---

    @Test
    fun `should update preferences successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.put("/api/v1/users/me/preferences") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"alimentsExclus":["gluten","soja"],"allergies":["arachide"],"alimentsFavoris":["tofu","quinoa"]}""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val exclus = body["alimentsExclus"]!!.jsonArray
            assertEquals(2, exclus.size)
            val allergies = body["allergies"]!!.jsonArray
            assertEquals(1, allergies.size)
            val favoris = body["alimentsFavoris"]!!.jsonArray
            assertEquals(2, favoris.size)
        }

    // --- GET /api/v1/users/me/export ---

    @Test
    fun `should export user data successfully`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/users/me/export") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertNotNull(body["user"])
            assertNotNull(body["exportedAt"])
            assertNotNull(body["journalEntries"])
            assertNotNull(body["poidsHistory"])
            assertNotNull(body["hydratation"])
            assertNotNull(body["consentements"])
        }

    // --- DELETE /api/v1/auth/account ---

    @Test
    fun `should delete account successfully`() =
        withTestApp {
            // Given — register via API to set up all data correctly
            val client = createJsonClient()
            val registerResponse =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"firebaseToken":"del-uid:del@example.com","email":"del@example.com","nom":"Del","prenom":"User"}""",
                    )
                }
            assertEquals(HttpStatusCode.Created, registerResponse.status)
            val regBody = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            val token = regBody["token"]!!.jsonPrimitive.content

            // When
            val response =
                client.delete("/api/v1/auth/account") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.NoContent, response.status)

            // Verify the user is gone — accessing /me should fail
            val meResponse =
                client.get("/api/v1/users/me") {
                    bearerAuth(token)
                }
            assertTrue(meResponse.status.value in 400..499)
        }

    // --- Full lifecycle ---

    @Test
    fun `should complete full profile lifecycle`() =
        withTestApp {
            // Given
            seedTestUser()
            val client = createJsonClient()
            val token = createTestToken()

            // Step 1: Get initial state (no profile)
            val meResponse1 =
                client.get("/api/v1/users/me") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, meResponse1.status)
            val me1 = json.parseToJsonElement(meResponse1.bodyAsText()).jsonObject
            assertEquals("false", me1["user"]!!.jsonObject["onboardingComplete"]!!.jsonPrimitive.content)

            // Step 2: Create profile
            val createResponse =
                client.post("/api/v1/users/me/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"sexe":"FEMME","age":25,"poidsKg":55.0,"tailleCm":165,"regimeAlimentaire":"VEGETARIEN","niveauActivite":"MODERE"}""",
                    )
                }
            assertEquals(HttpStatusCode.Created, createResponse.status)

            // Step 3: Verify profile shows in /me
            val meResponse2 =
                client.get("/api/v1/users/me") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, meResponse2.status)
            val me2 = json.parseToJsonElement(meResponse2.bodyAsText()).jsonObject
            assertEquals("true", me2["user"]!!.jsonObject["onboardingComplete"]!!.jsonPrimitive.content)
            assertNotNull(me2["profile"])

            // Step 4: Update profile
            val updateResponse =
                client.put("/api/v1/users/me/profile") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"age":26}""")
                }
            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // Step 5: Set preferences
            val prefResponse =
                client.put("/api/v1/users/me/preferences") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"allergies":["noix"],"alimentsFavoris":["lentilles"]}""")
                }
            assertEquals(HttpStatusCode.OK, prefResponse.status)

            // Step 6: Export data
            val exportResponse =
                client.get("/api/v1/users/me/export") {
                    bearerAuth(token)
                }
            assertEquals(HttpStatusCode.OK, exportResponse.status)
        }
}
