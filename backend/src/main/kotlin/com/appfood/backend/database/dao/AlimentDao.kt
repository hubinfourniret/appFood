package com.appfood.backend.database.dao

import com.appfood.backend.database.dbQuery
import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.SourceAliment
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.LikePattern
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

data class AlimentRow(
    val id: String,
    val nom: String,
    val marque: String?,
    val source: SourceAliment,
    val sourceId: String?,
    val codeBarres: String?,
    val categorie: String,
    val regimesCompatibles: String,
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
    val lipides: Double,
    val fibres: Double,
    val sel: Double,
    val sucres: Double,
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double,
)

class AlimentDao {
    suspend fun findById(id: String): AlimentRow? =
        dbQuery {
            AlimentsTable.selectAll()
                .where { AlimentsTable.id eq id }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findByCodeBarres(codeBarres: String): AlimentRow? =
        dbQuery {
            AlimentsTable.selectAll()
                .where { AlimentsTable.codeBarres eq codeBarres }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findBySourceId(
        source: SourceAliment,
        sourceId: String,
    ): AlimentRow? =
        dbQuery {
            AlimentsTable.selectAll()
                .where { (AlimentsTable.sourceAliment eq source) and (AlimentsTable.sourceId eq sourceId) }
                .map { it.toRow() }
                .singleOrNull()
        }

    suspend fun findBySource(
        source: SourceAliment,
        limit: Int = 100,
        offset: Long = 0,
    ): List<AlimentRow> =
        dbQuery {
            AlimentsTable.selectAll()
                .where { AlimentsTable.sourceAliment eq source }
                .limit(limit).offset(offset)
                .map { it.toRow() }
        }

    suspend fun findAll(
        limit: Int = 100,
        offset: Long = 0,
    ): List<AlimentRow> =
        dbQuery {
            AlimentsTable.selectAll()
                .limit(limit).offset(offset)
                .map { it.toRow() }
        }

    /**
     * PERF-01: candidates pour la recommandation, pre-filtres en SQL.
     *
     * Retourne au plus `limit` aliments tries par valeur DESC du nutriment cible,
     * optionnellement filtres par regime alimentaire (CSV/JSON, match via LIKE).
     * Si plusieurs nutriments sont en deficit, appelle cette methode plusieurs fois
     * et fusionne cote service.
     *
     * @param nutrient nom logique du nutriment (proteines, fer, calcium, vitamineB12,
     *   fibres, zinc, magnesium, vitamineD, omega3). Toute valeur inconnue retombe
     *   sur proteines.
     * @param regime ex: "VEGAN" ou "VEGETARIEN" - filtre LIKE sur regimes_compatibles.
     *   Passer null pour ne pas filtrer.
     * @param limit max 200 par defaut
     */
    suspend fun findCandidatesByNutrientDeficit(
        nutrient: String,
        regime: String?,
        limit: Int = 200,
    ): List<AlimentRow> =
        dbQuery {
            val sortColumn: Column<Double> =
                when (nutrient) {
                    "proteines" -> AlimentsTable.proteines
                    "fer" -> AlimentsTable.fer
                    "calcium" -> AlimentsTable.calcium
                    "vitamineB12" -> AlimentsTable.vitamineB12
                    "fibres" -> AlimentsTable.fibres
                    "zinc" -> AlimentsTable.zinc
                    "magnesium" -> AlimentsTable.magnesium
                    "vitamineD" -> AlimentsTable.vitamineD
                    "omega3" -> AlimentsTable.omega3
                    "vitamineC" -> AlimentsTable.vitamineC
                    else -> AlimentsTable.proteines
                }
            val query = AlimentsTable.selectAll()
            if (regime != null) {
                val pattern = LikePattern("%${escapeLike(regime)}%", '\\')
                query.andWhere { AlimentsTable.regimesCompatibles like pattern }
            }
            query
                .orderBy(sortColumn, SortOrder.DESC)
                .limit(limit)
                .map { it.toRow() }
        }

    private fun escapeLike(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")

    suspend fun count(): Long =
        dbQuery {
            AlimentsTable.selectAll().count()
        }

    suspend fun insert(row: AlimentRow): AlimentRow =
        dbQuery {
            AlimentsTable.insert {
                it[id] = row.id
                it[nom] = row.nom
                it[marque] = row.marque
                it[sourceAliment] = row.source
                it[sourceId] = row.sourceId
                it[codeBarres] = row.codeBarres
                it[categorie] = row.categorie
                it[regimesCompatibles] = row.regimesCompatibles
                it[calories] = row.calories
                it[proteines] = row.proteines
                it[glucides] = row.glucides
                it[lipides] = row.lipides
                it[fibres] = row.fibres
                it[sel] = row.sel
                it[sucres] = row.sucres
                it[fer] = row.fer
                it[calcium] = row.calcium
                it[zinc] = row.zinc
                it[magnesium] = row.magnesium
                it[vitamineB12] = row.vitamineB12
                it[vitamineD] = row.vitamineD
                it[vitamineC] = row.vitamineC
                it[omega3] = row.omega3
                it[omega6] = row.omega6
            }
            row
        }

    suspend fun insertBatch(rows: List<AlimentRow>) =
        dbQuery {
            AlimentsTable.batchInsert(rows, shouldReturnGeneratedValues = false) { row ->
                this[AlimentsTable.id] = row.id
                this[AlimentsTable.nom] = row.nom
                this[AlimentsTable.marque] = row.marque
                this[AlimentsTable.sourceAliment] = row.source
                this[AlimentsTable.sourceId] = row.sourceId
                this[AlimentsTable.codeBarres] = row.codeBarres
                this[AlimentsTable.categorie] = row.categorie
                this[AlimentsTable.regimesCompatibles] = row.regimesCompatibles
                this[AlimentsTable.calories] = row.calories
                this[AlimentsTable.proteines] = row.proteines
                this[AlimentsTable.glucides] = row.glucides
                this[AlimentsTable.lipides] = row.lipides
                this[AlimentsTable.fibres] = row.fibres
                this[AlimentsTable.sel] = row.sel
                this[AlimentsTable.sucres] = row.sucres
                this[AlimentsTable.fer] = row.fer
                this[AlimentsTable.calcium] = row.calcium
                this[AlimentsTable.zinc] = row.zinc
                this[AlimentsTable.magnesium] = row.magnesium
                this[AlimentsTable.vitamineB12] = row.vitamineB12
                this[AlimentsTable.vitamineD] = row.vitamineD
                this[AlimentsTable.vitamineC] = row.vitamineC
                this[AlimentsTable.omega3] = row.omega3
                this[AlimentsTable.omega6] = row.omega6
            }
        }

    /**
     * Upsert a batch of aliments using (source, source_id) as conflict keys.
     * On conflict, updates nutritional values but preserves the existing UUID.
     */
    suspend fun upsertBatch(rows: List<AlimentRow>) =
        dbQuery {
            AlimentsTable.batchUpsert(
                rows,
                AlimentsTable.sourceAliment,
                AlimentsTable.sourceId,
                shouldReturnGeneratedValues = false,
            ) { row ->
                this[AlimentsTable.id] = row.id
                this[AlimentsTable.nom] = row.nom
                this[AlimentsTable.marque] = row.marque
                this[AlimentsTable.sourceAliment] = row.source
                this[AlimentsTable.sourceId] = row.sourceId
                this[AlimentsTable.codeBarres] = row.codeBarres
                this[AlimentsTable.categorie] = row.categorie
                this[AlimentsTable.regimesCompatibles] = row.regimesCompatibles
                this[AlimentsTable.calories] = row.calories
                this[AlimentsTable.proteines] = row.proteines
                this[AlimentsTable.glucides] = row.glucides
                this[AlimentsTable.lipides] = row.lipides
                this[AlimentsTable.fibres] = row.fibres
                this[AlimentsTable.sel] = row.sel
                this[AlimentsTable.sucres] = row.sucres
                this[AlimentsTable.fer] = row.fer
                this[AlimentsTable.calcium] = row.calcium
                this[AlimentsTable.zinc] = row.zinc
                this[AlimentsTable.magnesium] = row.magnesium
                this[AlimentsTable.vitamineB12] = row.vitamineB12
                this[AlimentsTable.vitamineD] = row.vitamineD
                this[AlimentsTable.vitamineC] = row.vitamineC
                this[AlimentsTable.omega3] = row.omega3
                this[AlimentsTable.omega6] = row.omega6
            }
        }

    /**
     * Count how many of the given sourceIds already exist in the database for the given source.
     */
    suspend fun countExisting(
        source: SourceAliment,
        sourceIds: List<String>,
    ): Int =
        dbQuery {
            AlimentsTable.selectAll()
                .where { (AlimentsTable.sourceAliment eq source) and (AlimentsTable.sourceId inList sourceIds) }
                .count()
                .toInt()
        }

    suspend fun delete(id: String): Boolean =
        dbQuery {
            AlimentsTable.deleteWhere { AlimentsTable.id eq id } > 0
        }

    // --- Diagnostic helpers ---

    // TODO: remove after diagnostics complete (incident 2026-04-10)
    /**
     * Vide la table aliments. A utiliser uniquement pour le hack FORCE_REIMPORT_ALL.
     * ATTENTION : cascade sur ingredients et journal_entries via les FK (pas de ON DELETE CASCADE —
     * appelez d'abord JournalEntryDao.deleteAll() puis RecetteDao.truncateAll()).
     */
    suspend fun truncateAll() =
        dbQuery {
            AlimentsTable.deleteAll()
        }

    private fun ResultRow.toRow() =
        AlimentRow(
            id = this[AlimentsTable.id],
            nom = this[AlimentsTable.nom],
            marque = this[AlimentsTable.marque],
            source = this[AlimentsTable.sourceAliment],
            sourceId = this[AlimentsTable.sourceId],
            codeBarres = this[AlimentsTable.codeBarres],
            categorie = this[AlimentsTable.categorie],
            regimesCompatibles = this[AlimentsTable.regimesCompatibles],
            calories = this[AlimentsTable.calories],
            proteines = this[AlimentsTable.proteines],
            glucides = this[AlimentsTable.glucides],
            lipides = this[AlimentsTable.lipides],
            fibres = this[AlimentsTable.fibres],
            sel = this[AlimentsTable.sel],
            sucres = this[AlimentsTable.sucres],
            fer = this[AlimentsTable.fer],
            calcium = this[AlimentsTable.calcium],
            zinc = this[AlimentsTable.zinc],
            magnesium = this[AlimentsTable.magnesium],
            vitamineB12 = this[AlimentsTable.vitamineB12],
            vitamineD = this[AlimentsTable.vitamineD],
            vitamineC = this[AlimentsTable.vitamineC],
            omega3 = this[AlimentsTable.omega3],
            omega6 = this[AlimentsTable.omega6],
        )
}
