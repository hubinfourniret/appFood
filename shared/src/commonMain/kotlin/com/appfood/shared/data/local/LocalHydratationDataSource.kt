package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_hydratation

class LocalHydratationDataSource(private val database: AppDatabase) {

    private val queries = database.hydratationQueriesQueries

    fun findByUserAndDate(userId: String, date: String): Local_hydratation? {
        return queries.findByUserAndDate(userId, date).executeAsOneOrNull()
    }

    fun insertOrReplace(hydratation: Local_hydratation) {
        queries.insertOrReplace(
            id = hydratation.id,
            user_id = hydratation.user_id,
            date = hydratation.date,
            objectif_ml = hydratation.objectif_ml,
            total_ml = hydratation.total_ml,
            sync_status = hydratation.sync_status,
            updated_at = hydratation.updated_at,
        )
    }

    fun findPending(): List<Local_hydratation> {
        return queries.findPending().executeAsList()
    }

    fun updateSyncStatus(id: String, syncStatus: String) {
        queries.updateSyncStatus(syncStatus, id)
    }
}
