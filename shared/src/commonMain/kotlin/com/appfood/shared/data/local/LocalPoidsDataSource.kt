package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Local_poids

class LocalPoidsDataSource(private val database: AppDatabase) {

    private val queries = database.poidsQueriesQueries

    fun findByUser(userId: String): List<Local_poids> {
        return queries.findByUser(userId).executeAsList()
    }

    fun insertOrReplace(poids: Local_poids) {
        queries.insertOrReplace(
            id = poids.id,
            user_id = poids.user_id,
            poids_kg = poids.poids_kg,
            date = poids.date,
            sync_status = poids.sync_status,
            created_at = poids.created_at,
        )
    }

    fun findPending(): List<Local_poids> {
        return queries.findPending().executeAsList()
    }

    fun updateSyncStatus(id: String, syncStatus: String) {
        queries.updateSyncStatus(syncStatus, id)
    }
}
