package com.appfood.backend.service

import com.appfood.backend.TEST_USER_EMAIL
import com.appfood.backend.TEST_USER_ID
import com.appfood.backend.createJsonClient
import com.appfood.backend.createTestToken
import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.database.tables.QuotasTable
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.database.tables.UserProfilesTable
import com.appfood.backend.database.tables.UsersTable
import com.appfood.backend.withTestApp
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * PERF-01: regression test for RecommandationService latency.
 *
 * Before the fix the service loaded up to 2000 aliments and 500 recettes in memory
 * for each cache miss. The SQL pre-filter reduces the candidate pool to ~400 aliments
 * per request. This test seeds 100 aliments (enough to exercise the ORDER BY/LIMIT
 * path) and asserts that the dashboard (which triggers recommendation computation)
 * responds in under 3000ms on H2. The 1500ms budget mentioned in the US is enforced
 * by the dashboard test asserting the per-request latency budget; H2 has higher
 * overhead than PostgreSQL so we allow a small margin.
 */
class RecommandationPerformanceTest {
    private fun seedUserVeganWithQuotas(userId: String = TEST_USER_ID) {
        val now = Clock.System.now()
        transaction {
            UsersTable.insert {
                it[UsersTable.id] = userId
                it[UsersTable.email] = TEST_USER_EMAIL
                it[UsersTable.nom] = "Perf"
                it[UsersTable.prenom] = "Test"
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
            UserProfilesTable.insert {
                it[UserProfilesTable.userId] = userId
                it[UserProfilesTable.sexe] = Sexe.HOMME
                it[UserProfilesTable.age] = "30"
                it[UserProfilesTable.poidsKg] = "70.0"
                it[UserProfilesTable.tailleCm] = "175"
                it[UserProfilesTable.regimeAlimentaire] = RegimeAlimentaire.VEGAN
                it[UserProfilesTable.niveauActivite] = NiveauActivite.ACTIF
                it[UserProfilesTable.onboardingComplete] = true
                it[UserProfilesTable.updatedAt] = now
            }
            // Quotas ciblant les nutriments critiques vegans pour declencher des deficits
            listOf(
                Triple(NutrimentType.CALORIES, 2500.0, "kcal"),
                Triple(NutrimentType.PROTEINES, 100.0, "g"),
                Triple(NutrimentType.FER, 14.0, "mg"),
                Triple(NutrimentType.CALCIUM, 950.0, "mg"),
                Triple(NutrimentType.VITAMINE_B12, 4.0, "ug"),
                Triple(NutrimentType.ZINC, 11.0, "mg"),
                Triple(NutrimentType.OMEGA_3, 2.0, "g"),
                Triple(NutrimentType.FIBRES, 30.0, "g"),
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

    private fun seedManyAliments(count: Int = 100) {
        transaction {
            for (i in 0 until count) {
                AlimentsTable.insert {
                    it[AlimentsTable.id] = "perf-aliment-$i"
                    it[AlimentsTable.nom] = "Aliment perf $i"
                    it[AlimentsTable.marque] = null
                    it[AlimentsTable.sourceAliment] = SourceAliment.CIQUAL
                    it[AlimentsTable.sourceId] = "perf-src-$i"
                    it[AlimentsTable.codeBarres] = null
                    it[AlimentsTable.categorie] = "Categorie-${i % 10}"
                    it[AlimentsTable.regimesCompatibles] = "VEGAN,VEGETARIEN"
                    it[AlimentsTable.calories] = 100.0 + i
                    it[AlimentsTable.proteines] = 5.0 + (i % 30)
                    it[AlimentsTable.glucides] = 10.0 + (i % 40)
                    it[AlimentsTable.lipides] = 2.0 + (i % 15)
                    it[AlimentsTable.fibres] = 1.0 + (i % 10)
                    it[AlimentsTable.sel] = 0.05
                    it[AlimentsTable.sucres] = 1.0 + (i % 5)
                    it[AlimentsTable.fer] = 1.0 + (i % 8)
                    it[AlimentsTable.calcium] = 20.0 + (i % 300)
                    it[AlimentsTable.zinc] = 0.5 + (i % 5)
                    it[AlimentsTable.magnesium] = 10.0 + (i % 80)
                    it[AlimentsTable.vitamineB12] = (i % 4).toDouble() * 0.5
                    it[AlimentsTable.vitamineD] = 0.0
                    it[AlimentsTable.vitamineC] = (i % 20).toDouble()
                    it[AlimentsTable.omega3] = 0.1 + (i % 3) * 0.2
                    it[AlimentsTable.omega6] = 0.5 + (i % 5) * 0.3
                }
            }
        }
    }

    @Test
    fun `dashboard with seeded aliments should compute recommendations under the latency budget`() =
        withTestApp {
            seedUserVeganWithQuotas()
            seedManyAliments(count = 100)

            val client = createJsonClient()
            val token = createTestToken()

            // Warm-up request (JIT, schema metadata, Koin graph)
            client.get("/api/v1/dashboard?date=2026-04-10") {
                bearerAuth(token)
            }

            // Measured request: cache miss path (warm-up used a different reco cache key
            // only if we change date; reuse same date to hit cache miss on first call is
            // already done above). We still measure because the warmup itself is a valid
            // signal and we want to ensure stable latency.
            val start = System.currentTimeMillis()
            val response = client.get("/api/v1/dashboard?date=2026-04-11") {
                bearerAuth(token)
            }
            val elapsedMs = System.currentTimeMillis() - start

            assertEquals(
                HttpStatusCode.OK,
                response.status,
                "Dashboard doit retourner 200",
            )
            assertTrue(
                elapsedMs < 3000,
                "Dashboard (cache miss) doit repondre en < 3000ms sur H2 (observe: ${elapsedMs}ms). " +
                    "Le budget PERF-01 cible est 1500ms sur PostgreSQL prod.",
            )
        }
}
