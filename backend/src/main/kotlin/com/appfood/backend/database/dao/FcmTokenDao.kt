package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.FcmTokensTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

data class FcmTokenRow(
    val id: String,
    val userId: String,
    val token: String,
    val platform: String,
    val createdAt: kotlinx.datetime.Instant,
    val updatedAt: kotlinx.datetime.Instant,
)

class FcmTokenDao {
    suspend fun findByUserId(userId: String): List<FcmTokenRow> =
        dbQuery {
            FcmTokensTable.selectAll()
                .where { FcmTokensTable.userId eq userId }
                .map { it.toRow() }
        }

    suspend fun upsert(
        id: String,
        userId: String,
        token: String,
        platform: String,
    ) = dbQuery {
        val now = Clock.System.now()
        FcmTokensTable.upsert(FcmTokensTable.userId, FcmTokensTable.token) {
            it[FcmTokensTable.id] = id
            it[FcmTokensTable.userId] = userId
            it[FcmTokensTable.token] = token
            it[FcmTokensTable.platform] = platform
            it[FcmTokensTable.createdAt] = now
            it[FcmTokensTable.updatedAt] = now
        }
    }

    suspend fun deleteByToken(
        userId: String,
        token: String,
    ): Boolean =
        dbQuery {
            FcmTokensTable.deleteWhere {
                (FcmTokensTable.userId eq userId) and (FcmTokensTable.token eq token)
            } > 0
        }

    suspend fun deleteByUserId(userId: String): Int =
        dbQuery {
            FcmTokensTable.deleteWhere { FcmTokensTable.userId eq userId }
        }

    private fun ResultRow.toRow() =
        FcmTokenRow(
            id = this[FcmTokensTable.id],
            userId = this[FcmTokensTable.userId],
            token = this[FcmTokensTable.token],
            platform = this[FcmTokensTable.platform],
            createdAt = this[FcmTokensTable.createdAt],
            updatedAt = this[FcmTokensTable.updatedAt],
        )
}
