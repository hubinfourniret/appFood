package com.appfood.backend.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object JournalEntriesTable : Table("journal_entries") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).index()
    val date = date("date").index()
    val mealType = enumerationByName<MealType>("meal_type", 20)
    val alimentId = varchar("aliment_id", 36).references(AlimentsTable.id).nullable()
    val recetteId = varchar("recette_id", 36).references(RecettesTable.id).nullable()
    val nom = varchar("nom", 255)
    val quantiteGrammes = double("quantite_grammes")
    val nbPortions = double("nb_portions").nullable()

    // Nutriments calcules (snapshot au moment de la saisie)
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
    /**
     * JSON Map<ingredientId, grammes> — null si recette ajoutee sans ajustements
     * ou si entree de type aliment. Permet la restauration des ajustements en edit.
     */
    val ingredientOverridesJson = text("ingredient_overrides_json").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}
