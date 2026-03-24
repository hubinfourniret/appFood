package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object NotificationsTable : Table("notifications") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val type = enumerationByName<NotificationType>("type", 20)
    val titre = varchar("titre", 255)
    val contenu = text("contenu")
    val dateEnvoi = timestamp("date_envoi")
    val lue = bool("lue").default(false)
    override val primaryKey = PrimaryKey(id)
}
