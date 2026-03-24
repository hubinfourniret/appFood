package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ConsentsTable : Table("consents") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val type = enumerationByName<ConsentType>("type", 30)
    val accepte = bool("accepte")
    val dateConsentement = timestamp("date_consentement")
    val versionPolitique = varchar("version_politique", 10)
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_user_consent_type", userId, type)
    }
}
