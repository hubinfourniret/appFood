package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table

object AlimentsTable : Table("aliments") {
    val id = varchar("id", 36)
    val nom = varchar("nom", 255)
    val marque = varchar("marque", 255).nullable()
    val sourceAliment = enumerationByName<SourceAliment>("source", 20)
    val sourceId = varchar("source_id", 100).nullable()
    val codeBarres = varchar("code_barres", 50).nullable().index()
    val categorie = varchar("categorie", 100)
    val regimesCompatibles = text("regimes_compatibles")
    // Nutriments pour 100g
    val calories = double("calories")
    val proteines = double("proteines")
    val glucides = double("glucides")
    val lipides = double("lipides")
    val fibres = double("fibres")
    val sel = double("sel")
    val sucres = double("sucres")
    val fer = double("fer")
    val calcium = double("calcium")
    val zinc = double("zinc")
    val magnesium = double("magnesium")
    val vitamineB12 = double("vitamine_b12")
    val vitamineD = double("vitamine_d")
    val vitamineC = double("vitamine_c")
    val omega3 = double("omega_3")
    val omega6 = double("omega_6")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_aliments_source_source_id", sourceAliment, sourceId)
    }
}
