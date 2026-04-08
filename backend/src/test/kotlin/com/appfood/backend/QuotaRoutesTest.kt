package com.appfood.backend

import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.database.tables.QuotasTable
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.database.tables.UserProfilesTable
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
import kotlin.test.assertTrue

class QuotaRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun seedUserWithProfile(userId: String = TEST_USER_ID) {
        val now = Clock.System.now()
        transaction {
            UsersTable.insert {
                it[UsersTable.id] = userId
                it[UsersTable.email] = TEST_USER_EMAIL
                it[UsersTable.nom] = "Test"
                it[UsersTable.prenom] = "User"
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
            UserProfilesTable.insert {
                it[UserProfilesTable.userId] = userId
                it[UserProfilesTable.sexe] = Sexe.HOMME
                it[UserProfilesTable.age] = "30"
                it[UserProfilesTable.poidsKg] = "75.0"
                it[UserProfilesTable.tailleCm] = "178"
                it[UserProfilesTable.regimeAlimentaire] = RegimeAlimentaire.VEGAN
                it[UserProfilesTable.niveauActivite] = NiveauActivite.MODERE
                it[UserProfilesTable.onboardingComplete] = true
                it[UserProfilesTable.updatedAt] = now
            }
        }
    }

    private fun seedQuotas(userId: String = TEST_USER_ID) {
        val now = Clock.System.now()
        transaction {
            // Insert a few quotas for testing
            val quotas =
                listOf(
                    Triple(NutrimentType.CALORIES, 2500.0, "kcal"),
                    Triple(NutrimentType.PROTEINES, 75.0, "g"),
                    Triple(NutrimentType.GLUCIDES, 300.0, "g"),
                    Triple(NutrimentType.LIPIDES, 80.0, "g"),
                    Triple(NutrimentType.FER, 18.0, "mg"),
                    Triple(NutrimentType.VITAMINE_B12, 2.5, "ug"),
                )
            quotas.forEach { (nutriment, cible, unite) ->
                QuotasTable.insert {
                    it[QuotasTable.userId] = userId
                    it[QuotasTable.nutriment] = nutriment
                    it[QuotasTable.valeurCible] = cible
                    it[QuotasTable.estPersonnalise] = false
                    it[QuotasTable.valeurCalculee] = cible
                    it[QuotasTable.unite] = unite
                    it[QuotasTable.updatedAt] = now
                }
            }
        }
    }

    @Test
    fun `should return unauthorized when accessing quotas without token`() =
        withTestApp {
            // Given
            val client = createJsonClient()

            // When
            val response = client.get("/api/v1/quotas/status")

            // Then
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `should get quota status successfully`() =
        withTestApp {
            // Given
            seedUserWithProfile()
            seedQuotas()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/quotas/status") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("date"))
            val data = body["data"]!!.jsonArray
            assertTrue(data.size >= 1)
            // Each status should have required fields
            val first = data[0].jsonObject
            assertTrue(first.containsKey("nutriment"))
            assertTrue(first.containsKey("valeurCible"))
            assertTrue(first.containsKey("valeurConsommee"))
            assertTrue(first.containsKey("pourcentage"))
            assertTrue(first.containsKey("unite"))
        }

    @Test
    fun `should get quota status for specific date`() =
        withTestApp {
            // Given
            seedUserWithProfile()
            seedQuotas()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/quotas/status?date=2026-04-08") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("2026-04-08", body["date"]!!.jsonPrimitive.content)
        }

    @Test
    fun `should return not found when no quotas exist`() =
        withTestApp {
            // Given — user exists but no quotas
            val userId = "user-no-quotas"
            val now = Clock.System.now()
            transaction {
                UsersTable.insert {
                    it[UsersTable.id] = userId
                    it[UsersTable.email] = "noquotas@example.com"
                    it[UsersTable.nom] = "No"
                    it[UsersTable.prenom] = "Quotas"
                    it[UsersTable.createdAt] = now
                    it[UsersTable.updatedAt] = now
                }
            }
            val client = createJsonClient()
            val token = createTestToken(userId)

            // When
            val response =
                client.get("/api/v1/quotas/status") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `should return error for invalid date format in quota status`() =
        withTestApp {
            // Given
            seedUserWithProfile()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/quotas/status?date=invalid") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun `should get all quotas`() =
        withTestApp {
            // Given
            seedUserWithProfile()
            seedQuotas()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.get("/api/v1/quotas") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertEquals(6, data.size)
        }

    @Test
    fun `should recalculate quotas`() =
        withTestApp {
            // Given
            seedUserWithProfile()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.post("/api/v1/quotas/recalculate") {
                    bearerAuth(token)
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            assertTrue(data.size > 0)
        }

    @Test
    fun `should update a specific quota`() =
        withTestApp {
            // Given
            seedUserWithProfile()
            seedQuotas()
            val client = createJsonClient()
            val token = createTestToken()

            // When
            val response =
                client.put("/api/v1/quotas/CALORIES") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody("""{"valeurCible":3000.0}""")
                }

            // Then
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("CALORIES", body["nutriment"]!!.jsonPrimitive.content)
        }
}
