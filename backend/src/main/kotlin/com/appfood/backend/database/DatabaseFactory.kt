package com.appfood.backend.database

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
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val ALL_TABLES =
    arrayOf(
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
 * Configure la connexion a la base de donnees PostgreSQL.
 * Cree les tables manquantes via Exposed SchemaUtils.
 */
fun Application.configureDatabase() {
    val config = environment.config
    val dbUrl = config.property("appfood.database.url").getString()
    val dbUser = config.property("appfood.database.user").getString()
    val dbPassword = config.property("appfood.database.password").getString()

    // Pool de connexions HikariCP — critique pour Railway (latence TCP+TLS+auth
    // a chaque nouvelle connexion). Sans pool, chaque query fait ~1.8s.
    val hikariConfig =
        HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    // Creation des tables manquantes
    val log = environment.log
    transaction {
        SchemaUtils.createMissingTablesAndColumns(*ALL_TABLES)

        // V6 migration: ensure sensitive profile columns are TEXT (not INTEGER).
        // SchemaUtils doesn't alter existing column types, so we fix them manually.
        try {
            exec("ALTER TABLE user_profiles ALTER COLUMN age TYPE TEXT USING age::TEXT")
        } catch (_: Exception) { /* already TEXT */ }
        try {
            exec("ALTER TABLE user_profiles ALTER COLUMN poids_kg TYPE TEXT USING poids_kg::TEXT")
        } catch (_: Exception) { /* already TEXT */ }
        try {
            exec("ALTER TABLE user_profiles ALTER COLUMN taille_cm TYPE TEXT USING taille_cm::TEXT")
        } catch (_: Exception) { /* already TEXT */ }
    }
    log.info("Schema PostgreSQL verifie — tables creees/mises a jour si necessaire")
}
