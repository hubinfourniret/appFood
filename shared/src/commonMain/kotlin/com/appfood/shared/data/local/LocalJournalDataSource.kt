package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_journal_entry

class LocalJournalDataSource(private val database: AppDatabase) {

    private val queries = database.journalQueriesQueries

    fun findByUserAndDate(userId: String, date: String): List<Local_journal_entry> {
        return queries.findByUserAndDate(userId, date).executeAsList()
    }

    fun findById(id: String): Local_journal_entry? {
        return queries.findById(id).executeAsOneOrNull()
    }

    fun insertOrReplace(entry: Local_journal_entry) {
        queries.insertOrReplace(
            id = entry.id,
            user_id = entry.user_id,
            aliment_id = entry.aliment_id,
            recette_id = entry.recette_id,
            nom = entry.nom,
            meal_type = entry.meal_type,
            quantite_grammes = entry.quantite_grammes,
            nb_portions = entry.nb_portions,
            date = entry.date,
            calories = entry.calories,
            proteines = entry.proteines,
            glucides = entry.glucides,
            lipides = entry.lipides,
            fibres = entry.fibres,
            sel = entry.sel,
            sucres = entry.sucres,
            fer = entry.fer,
            calcium = entry.calcium,
            zinc = entry.zinc,
            magnesium = entry.magnesium,
            vitamine_b12 = entry.vitamine_b12,
            vitamine_d = entry.vitamine_d,
            vitamine_c = entry.vitamine_c,
            omega_3 = entry.omega_3,
            omega_6 = entry.omega_6,
            sync_status = entry.sync_status,
            created_at = entry.created_at,
            updated_at = entry.updated_at,
        )
    }

    fun deleteById(id: String) {
        queries.deleteById(id)
    }

    fun findPending(): List<Local_journal_entry> {
        return queries.findPending().executeAsList()
    }

    fun updateSyncStatus(id: String, syncStatus: String) {
        queries.updateSyncStatus(syncStatus, id)
    }
}
