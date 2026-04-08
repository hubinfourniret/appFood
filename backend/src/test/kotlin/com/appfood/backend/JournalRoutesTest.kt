package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.MealType
import com.appfood.backend.database.tables.SourceAliment
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
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JournalRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun seedTestUser(userId: String = TEST_USER_ID, email: String = TEST_USER_EMAIL) {
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

    private fun seedTestAliment(alimentId: String = "aliment-001") {
        transaction {
            AlimentsTable.insert {
                it[AlimentsTable.id] = alimentId
                it[AlimentsTable.nom] = "Tofu ferme"
                it[AlimentsTable.marque] = null
                it[AlimentsTable.sourceAliment] = SourceAliment.CIQUAL
                it[AlimentsTable.sourceId] = "ciqual-001"
                it[AlimentsTable.codeBarres] = null
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

    @Test
    fun `should add journal entry successfully`() = withTestApp {
        // Given
        seedTestUser()
        seedTestAliment()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"PETIT_DEJEUNER","alimentId":"aliment-001","quantiteGrammes":150.0}""")
        }

        // Then
        assertEquals(HttpStatusCode.Created, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val data = body["data"]!!.jsonObject
        assertEquals("2026-04-08", data["date"]!!.jsonPrimitive.content)
        assertEquals("PETIT_DEJEUNER", data["mealType"]!!.jsonPrimitive.content)
        assertEquals("Tofu ferme", data["nom"]!!.jsonPrimitive.content)
    }

    @Test
    fun `should return unauthorized when adding entry without token`() = withTestApp {
        // Given
        val client = createJsonClient()

        // When
        val response = client.post("/api/v1/journal") {
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"DEJEUNER","alimentId":"aliment-001","quantiteGrammes":100.0}""")
        }

        // Then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should list journal entries for a date`() = withTestApp {
        // Given
        seedTestUser()
        seedTestAliment()
        val client = createJsonClient()
        val token = createTestToken()

        // Add an entry
        client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"DEJEUNER","alimentId":"aliment-001","quantiteGrammes":200.0}""")
        }

        // When
        val response = client.get("/api/v1/journal?date=2026-04-08") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val data = body["data"]!!.jsonArray
        assertTrue(data.size >= 1)
        assertEquals(data.size, body["total"]!!.jsonPrimitive.int)
    }

    @Test
    fun `should return empty list for date with no entries`() = withTestApp {
        // Given
        seedTestUser()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.get("/api/v1/journal?date=2025-01-01") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val data = body["data"]!!.jsonArray
        assertEquals(0, data.size)
    }

    @Test
    fun `should return error for invalid date format`() = withTestApp {
        // Given
        seedTestUser()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.get("/api/v1/journal?date=not-a-date") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val error = body["error"]!!.jsonObject
        assertEquals("VALIDATION_ERROR", error["code"]!!.jsonPrimitive.content)
    }

    @Test
    fun `should return error for invalid meal type`() = withTestApp {
        // Given
        seedTestUser()
        seedTestAliment()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"INVALID_MEAL","alimentId":"aliment-001","quantiteGrammes":100.0}""")
        }

        // Then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `should delete journal entry`() = withTestApp {
        // Given
        seedTestUser()
        seedTestAliment()
        val client = createJsonClient()
        val token = createTestToken()

        // Add an entry
        val addResponse = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"DINER","alimentId":"aliment-001","quantiteGrammes":100.0}""")
        }
        assertEquals(HttpStatusCode.Created, addResponse.status)
        val entryId = json.parseToJsonElement(addResponse.bodyAsText())
            .jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content

        // When
        val deleteResponse = client.delete("/api/v1/journal/$entryId") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
    }

    @Test
    fun `should update journal entry`() = withTestApp {
        // Given
        seedTestUser()
        seedTestAliment()
        val client = createJsonClient()
        val token = createTestToken()

        // Add an entry
        val addResponse = client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"COLLATION","alimentId":"aliment-001","quantiteGrammes":50.0}""")
        }
        val entryId = json.parseToJsonElement(addResponse.bodyAsText())
            .jsonObject["data"]!!.jsonObject["id"]!!.jsonPrimitive.content

        // When
        val updateResponse = client.put("/api/v1/journal/$entryId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"quantiteGrammes":200.0}""")
        }

        // Then
        assertEquals(HttpStatusCode.OK, updateResponse.status)
    }

    @Test
    fun `should get daily summary`() = withTestApp {
        // Given
        seedTestUser()
        seedTestAliment()
        val client = createJsonClient()
        val token = createTestToken()

        // Add an entry
        client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"DEJEUNER","alimentId":"aliment-001","quantiteGrammes":100.0}""")
        }

        // When
        val response = client.get("/api/v1/journal/summary?date=2026-04-08") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("2026-04-08", body["date"]!!.jsonPrimitive.content)
        assertTrue(body.containsKey("totalNutriments"))
        assertTrue(body.containsKey("nbEntrees"))
    }
}
