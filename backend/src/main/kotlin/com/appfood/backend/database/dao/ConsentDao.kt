package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.ConsentType
import com.appfood.backend.database.tables.ConsentsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

data class ConsentRow(
    val id: String,
    val userId: String,
    val type: ConsentType,
    val accepte: Boolean,
    val dateConsentement: kotlinx.datetime.Instant,
    val versionPolitique: String,
)

class ConsentDao {
    suspend fun findByUserId(userId: String): List<ConsentRow> =
        dbQuery {
            ConsentsTable.selectAll()
                .where { ConsentsTable.userId eq userId }
                .map { it.toRow() }
        }

    suspend fun findByUserAndType(
        userId: String,
        type: ConsentType,
    ): ConsentRow? =
        dbQuery {
            ConsentsTable.selectAll()
                .where { (ConsentsTable.userId eq userId) and (ConsentsTable.type eq type) }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun upsert(row: ConsentRow) =
        dbQuery {
            ConsentsTable.upsert(ConsentsTable.userId, ConsentsTable.type) {
                it[id] = row.id
                it[userId] = row.userId
                it[type] = row.type
                it[accepte] = row.accepte
                it[dateConsentement] = row.dateConsentement
                it[versionPolitique] = row.versionPolitique
            }
        }

    suspend fun deleteByUserId(userId: String): Int =
        dbQuery {
            ConsentsTable.deleteWhere { ConsentsTable.userId eq userId }
        }

    private fun ResultRow.toRow() =
        ConsentRow(
            id = this[ConsentsTable.id],
            userId = this[ConsentsTable.userId],
            type = this[ConsentsTable.type],
            accepte = this[ConsentsTable.accepte],
            dateConsentement = this[ConsentsTable.dateConsentement],
            versionPolitique = this[ConsentsTable.versionPolitique],
        )
}
