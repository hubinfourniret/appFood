package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.IngredientsTable
import com.appfood.backend.database.tables.RecettesTable
import com.appfood.backend.database.tables.SourceRecette
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class RecetteRow(
    val id: String,
    val nom: String,
    val description: String,
    val tempsPreparationMin: Int,
    val tempsCuissonMin: Int,
    val nbPortions: Int,
    val regimesCompatibles: String,
    val source: SourceRecette,
    val typeRepas: String,
    val etapes: String,
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val fibres: Double,
    val sel: Double,
    val sucres: Double,
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double,
    val imageUrl: String?,
    val publie: Boolean,
    val createdAt: kotlinx.datetime.Instant,
    val updatedAt: kotlinx.datetime.Instant,
)

data class IngredientRow(
    val id: String,
    val recetteId: String,
    val alimentId: String,
    val alimentNom: String,
    val quantiteGrammes: Double,
)

class RecetteDao {
    suspend fun findById(id: String): RecetteRow? =
        dbQuery {
            RecettesTable.selectAll()
                .where { RecettesTable.id eq id }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findAll(
        limit: Int = 100,
        offset: Long = 0,
    ): List<RecetteRow> =
        dbQuery {
            RecettesTable.selectAll()
                .where { RecettesTable.publie eq true }
                .limit(limit).offset(offset)
                .map { it.toRow() }
        }

    suspend fun count(): Long =
        dbQuery {
            RecettesTable.selectAll()
                .where { RecettesTable.publie eq true }
                .count()
        }

    suspend fun findAllPublished(): List<RecetteRow> =
        dbQuery {
            RecettesTable.selectAll()
                .where { RecettesTable.publie eq true }
                .map { it.toRow() }
        }

    suspend fun insert(row: RecetteRow): RecetteRow =
        dbQuery {
            RecettesTable.insert {
                it[id] = row.id
                it[nom] = row.nom
                it[description] = row.description
                it[tempsPreparationMin] = row.tempsPreparationMin
                it[tempsCuissonMin] = row.tempsCuissonMin
                it[nbPortions] = row.nbPortions
                it[regimesCompatibles] = row.regimesCompatibles
                it[sourceRecette] = row.source
                it[typeRepas] = row.typeRepas
                it[etapes] = row.etapes
                it[calories] = row.calories
                it[proteines] = row.proteines
                it[glucides] = row.glucides
                it[lipides] = row.lipides
                it[fibres] = row.fibres
                it[sel] = row.sel
                it[sucres] = row.sucres
                it[fer] = row.fer
                it[calcium] = row.calcium
                it[zinc] = row.zinc
                it[magnesium] = row.magnesium
                it[vitamineB12] = row.vitamineB12
                it[vitamineD] = row.vitamineD
                it[vitamineC] = row.vitamineC
                it[omega3] = row.omega3
                it[omega6] = row.omega6
                it[imageUrl] = row.imageUrl
                it[publie] = row.publie
                it[createdAt] = row.createdAt
                it[updatedAt] = row.updatedAt
            }
            row
        }

    suspend fun update(row: RecetteRow): Boolean =
        dbQuery {
            RecettesTable.update({ RecettesTable.id eq row.id }) {
                it[nom] = row.nom
                it[description] = row.description
                it[tempsPreparationMin] = row.tempsPreparationMin
                it[tempsCuissonMin] = row.tempsCuissonMin
                it[nbPortions] = row.nbPortions
                it[regimesCompatibles] = row.regimesCompatibles
                it[typeRepas] = row.typeRepas
                it[etapes] = row.etapes
                it[calories] = row.calories
                it[proteines] = row.proteines
                it[glucides] = row.glucides
                it[lipides] = row.lipides
                it[fibres] = row.fibres
                it[sel] = row.sel
                it[sucres] = row.sucres
                it[fer] = row.fer
                it[calcium] = row.calcium
                it[zinc] = row.zinc
                it[magnesium] = row.magnesium
                it[vitamineB12] = row.vitamineB12
                it[vitamineD] = row.vitamineD
                it[vitamineC] = row.vitamineC
                it[omega3] = row.omega3
                it[omega6] = row.omega6
                it[imageUrl] = row.imageUrl
                it[publie] = row.publie
                it[updatedAt] = Clock.System.now()
            } > 0
        }

    suspend fun delete(id: String): Boolean =
        dbQuery {
            RecettesTable.deleteWhere { RecettesTable.id eq id } > 0
        }

    // --- Ingredients ---

    suspend fun findIngredientsByRecetteId(recetteId: String): List<IngredientRow> =
        dbQuery {
            IngredientsTable.selectAll()
                .where { IngredientsTable.recetteId eq recetteId }
                .map { it.toIngredientRow() }
        }

    suspend fun insertIngredient(row: IngredientRow): IngredientRow =
        dbQuery {
            IngredientsTable.insert {
                it[id] = row.id
                it[recetteId] = row.recetteId
                it[alimentId] = row.alimentId
                it[alimentNom] = row.alimentNom
                it[quantiteGrammes] = row.quantiteGrammes
            }
            row
        }

    suspend fun deleteIngredientsByRecetteId(recetteId: String): Int =
        dbQuery {
            IngredientsTable.deleteWhere { IngredientsTable.recetteId eq recetteId }
        }

    private fun ResultRow.toRow() =
        RecetteRow(
            id = this[RecettesTable.id],
            nom = this[RecettesTable.nom],
            description = this[RecettesTable.description],
            tempsPreparationMin = this[RecettesTable.tempsPreparationMin],
            tempsCuissonMin = this[RecettesTable.tempsCuissonMin],
            nbPortions = this[RecettesTable.nbPortions],
            regimesCompatibles = this[RecettesTable.regimesCompatibles],
            source = this[RecettesTable.sourceRecette],
            typeRepas = this[RecettesTable.typeRepas],
            etapes = this[RecettesTable.etapes],
            calories = this[RecettesTable.calories],
            proteines = this[RecettesTable.proteines],
            glucides = this[RecettesTable.glucides],
            lipides = this[RecettesTable.lipides],
            fibres = this[RecettesTable.fibres],
            sel = this[RecettesTable.sel],
            sucres = this[RecettesTable.sucres],
            fer = this[RecettesTable.fer],
            calcium = this[RecettesTable.calcium],
            zinc = this[RecettesTable.zinc],
            magnesium = this[RecettesTable.magnesium],
            vitamineB12 = this[RecettesTable.vitamineB12],
            vitamineD = this[RecettesTable.vitamineD],
            vitamineC = this[RecettesTable.vitamineC],
            omega3 = this[RecettesTable.omega3],
            omega6 = this[RecettesTable.omega6],
            imageUrl = this[RecettesTable.imageUrl],
            publie = this[RecettesTable.publie],
            createdAt = this[RecettesTable.createdAt],
            updatedAt = this[RecettesTable.updatedAt],
        )

    private fun ResultRow.toIngredientRow() =
        IngredientRow(
            id = this[IngredientsTable.id],
            recetteId = this[IngredientsTable.recetteId],
            alimentId = this[IngredientsTable.alimentId],
            alimentNom = this[IngredientsTable.alimentNom],
            quantiteGrammes = this[IngredientsTable.quantiteGrammes],
        )
}
