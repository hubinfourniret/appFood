package com.appfood.shared.sync

/**
 * Interface for enqueuing entries in the sync queue.
 * Extracted from SyncManager to enable testability (fakes in ViewModel tests).
 */
interface SyncEnqueuer {
    fun enqueue(
        entityType: String,
        entityId: String,
        action: String,
        payloadJson: String,
    )
}
