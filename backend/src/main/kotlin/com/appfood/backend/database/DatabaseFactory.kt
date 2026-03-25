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
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Configure la connexion a la base de donnees PostgreSQL.
 * Lance les migrations Flyway puis verifie le schema Exposed.
 */
fun Application.configureDatabase() {
    val config = environment.config
    val dbUrl = config.property("appfood.database.url").getString()
    val dbUser = config.property("appfood.database.user").getString()
    val dbPassword = config.property("appfood.database.password").getString()

    // Migrations Flyway
    val flyway = Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .locations("classpath:db/migration")
        .validateMigrationNaming(true)
        .load()
    flyway.migrate()

    // Connexion Exposed
    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword,
    )

    // Verification du schema (ne cree pas les tables — Flyway s'en charge)
    transaction {
        SchemaUtils.statementsRequiredToActualizeScheme(
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
        ).forEach { statement ->
            this@configureDatabase.environment.log.warn("Schema drift detecte : $statement")
        }
    }
}
