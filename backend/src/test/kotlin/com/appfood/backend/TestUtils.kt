package com.appfood.backend

import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.ConsentsTable
import com.appfood.backend.database.tables.FaqTable
import com.appfood.backend.database.tables.FcmTokensTable
import com.appfood.backend.database.tables.HydratationEntriesTable
import com.appfood.backend.database.tables.HydratationTable
import com.appfood.backend.database.tables.IngredientsTable
import com.appfood.backend.database.tables.JournalEntriesTable
import com.appfood.backend.database.tables.NotificationsTable
import com.appfood.backend.database.tables.PoidsHistoryTable
import com.appfood.backend.database.tables.PortionsTable
import com.appfood.backend.database.tables.QuotasTable
import com.appfood.backend.database.tables.RecettesTable
import com.appfood.backend.database.tables.UserPreferencesTable
import com.appfood.backend.database.tables.UserProfilesTable
import com.appfood.backend.database.tables.UsersTable
import com.appfood.backend.di.backendModule
import com.appfood.backend.plugins.configureRouting
import com.appfood.backend.plugins.configureSerialization
import com.appfood.backend.plugins.configureStatusPages
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.plugin.Koin
import java.util.Date

// JWT constants matching the test config
const val TEST_JWT_SECRET = "test-secret-for-integration-tests-only"
const val TEST_JWT_ISSUER = "appfood-api"
const val TEST_JWT_AUDIENCE = "appfood-mobile"

// Default test user
const val TEST_USER_ID = "test-user-001"
const val TEST_USER_EMAIL = "test@example.com"

/**
 * All tables used in the application, matching DatabaseFactory.
 * Some tables (HydratationEntriesTable, IngredientsTable) might not exist
 * as standalone objects depending on codebase state; we include what is available.
 */
private val ALL_TEST_TABLES = arrayOf(
    UsersTable,
    UserProfilesTable,
    UserPreferencesTable,
    AlimentsTable,
    PortionsTable,
    RecettesTable,
    IngredientsTable,
    JournalEntriesTable,
    QuotasTable,
    PoidsHistoryTable,
    HydratationTable,
    HydratationEntriesTable,
    FcmTokensTable,
    NotificationsTable,
    ConsentsTable,
    FaqTable,
)

/**
 * Generates a valid JWT token for test requests.
 */
fun createTestToken(
    userId: String = TEST_USER_ID,
    expiresInMs: Long = 3_600_000L,
): String {
    return JWT.create()
        .withSubject(userId)
        .withIssuer(TEST_JWT_ISSUER)
        .withAudience(TEST_JWT_AUDIENCE)
        .withExpiresAt(Date(System.currentTimeMillis() + expiresInMs))
        .sign(Algorithm.HMAC256(TEST_JWT_SECRET))
}

/**
 * Generates an expired JWT token.
 */
fun createExpiredToken(userId: String = TEST_USER_ID): String {
    return JWT.create()
        .withSubject(userId)
        .withIssuer(TEST_JWT_ISSUER)
        .withAudience(TEST_JWT_AUDIENCE)
        .withExpiresAt(Date(System.currentTimeMillis() - 60_000L))
        .sign(Algorithm.HMAC256(TEST_JWT_SECRET))
}

/**
 * Initializes the H2 in-memory database and creates all tables.
 */
fun initTestDatabase() {
    Database.connect(
        url = "jdbc:h2:mem:testdb_${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        driver = "org.h2.Driver",
        user = "sa",
        password = "",
    )
    transaction {
        SchemaUtils.create(*ALL_TEST_TABLES)
    }
}

/**
 * Configures the test application module with H2 database and all plugins.
 * Auth is configured inline to avoid reading from HOCON config.
 */
fun Application.testModule() {
    install(Koin) {
        modules(
            backendModule(
                meilisearchUrl = "http://localhost:7700",
                meilisearchApiKey = "test-key",
                jwtSecret = TEST_JWT_SECRET,
                jwtIssuer = TEST_JWT_ISSUER,
                jwtAudience = TEST_JWT_AUDIENCE,
            ),
        )
    }

    configureSerialization()

    // Inline CORS for tests (no HOCON config needed)
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowCredentials = true
    }

    configureStatusPages()

    // Inline JWT auth for tests (no HOCON config needed)
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "appfood"
            verifier(
                JWT.require(Algorithm.HMAC256(TEST_JWT_SECRET))
                    .withAudience(TEST_JWT_AUDIENCE)
                    .withIssuer(TEST_JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.subject
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    io.ktor.http.HttpStatusCode.Unauthorized,
                    com.appfood.backend.plugins.ErrorResponse(
                        status = 401,
                        message = "Token invalide ou expire",
                    ),
                )
            }
        }
    }

    // Database is initialized via initTestDatabase() — no configureDatabase() call
    configureRouting()
}

/**
 * Creates an HTTP client with JSON content negotiation for test requests.
 */
fun ApplicationTestBuilder.createJsonClient(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                prettyPrint = false
            })
        }
    }
}

/**
 * Runs a test with the full application configured and an H2 in-memory database.
 */
fun withTestApp(block: suspend ApplicationTestBuilder.() -> Unit) {
    initTestDatabase()
    testApplication {
        application {
            testModule()
        }
        block()
    }
}
