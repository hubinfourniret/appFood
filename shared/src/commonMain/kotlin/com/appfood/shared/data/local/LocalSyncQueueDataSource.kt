package com.appfood.shared.data.local

import com.appfood.shared.db.AppDatabase
import com.appfood.shared.db.Sync_queue

class LocalSyncQueueDataSource(private val database: AppDatabase) {

    private val queries = database.syncQueueQueriesQueries

    fun findAll(): List<Sync_queue> {
        return queries.findAll().executeAsList()
    }

    fun findByEntityType(entityType: String): List<Sync_queue> {
        return queries.findByEntityType(entityType).executeAsList()
    }

    fun insert(entry: Sync_queue) {
        queries.insert(
            id = entry.id,
            entity_type = entry.entity_type,
            entity_id = entry.entity_id,
            action = entry.action,
            payload_json = entry.payload_json,
            created_at = entry.created_at,
            retry_count = entry.retry_count,
            last_error = entry.last_error,
        )
    }

    fun deleteById(id: String) {
        queries.deleteById(id)
    }

    fun incrementRetry(id: String, lastError: String?) {
        queries.incrementRetry(lastError, id)
    }

    fun findRetryable(maxRetries: Long): List<Sync_queue> {
        return queries.findByRetryCountLessThan(maxRetries).executeAsList()
    }
}
