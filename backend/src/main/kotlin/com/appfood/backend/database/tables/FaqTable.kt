package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table

object FaqTable : Table("faq") {
    val id = varchar("id", 36)
    val theme = varchar("theme", 100)
    val question = text("question")
    val reponse = text("reponse")
    val ordre = integer("ordre")
    val actif = bool("actif").default(true)
    override val primaryKey = PrimaryKey(id)
}
