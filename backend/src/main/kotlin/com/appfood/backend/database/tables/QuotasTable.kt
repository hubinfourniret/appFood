package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object QuotasTable : Table("quotas") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val nutriment = enumerationByName<NutrimentType>("nutriment", 20)
    val valeurCible = double("valeur_cible")
    val estPersonnalise = bool("est_personnalise").default(false)
    val valeurCalculee = double("valeur_calculee")
    val unite = varchar("unite", 10)
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(userId, nutriment)
}
