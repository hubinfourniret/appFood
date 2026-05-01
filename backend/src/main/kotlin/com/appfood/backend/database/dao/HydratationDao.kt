package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.HydratationEntriesTable
import com.appfood.backend.database.tables.HydratationTable
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class HydratationRow(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val quantiteMl: Int,
    val objectifMl: Int,
    val estObjectifPersonnalise: Boolean,
    val updatedAt: kotlinx.datetime.Instant,
)

data class HydratationEntryRow(
    val id: String,
    val hydratationId: String,
    val heure: kotlinx.datetime.Instant,
    val quantiteMl: Int,
)

class HydratationDao {
    suspend fun findByUserAndDate(
        userId: String,
        date: LocalDate,
    ): HydratationRow? =
        dbQuery {
            HydratationTable.selectAll()
                .where { (HydratationTable.userId eq userId) and (HydratationTable.date eq date) }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findByUserAndDateRange(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<HydratationRow> =
        dbQuery {
            HydratationTable.selectAll()
                .where {
                    (HydratationTable.userId eq userId) and
                        (HydratationTable.date greaterEq from) and
                        (HydratationTable.date lessEq to)
                }
                .map { it.toRow() }
        }

    suspend fun insert(row: HydratationRow): HydratationRow =
        dbQuery {
            HydratationTable.insert {
                it[id] = row.id
                it[userId] = row.userId
                it[date] = row.date
                it[quantiteMl] = row.quantiteMl
                it[objectifMl] = row.objectifMl
                it[estObjectifPersonnalise] = row.estObjectifPersonnalise
                it[updatedAt] = row.updatedAt
            }
            row
        }

    suspend fun updateQuantite(
        id: String,
        userId: String,
        quantiteMl: Int,
    ): Boolean =
        dbQuery {
            HydratationTable.update({
                (HydratationTable.id eq id) and (HydratationTable.userId eq userId)
            }) {
                it[HydratationTable.quantiteMl] = quantiteMl
                it[HydratationTable.updatedAt] = Clock.System.now()
            } > 0
        }

    suspend fun updateObjectif(
        id: String,
        userId: String,
        objectifMl: Int,
        estObjectifPersonnalise: Boolean,
    ): Boolean =
        dbQuery {
            HydratationTable.update({
                (HydratationTable.id eq id) and (HydratationTable.userId eq userId)
            }) {
                it[HydratationTable.objectifMl] = objectifMl
                it[HydratationTable.estObjectifPersonnalise] = estObjectifPersonnalise
                it[HydratationTable.updatedAt] = Clock.System.now()
            } > 0
        }

    // --- Entries ---

    suspend fun findEntriesByHydratationId(hydratationId: String): List<HydratationEntryRow> =
        dbQuery {
            HydratationEntriesTable.selectAll()
                .where { HydratationEntriesTable.hydratationId eq hydratationId }
                .map { it.toEntryRow() }
        }

    suspend fun insertEntry(row: HydratationEntryRow): HydratationEntryRow =
        dbQuery {
            HydratationEntriesTable.insert {
                it[id] = row.id
                it[hydratationId] = row.hydratationId
                it[heure] = row.heure
                it[quantiteMl] = row.quantiteMl
            }
            row
        }

    suspend fun deleteEntry(id: String): Boolean =
        dbQuery {
            HydratationEntriesTable.deleteWhere { HydratationEntriesTable.id eq id } > 0
        }

    suspend fun findEntryById(id: String): HydratationEntryRow? =
        dbQuery {
            HydratationEntriesTable.selectAll()
                .where { HydratationEntriesTable.id eq id }
                .map { it.toEntryRow() }
                .singleOrNull()
        }

    suspend fun findById(id: String): HydratationRow? =
        dbQuery {
            HydratationTable.selectAll()
                .where { HydratationTable.id eq id }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findByUserId(userId: String): List<HydratationRow> =
        dbQuery {
            HydratationTable.selectAll()
                .where { HydratationTable.userId eq userId }
                .map { it.toRow() }
        }

    suspend fun deleteByUserId(userId: String): Int =
        dbQuery {
            // Delete entries first (they reference hydratation via hydratationId)
            val hydratationIds =
                HydratationTable.selectAll()
                    .where { HydratationTable.userId eq userId }
                    .map { it[HydratationTable.id] }
            hydratationIds.forEach { hId ->
                HydratationEntriesTable.deleteWhere { HydratationEntriesTable.hydratationId eq hId }
            }
            HydratationTable.deleteWhere { HydratationTable.userId eq userId }
        }

    private fun ResultRow.toRow() =
        HydratationRow(
            id = this[HydratationTable.id],
            userId = this[HydratationTable.userId],
            date = this[HydratationTable.date],
            quantiteMl = this[HydratationTable.quantiteMl],
            objectifMl = this[HydratationTable.objectifMl],
            estObjectifPersonnalise = this[HydratationTable.estObjectifPersonnalise],
            updatedAt = this[HydratationTable.updatedAt],
        )

    private fun ResultRow.toEntryRow() =
        HydratationEntryRow(
            id = this[HydratationEntriesTable.id],
            hydratationId = this[HydratationEntriesTable.hydratationId],
            heure = this[HydratationEntriesTable.heure],
            quantiteMl = this[HydratationEntriesTable.quantiteMl],
        )
}
