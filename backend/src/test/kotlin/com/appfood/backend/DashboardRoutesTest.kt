package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.database.tables.QuotasTable
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.database.tables.UserProfilesTable
import com.appfood.backend.database.tables.UsersTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
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

class DashboardRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun seedFullUser(userId: String = TEST_USER_ID) {
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
                it[UserProfilesTable.sexe] = Sexe.FEMME
                it[UserProfilesTable.age] = 28
                it[UserProfilesTable.poidsKg] = 60.0
                it[UserProfilesTable.tailleCm] = 165
                it[UserProfilesTable.regimeAlimentaire] = RegimeAlimentaire.VEGETARIEN
                it[UserProfilesTable.niveauActivite] = NiveauActivite.ACTIF
                it[UserProfilesTable.onboardingComplete] = true
                it[UserProfilesTable.updatedAt] = now
            }
        }
        // Seed minimal quotas so the dashboard can compute status
        transaction {
            listOf(
                Triple(NutrimentType.CALORIES, 2000.0, "kcal"),
                Triple(NutrimentType.PROTEINES, 60.0, "g"),
            ).forEach { (nutriment, cible, unite) ->
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
    fun `should return unauthorized when accessing dashboard without token`() = withTestApp {
        // Given
        val client = createJsonClient()

        // When
        val response = client.get("/api/v1/dashboard")

        // Then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `should get dashboard successfully`() = withTestApp {
        // Given
        seedFullUser()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.get("/api/v1/dashboard?date=2026-04-08") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("2026-04-08", body["date"]!!.jsonPrimitive.content)
        assertTrue(body.containsKey("quotasStatus"))
        assertTrue(body.containsKey("journalDuJour"))
        assertTrue(body.containsKey("recommandationsAliments"))
        assertTrue(body.containsKey("recommandationsRecettes"))
    }

    @Test
    fun `should get dashboard with default date when no date param`() = withTestApp {
        // Given
        seedFullUser()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.get("/api/v1/dashboard") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body.containsKey("date"))
    }

    @Test
    fun `should return error for invalid date in dashboard`() = withTestApp {
        // Given
        seedFullUser()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.get("/api/v1/dashboard?date=not-valid") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `should return dashboard with empty journal when no entries exist`() = withTestApp {
        // Given
        seedFullUser()
        val client = createJsonClient()
        val token = createTestToken()

        // When
        val response = client.get("/api/v1/dashboard?date=2025-01-01") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val journal = body["journalDuJour"]!!.jsonArray
        assertEquals(0, journal.size)
    }

    @Test
    fun `should get dashboard with journal entries`() = withTestApp {
        // Given
        seedFullUser()
        val now = Clock.System.now()
        // Seed an aliment
        transaction {
            AlimentsTable.insert {
                it[AlimentsTable.id] = "aliment-dash-001"
                it[AlimentsTable.nom] = "Lentilles corail"
                it[AlimentsTable.marque] = null
                it[AlimentsTable.sourceAliment] = SourceAliment.CIQUAL
                it[AlimentsTable.sourceId] = "ciqual-dash-001"
                it[AlimentsTable.codeBarres] = null
                it[AlimentsTable.categorie] = "Legumineuses"
                it[AlimentsTable.regimesCompatibles] = "VEGAN,VEGETARIEN"
                it[AlimentsTable.calories] = 116.0
                it[AlimentsTable.proteines] = 9.0
                it[AlimentsTable.glucides] = 20.0
                it[AlimentsTable.lipides] = 0.4
                it[AlimentsTable.fibres] = 1.8
                it[AlimentsTable.sel] = 0.01
                it[AlimentsTable.sucres] = 1.5
                it[AlimentsTable.fer] = 3.3
                it[AlimentsTable.calcium] = 25.0
                it[AlimentsTable.zinc] = 1.3
                it[AlimentsTable.magnesium] = 36.0
                it[AlimentsTable.vitamineB12] = 0.0
                it[AlimentsTable.vitamineD] = 0.0
                it[AlimentsTable.vitamineC] = 1.5
                it[AlimentsTable.omega3] = 0.1
                it[AlimentsTable.omega6] = 0.1
            }
        }

        val client = createJsonClient()
        val token = createTestToken()

        // Add a journal entry
        client.post("/api/v1/journal") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"date":"2026-04-08","mealType":"DEJEUNER","alimentId":"aliment-dash-001","quantiteGrammes":200.0}""")
        }

        // When
        val response = client.get("/api/v1/dashboard?date=2026-04-08") {
            bearerAuth(token)
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val journal = body["journalDuJour"]!!.jsonArray
        assertTrue(journal.size >= 1)
    }

    @Test
    fun `should return expired token error on dashboard`() = withTestApp {
        // Given
        val client = createJsonClient()
        val expiredToken = createExpiredToken()

        // When
        val response = client.get("/api/v1/dashboard") {
            bearerAuth(expiredToken)
        }

        // Then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
