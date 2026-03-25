package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.JournalEntriesTable
import com.appfood.backend.database.tables.MealType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class JournalEntryRow(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val mealType: MealType,
    val alimentId: String?,
    val recetteId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val nbPortions: Double?,
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
    val createdAt: kotlinx.datetime.Instant,
    val updatedAt: kotlinx.datetime.Instant,
)

class JournalEntryDao {

    suspend fun findById(id: String, userId: String): JournalEntryRow? = dbQuery {
        JournalEntriesTable.selectAll()
            .where { (JournalEntriesTable.id eq id) and (JournalEntriesTable.userId eq userId) }
            .map { it.toRow() }
            .singleOrNull()
    }

    suspend fun findByUserAndDate(userId: String, date: LocalDate): List<JournalEntryRow> = dbQuery {
        JournalEntriesTable.selectAll()
            .where { (JournalEntriesTable.userId eq userId) and (JournalEntriesTable.date eq date) }
            .map { it.toRow() }
    }

    suspend fun findByUserAndDateRange(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<JournalEntryRow> = dbQuery {
        JournalEntriesTable.selectAll()
            .where {
                (JournalEntriesTable.userId eq userId) and
                    (JournalEntriesTable.date greaterEq from) and
                    (JournalEntriesTable.date lessEq to)
            }
            .map { it.toRow() }
    }

    suspend fun insert(row: JournalEntryRow): JournalEntryRow = dbQuery {
        JournalEntriesTable.insert {
            it[id] = row.id
            it[userId] = row.userId
            it[date] = row.date
            it[mealType] = row.mealType
            it[alimentId] = row.alimentId
            it[recetteId] = row.recetteId
            it[nom] = row.nom
            it[quantiteGrammes] = row.quantiteGrammes
            it[nbPortions] = row.nbPortions
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
            it[createdAt] = row.createdAt
            it[updatedAt] = row.updatedAt
        }
        row
    }

    suspend fun update(row: JournalEntryRow): Boolean = dbQuery {
        JournalEntriesTable.update({
            (JournalEntriesTable.id eq row.id) and (JournalEntriesTable.userId eq row.userId)
        }) {
            it[mealType] = row.mealType
            it[alimentId] = row.alimentId
            it[recetteId] = row.recetteId
            it[nom] = row.nom
            it[quantiteGrammes] = row.quantiteGrammes
            it[nbPortions] = row.nbPortions
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
            it[updatedAt] = Clock.System.now()
        } > 0
    }

    suspend fun findByUserAll(userId: String): List<JournalEntryRow> = dbQuery {
        JournalEntriesTable.selectAll()
            .where { JournalEntriesTable.userId eq userId }
            .map { it.toRow() }
    }

    suspend fun delete(id: String, userId: String): Boolean = dbQuery {
        JournalEntriesTable.deleteWhere {
            (JournalEntriesTable.id eq id) and (JournalEntriesTable.userId eq userId)
        } > 0
    }

    suspend fun deleteByUserId(userId: String): Int = dbQuery {
        JournalEntriesTable.deleteWhere { JournalEntriesTable.userId eq userId }
    }

    private fun ResultRow.toRow() = JournalEntryRow(
        id = this[JournalEntriesTable.id],
        userId = this[JournalEntriesTable.userId],
        date = this[JournalEntriesTable.date],
        mealType = this[JournalEntriesTable.mealType],
        alimentId = this[JournalEntriesTable.alimentId],
        recetteId = this[JournalEntriesTable.recetteId],
        nom = this[JournalEntriesTable.nom],
        quantiteGrammes = this[JournalEntriesTable.quantiteGrammes],
        nbPortions = this[JournalEntriesTable.nbPortions],
        calories = this[JournalEntriesTable.calories],
        proteines = this[JournalEntriesTable.proteines],
        glucides = this[JournalEntriesTable.glucides],
        lipides = this[JournalEntriesTable.lipides],
        fibres = this[JournalEntriesTable.fibres],
        sel = this[JournalEntriesTable.sel],
        sucres = this[JournalEntriesTable.sucres],
        fer = this[JournalEntriesTable.fer],
        calcium = this[JournalEntriesTable.calcium],
        zinc = this[JournalEntriesTable.zinc],
        magnesium = this[JournalEntriesTable.magnesium],
        vitamineB12 = this[JournalEntriesTable.vitamineB12],
        vitamineD = this[JournalEntriesTable.vitamineD],
        vitamineC = this[JournalEntriesTable.vitamineC],
        omega3 = this[JournalEntriesTable.omega3],
        omega6 = this[JournalEntriesTable.omega6],
        createdAt = this[JournalEntriesTable.createdAt],
        updatedAt = this[JournalEntriesTable.updatedAt],
    )
}
