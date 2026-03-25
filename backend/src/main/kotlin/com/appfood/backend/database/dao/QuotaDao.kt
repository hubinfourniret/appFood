package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.database.tables.QuotasTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

data class QuotaRow(
    val userId: String,
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val estPersonnalise: Boolean,
    val valeurCalculee: Double,
    val unite: String,
    val updatedAt: kotlinx.datetime.Instant,
)

class QuotaDao {

    suspend fun findByUserId(userId: String): List<QuotaRow> = dbQuery {
        QuotasTable.selectAll()
            .where { QuotasTable.userId eq userId }
            .map { it.toRow() }
    }

    suspend fun findByUserAndNutriment(userId: String, nutriment: NutrimentType): QuotaRow? = dbQuery {
        QuotasTable.selectAll()
            .where { (QuotasTable.userId eq userId) and (QuotasTable.nutriment eq nutriment) }
            .map { it.toRow() }
            .singleOrNull()
    }

    suspend fun upsert(row: QuotaRow) = dbQuery {
        QuotasTable.upsert(QuotasTable.userId, QuotasTable.nutriment) {
            it[userId] = row.userId
            it[nutriment] = row.nutriment
            it[valeurCible] = row.valeurCible
            it[estPersonnalise] = row.estPersonnalise
            it[valeurCalculee] = row.valeurCalculee
            it[unite] = row.unite
            it[updatedAt] = Clock.System.now()
        }
    }

    suspend fun upsertAll(rows: List<QuotaRow>) = dbQuery {
        rows.forEach { row ->
            QuotasTable.upsert(QuotasTable.userId, QuotasTable.nutriment) {
                it[userId] = row.userId
                it[nutriment] = row.nutriment
                it[valeurCible] = row.valeurCible
                it[estPersonnalise] = row.estPersonnalise
                it[valeurCalculee] = row.valeurCalculee
                it[unite] = row.unite
                it[updatedAt] = Clock.System.now()
            }
        }
    }

    suspend fun deleteByUserId(userId: String): Int = dbQuery {
        QuotasTable.deleteWhere { QuotasTable.userId eq userId }
    }

    private fun ResultRow.toRow() = QuotaRow(
        userId = this[QuotasTable.userId],
        nutriment = this[QuotasTable.nutriment],
        valeurCible = this[QuotasTable.valeurCible],
        estPersonnalise = this[QuotasTable.estPersonnalise],
        valeurCalculee = this[QuotasTable.valeurCalculee],
        unite = this[QuotasTable.unite],
        updatedAt = this[QuotasTable.updatedAt],
    )
}
