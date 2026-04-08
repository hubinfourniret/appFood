package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.PoidsHistoryTable
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

data class PoidsHistoryRow(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val poidsKg: Double,
    val estReference: Boolean,
    val createdAt: kotlinx.datetime.Instant,
)

class PoidsHistoryDao {
    suspend fun findByUserId(
        userId: String,
        limit: Int = 100,
    ): List<PoidsHistoryRow> =
        dbQuery {
            PoidsHistoryTable.selectAll()
                .where { PoidsHistoryTable.userId eq userId }
                .orderBy(PoidsHistoryTable.date, SortOrder.DESC)
                .limit(limit)
                .map { it.toRow() }
        }

    suspend fun findLatest(userId: String): PoidsHistoryRow? =
        dbQuery {
            PoidsHistoryTable.selectAll()
                .where { PoidsHistoryTable.userId eq userId }
                .orderBy(PoidsHistoryTable.date, SortOrder.DESC)
                .limit(1)
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun insert(
        id: String,
        userId: String,
        date: LocalDate,
        poidsKg: Double,
        estReference: Boolean = false,
    ): PoidsHistoryRow =
        dbQuery {
            val now = Clock.System.now()
            PoidsHistoryTable.insert {
                it[PoidsHistoryTable.id] = id
                it[PoidsHistoryTable.userId] = userId
                it[PoidsHistoryTable.date] = date
                it[PoidsHistoryTable.poidsKg] = poidsKg
                it[PoidsHistoryTable.estReference] = estReference
                it[PoidsHistoryTable.createdAt] = now
            }
            PoidsHistoryRow(id, userId, date, poidsKg, estReference, now)
        }

    suspend fun findReference(userId: String): PoidsHistoryRow? =
        dbQuery {
            PoidsHistoryTable.selectAll()
                .where { (PoidsHistoryTable.userId eq userId) and (PoidsHistoryTable.estReference eq true) }
                .orderBy(PoidsHistoryTable.date, SortOrder.DESC)
                .limit(1)
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findByUserAndDateRange(
        userId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<PoidsHistoryRow> =
        dbQuery {
            PoidsHistoryTable.selectAll()
                .where {
                    (PoidsHistoryTable.userId eq userId) and
                        (PoidsHistoryTable.date greaterEq from) and
                        (PoidsHistoryTable.date lessEq to)
                }
                .orderBy(PoidsHistoryTable.date, SortOrder.DESC)
                .map { it.toRow() }
        }

    suspend fun delete(
        id: String,
        userId: String,
    ): Boolean =
        dbQuery {
            PoidsHistoryTable.deleteWhere {
                (PoidsHistoryTable.id eq id) and (PoidsHistoryTable.userId eq userId)
            } > 0
        }

    suspend fun deleteByUserId(userId: String): Int =
        dbQuery {
            PoidsHistoryTable.deleteWhere { PoidsHistoryTable.userId eq userId }
        }

    private fun ResultRow.toRow() =
        PoidsHistoryRow(
            id = this[PoidsHistoryTable.id],
            userId = this[PoidsHistoryTable.userId],
            date = this[PoidsHistoryTable.date],
            poidsKg = this[PoidsHistoryTable.poidsKg],
            estReference = this[PoidsHistoryTable.estReference],
            createdAt = this[PoidsHistoryTable.createdAt],
        )
}
