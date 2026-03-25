package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.UserPreferencesTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class UserPreferencesRow(
    val userId: String,
    val alimentsExclus: String,
    val allergies: String,
    val alimentsFavoris: String,
    val updatedAt: kotlinx.datetime.Instant,
)

class UserPreferencesDao {

    suspend fun findByUserId(userId: String): UserPreferencesRow? = dbQuery {
        UserPreferencesTable.selectAll()
            .where { UserPreferencesTable.userId eq userId }
            .map { it.toRow() }
            .singleOrNull()
    }

    suspend fun insert(
        userId: String,
        alimentsExclus: String = "[]",
        allergies: String = "[]",
        alimentsFavoris: String = "[]",
    ): UserPreferencesRow = dbQuery {
        val now = Clock.System.now()
        UserPreferencesTable.insert {
            it[UserPreferencesTable.userId] = userId
            it[UserPreferencesTable.alimentsExclus] = alimentsExclus
            it[UserPreferencesTable.allergies] = allergies
            it[UserPreferencesTable.alimentsFavoris] = alimentsFavoris
            it[UserPreferencesTable.updatedAt] = now
        }
        UserPreferencesRow(userId, alimentsExclus, allergies, alimentsFavoris, now)
    }

    suspend fun update(
        userId: String,
        alimentsExclus: String,
        allergies: String,
        alimentsFavoris: String,
    ): Boolean = dbQuery {
        UserPreferencesTable.update({ UserPreferencesTable.userId eq userId }) {
            it[UserPreferencesTable.alimentsExclus] = alimentsExclus
            it[UserPreferencesTable.allergies] = allergies
            it[UserPreferencesTable.alimentsFavoris] = alimentsFavoris
            it[UserPreferencesTable.updatedAt] = Clock.System.now()
        } > 0
    }

    suspend fun delete(userId: String): Boolean = dbQuery {
        UserPreferencesTable.deleteWhere { UserPreferencesTable.userId eq userId } > 0
    }

    private fun ResultRow.toRow() = UserPreferencesRow(
        userId = this[UserPreferencesTable.userId],
        alimentsExclus = this[UserPreferencesTable.alimentsExclus],
        allergies = this[UserPreferencesTable.allergies],
        alimentsFavoris = this[UserPreferencesTable.alimentsFavoris],
        updatedAt = this[UserPreferencesTable.updatedAt],
    )
}
