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

    // Connexion Exposed
    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword,
    )

    // Creation des tables manquantes
    val log = environment.log
    transaction {
        SchemaUtils.createMissingTablesAndColumns(*ALL_TABLES)
    }
    log.info("Schema PostgreSQL verifie — tables creees/mises a jour si necessaire")
}
