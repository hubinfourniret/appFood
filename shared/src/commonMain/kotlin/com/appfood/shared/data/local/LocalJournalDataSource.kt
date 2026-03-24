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
            meal_type = entry.meal_type,
            quantite_grammes = entry.quantite_grammes,
            date = entry.date,
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
