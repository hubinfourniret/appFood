package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.NotificationType
import com.appfood.backend.database.tables.NotificationsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class NotificationRow(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val titre: String,
    val contenu: String,
    val dateEnvoi: kotlinx.datetime.Instant,
    val lue: Boolean,
)

class NotificationDao {

    suspend fun findById(id: String, userId: String): NotificationRow? = dbQuery {
        NotificationsTable.selectAll()
            .where { (NotificationsTable.id eq id) and (NotificationsTable.userId eq userId) }
            .map { it.toRow() }
            .singleOrNull()
    }

    suspend fun findByUserId(userId: String, limit: Int = 50, offset: Long = 0): List<NotificationRow> = dbQuery {
        NotificationsTable.selectAll()
            .where { NotificationsTable.userId eq userId }
            .orderBy(NotificationsTable.dateEnvoi, SortOrder.DESC)
            .limit(limit).offset(offset)
            .map { it.toRow() }
    }

    suspend fun findByUserIdUnreadOnly(userId: String, limit: Int = 50, offset: Long = 0): List<NotificationRow> = dbQuery {
        NotificationsTable.selectAll()
            .where { (NotificationsTable.userId eq userId) and (NotificationsTable.lue eq false) }
            .orderBy(NotificationsTable.dateEnvoi, SortOrder.DESC)
            .limit(limit).offset(offset)
            .map { it.toRow() }
    }

    suspend fun countByUserId(userId: String): Long = dbQuery {
        NotificationsTable.selectAll()
            .where { NotificationsTable.userId eq userId }
            .count()
    }

    suspend fun countUnread(userId: String): Long = dbQuery {
        NotificationsTable.selectAll()
            .where { (NotificationsTable.userId eq userId) and (NotificationsTable.lue eq false) }
            .count()
    }

    suspend fun insert(row: NotificationRow): NotificationRow = dbQuery {
        NotificationsTable.insert {
            it[id] = row.id
            it[userId] = row.userId
            it[type] = row.type
            it[titre] = row.titre
            it[contenu] = row.contenu
            it[dateEnvoi] = row.dateEnvoi
            it[lue] = row.lue
        }
        row
    }

    suspend fun markAsRead(id: String, userId: String): Boolean = dbQuery {
        NotificationsTable.update({
            (NotificationsTable.id eq id) and (NotificationsTable.userId eq userId)
        }) {
            it[lue] = true
        } > 0
    }

    suspend fun markAllAsRead(userId: String): Int = dbQuery {
        NotificationsTable.update({
            (NotificationsTable.userId eq userId) and (NotificationsTable.lue eq false)
        }) {
            it[lue] = true
        }
    }

    suspend fun delete(id: String, userId: String): Boolean = dbQuery {
        NotificationsTable.deleteWhere {
            (NotificationsTable.id eq id) and (NotificationsTable.userId eq userId)
        } > 0
    }

    suspend fun deleteByUserId(userId: String): Int = dbQuery {
        NotificationsTable.deleteWhere { NotificationsTable.userId eq userId }
    }

    private fun ResultRow.toRow() = NotificationRow(
        id = this[NotificationsTable.id],
        userId = this[NotificationsTable.userId],
        type = this[NotificationsTable.type],
        titre = this[NotificationsTable.titre],
        contenu = this[NotificationsTable.contenu],
        dateEnvoi = this[NotificationsTable.dateEnvoi],
        lue = this[NotificationsTable.lue],
    )
}
