package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table

object PortionsTable : Table("portions") {
    val id = varchar("id", 36)
    val alimentId = varchar("aliment_id", 36).references(AlimentsTable.id).nullable()
    val nom = varchar("nom", 100)
    val quantiteGrammes = double("quantite_grammes")
    val estGenerique = bool("est_generique").default(false)
    val estPersonnalise = bool("est_personnalise").default(false)
    val userId = varchar("user_id", 36).references(UsersTable.id).nullable()
    override val primaryKey = PrimaryKey(id)
}
