package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_recette
import kotlin.time.Clock

class LocalRecetteDataSource(private val database: AppDatabase) {

    private val queries = database.recetteQueriesQueries

    fun findById(id: String): Local_recette? {
        val result = queries.findById(id).executeAsOneOrNull()
        if (result != null) {
            updateAccessDate(id)
        }
        return result
    }

    fun findAll(): List<Local_recette> {
        return queries.findAll().executeAsList()
    }

    fun insertOrReplace(recette: Local_recette) {
        queries.insertOrReplace(
            id = recette.id,
            nom = recette.nom,
            description = recette.description,
            temps_preparation_min = recette.temps_preparation_min,
            temps_cuisson_min = recette.temps_cuisson_min,
            nb_portions = recette.nb_portions,
            regimes_compatibles = recette.regimes_compatibles,
            source = recette.source,
            type_repas = recette.type_repas,
            image_url = recette.image_url,
            calories = recette.calories,
            proteines = recette.proteines,
            glucides = recette.glucides,
            lipides = recette.lipides,
            fibres = recette.fibres,
            access_date = Clock.System.now().toEpochMilliseconds(),
            created_at = recette.created_at,
        )
    }

    fun updateAccessDate(id: String) {
        queries.updateAccessDate(
            access_date = Clock.System.now().toEpochMilliseconds(),
            id = id,
        )
    }

    fun deleteById(id: String) {
        queries.deleteById(id)
    }

    fun countAll(): Long {
        return queries.countAll().executeAsOne()
    }

    /**
     * Strategie LRU : supprime les entrees les plus anciennes (par access_date)
     * si le cache depasse maxEntries.
     */
    fun evictOldEntries(maxEntries: Int) {
        val count = countAll()
        if (count > maxEntries) {
            val toDelete = count - maxEntries
            queries.deleteOldest(toDelete)
        }
    }
}
