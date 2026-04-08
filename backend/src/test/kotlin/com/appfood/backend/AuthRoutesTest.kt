package com.appfood.backend

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should register new user successfully`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When
            val response =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """{"firebaseToken":"test-uid-001:newuser@example.com",""" +
                            """"email":"newuser@example.com","nom":"Dupont","prenom":"Jean"}""",
                    )
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertNotNull(body["token"])
            val user = body["user"]!!.jsonObject
            assertEquals("newuser@example.com", user["email"]!!.jsonPrimitive.content)
            assertEquals("Dupont", user["nom"]!!.jsonPrimitive.content)
            assertEquals("Jean", user["prenom"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should register user without nom and prenom`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When
            val response =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"firebaseToken":"uid-minimal:minimal@example.com","email":"minimal@example.com"}""")
                }

            // Then
            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertNotNull(body["token"])
        }

    @Test
    fun `should return conflict when registering with existing email`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // Register first user
            client.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"firebaseToken":"uid-first:dup@example.com","email":"dup@example.com","nom":"First","prenom":"User"}""")
            }

            // When — register with same email but different uid
            val response =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"firebaseToken":"uid-second:dup@example.com","email":"dup@example.com","nom":"Second","prenom":"User"}""")
                }

            // Then
            assertEquals(HttpStatusCode.Conflict, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val error = body["error"]!!.jsonObject
            assertEquals("CONFLICT", error["code"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return conflict when registering with existing firebase uid`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // Register first user
            client.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"firebaseToken":"same-uid:first@example.com","email":"first@example.com"}""")
            }

            // When — register with same UID but different email
            val response =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"firebaseToken":"same-uid:second@example.com","email":"second@example.com"}""")
                }

            // Then
            assertEquals(HttpStatusCode.Conflict, response.status)
        }

    @Test
    fun `should login successfully after registration`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // Register user
            client.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"firebaseToken":"login-uid:login@example.com","email":"login@example.com","nom":"Login","prenom":"Test"}""")
            }

            // When
            val response =
                client.post("/api/v1/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"firebaseToken":"login-uid:login@example.com"}""")
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertNotNull(body["token"])
            val user = body["user"]!!.jsonObject
            assertEquals("login@example.com", user["email"]!!.jsonPrimitive.content)
            assertEquals("false", user["onboardingComplete"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return unauthorized when logging in with unregistered user`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When — login without prior registration
            val response =
                client.post("/api/v1/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"firebaseToken":"unknown-uid:unknown@example.com"}""")
                }

            // Then
            assertEquals(HttpStatusCode.Unauthorized, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val error = body["error"]!!.jsonObject
            assertEquals("UNAUTHORIZED", error["code"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return error when register body is missing required fields`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When — missing firebaseToken and email
            val response =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"nom":"Test"}""")
                }

            // Then — deserialization error, should be 4xx
            assertTrue(response.status.value in 400..499)
        }

    @Test
    fun `should return unauthorized when accessing protected endpoint without token`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When — try to delete account without auth
            val response = client.delete("/api/v1/auth/account")

            // Then
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when accessing protected endpoint with invalid token`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When — try to delete account with garbage token
            val response =
                client.delete("/api/v1/auth/account") {
                    bearerAuth("this-is-not-a-valid-jwt")
                }

            // Then
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return unauthorized when accessing protected endpoint with expired token`() =
        withTestApp {
            // Given
            val client = createJsonClient()
            val expiredToken = createExpiredToken("some-user")

            // When — try to access protected route with expired token
            val response =
                client.get("/api/v1/users/me") {
                    bearerAuth(expiredToken)
                }

            // Then
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should return valid JWT token that works on protected endpoints`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // Register user
            val registerResponse =
                client.post("/api/v1/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"firebaseToken":"jwt-uid:jwt@example.com","email":"jwt@example.com","nom":"JWT","prenom":"Test"}""")
                }
            assertEquals(HttpStatusCode.Created, registerResponse.status)

            // Extract token from register response
            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            val token = body["token"]!!.jsonPrimitive.content

            // When — use the token to access protected /users/me
            val meResponse =
                client.get("/api/v1/users/me") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, meResponse.status)
            val meBody = json.parseToJsonElement(meResponse.bodyAsText()).jsonObject
            val userData = meBody["user"]!!.jsonObject
            assertEquals("jwt@example.com", userData["email"]!!.jsonPrimitive.content)
        }
}
