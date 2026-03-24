package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PoidsHistoryTable : Table("poids_history") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val date = date("date")
    val poidsKg = double("poids_kg")
    val estReference = bool("est_reference").default(false)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}
