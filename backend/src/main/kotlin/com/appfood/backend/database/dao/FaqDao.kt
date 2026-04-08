package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.FaqTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

data class FaqRow(
    val id: String,
    val theme: String,
    val question: String,
    val reponse: String,
    val ordre: Int,
    val actif: Boolean,
)

class FaqDao {
    suspend fun findAllActive(): List<FaqRow> =
        dbQuery {
            FaqTable.selectAll()
                .where { FaqTable.actif eq true }
                .orderBy(FaqTable.ordre, SortOrder.ASC)
                .map { it.toRow() }
        }

    suspend fun findAll(): List<FaqRow> =
        dbQuery {
            FaqTable.selectAll()
                .orderBy(FaqTable.ordre, SortOrder.ASC)
                .map { it.toRow() }
        }

    suspend fun findById(id: String): FaqRow? =
        dbQuery {
            FaqTable.selectAll()
                .where { FaqTable.id eq id }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun insert(row: FaqRow): FaqRow =
        dbQuery {
            FaqTable.insert {
                it[id] = row.id
                it[theme] = row.theme
                it[question] = row.question
                it[reponse] = row.reponse
                it[ordre] = row.ordre
                it[actif] = row.actif
            }
            row
        }

    suspend fun update(row: FaqRow): Boolean =
        dbQuery {
            FaqTable.update({ FaqTable.id eq row.id }) {
                it[theme] = row.theme
                it[question] = row.question
                it[reponse] = row.reponse
                it[ordre] = row.ordre
                it[actif] = row.actif
            } > 0
        }

    suspend fun delete(id: String): Boolean =
        dbQuery {
            FaqTable.deleteWhere { FaqTable.id eq id } > 0
        }

    private fun ResultRow.toRow() =
        FaqRow(
            id = this[FaqTable.id],
            theme = this[FaqTable.theme],
            question = this[FaqTable.question],
            reponse = this[FaqTable.reponse],
            ordre = this[FaqTable.ordre],
            actif = this[FaqTable.actif],
        )
}
