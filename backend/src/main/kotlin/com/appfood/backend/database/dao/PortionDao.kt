package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.PortionsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

data class PortionRow(
    val id: String,
    val alimentId: String?,
    val nom: String,
    val quantiteGrammes: Double,
    val estGenerique: Boolean,
    val estPersonnalise: Boolean,
    val userId: String?,
)

class PortionDao {

    suspend fun findById(id: String): PortionRow? = dbQuery {
        PortionsTable.selectAll()
            .where { PortionsTable.id eq id }
            .map { it.toRow() }
            .singleOrNull()
    }

    suspend fun findByAlimentId(alimentId: String, userId: String?): List<PortionRow> = dbQuery {
        PortionsTable.selectAll()
            .where {
                // Portions specifiques a l'aliment OU generiques
                val base = (PortionsTable.alimentId eq alimentId) or
                    (PortionsTable.estGenerique eq true)
                // + portions personnalisees de l'utilisateur pour cet aliment
                if (userId != null) {
                    base or ((PortionsTable.userId eq userId) and (PortionsTable.alimentId eq alimentId))
                } else {
                    base
                }
            }
            .map { it.toRow() }
    }

    suspend fun findGeneriques(): List<PortionRow> = dbQuery {
        PortionsTable.selectAll()
            .where { PortionsTable.estGenerique eq true }
            .map { it.toRow() }
    }

    suspend fun insert(row: PortionRow): PortionRow = dbQuery {
        PortionsTable.insert {
            it[id] = row.id
            it[alimentId] = row.alimentId
            it[nom] = row.nom
            it[quantiteGrammes] = row.quantiteGrammes
            it[estGenerique] = row.estGenerique
            it[estPersonnalise] = row.estPersonnalise
            it[userId] = row.userId
        }
        row
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        PortionsTable.deleteWhere { PortionsTable.id eq id } > 0
    }

    suspend fun deleteByUserId(userId: String): Int = dbQuery {
        PortionsTable.deleteWhere { PortionsTable.userId eq userId }
    }

    private fun ResultRow.toRow() = PortionRow(
        id = this[PortionsTable.id],
        alimentId = this[PortionsTable.alimentId],
        nom = this[PortionsTable.nom],
        quantiteGrammes = this[PortionsTable.quantiteGrammes],
        estGenerique = this[PortionsTable.estGenerique],
        estPersonnalise = this[PortionsTable.estPersonnalise],
        userId = this[PortionsTable.userId],
    )
}
