package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.Role
import com.appfood.backend.database.tables.UsersTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class UserRow(
    val id: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val role: Role,
    val createdAt: kotlinx.datetime.Instant,
    val updatedAt: kotlinx.datetime.Instant,
)

class UserDao {
    suspend fun findById(id: String): UserRow? =
        dbQuery {
            UsersTable.selectAll()
                .where { UsersTable.id eq id }
                .map { it.toUserRow() }
                .singleOrNull()
        }

    suspend fun findByEmail(email: String): UserRow? =
        dbQuery {
            UsersTable.selectAll()
                .where { UsersTable.email eq email }
                .map { it.toUserRow() }
                .singleOrNull()
        }

    suspend fun insert(
        id: String,
        email: String,
        nom: String? = null,
        prenom: String? = null,
        role: Role = Role.USER,
    ): UserRow =
        dbQuery {
            val now = Clock.System.now()
            UsersTable.insert {
                it[UsersTable.id] = id
                it[UsersTable.email] = email
                it[UsersTable.nom] = nom
                it[UsersTable.prenom] = prenom
                it[UsersTable.role] = role
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
            UserRow(id, email, nom, prenom, role, now, now)
        }

    suspend fun update(
        id: String,
        nom: String?,
        prenom: String?,
    ): Boolean =
        dbQuery {
            UsersTable.update({ UsersTable.id eq id }) {
                it[UsersTable.nom] = nom
                it[UsersTable.prenom] = prenom
                it[UsersTable.updatedAt] = Clock.System.now()
            } > 0
        }

    suspend fun delete(id: String): Boolean =
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id eq id } > 0
        }

    private fun ResultRow.toUserRow() =
        UserRow(
            id = this[UsersTable.id],
            email = this[UsersTable.email],
            nom = this[UsersTable.nom],
            prenom = this[UsersTable.prenom],
            role = this[UsersTable.role],
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt],
        )
}
