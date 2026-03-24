package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object HydratationTable : Table("hydratation") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val date = date("date")
    val quantiteMl = integer("quantite_ml")
    val objectifMl = integer("objectif_ml")
    val estObjectifPersonnalise = bool("est_objectif_personnalise").default(false)
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_user_date_hydra", userId, date)
    }
}

object HydratationEntriesTable : Table("hydratation_entries") {
    val id = varchar("id", 36)
    val hydratationId = varchar("hydratation_id", 36).references(HydratationTable.id, onDelete = ReferenceOption.CASCADE)
    val heure = timestamp("heure")
    val quantiteMl = integer("quantite_ml")
    override val primaryKey = PrimaryKey(id)
}
