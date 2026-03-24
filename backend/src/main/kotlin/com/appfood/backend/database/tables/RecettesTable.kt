package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RecettesTable : Table("recettes") {
    val id = varchar("id", 36)
    val nom = varchar("nom", 255)
    val description = text("description")
    val tempsPreparationMin = integer("temps_preparation_min")
    val tempsCuissonMin = integer("temps_cuisson_min")
    val nbPortions = integer("nb_portions")
    val regimesCompatibles = text("regimes_compatibles")
    val source = enumerationByName<SourceRecette>("source", 20)
    val typeRepas = text("type_repas")
    val etapes = text("etapes")
    // Nutriments totaux (calcules)
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
    val imageUrl = varchar("image_url", 500).nullable()
    val publie = bool("publie").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object IngredientsTable : Table("ingredients") {
    val id = varchar("id", 36)
    val recetteId = varchar("recette_id", 36).references(RecettesTable.id, onDelete = ReferenceOption.CASCADE)
    val alimentId = varchar("aliment_id", 36).references(AlimentsTable.id)
    val alimentNom = varchar("aliment_nom", 255)
    val quantiteGrammes = double("quantite_grammes")
    override val primaryKey = PrimaryKey(id)
}
