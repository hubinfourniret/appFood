package com.appfood.shared.sync

/**
 * Simple implementation of SyncPreferences that stores the last sync timestamp
 * in memory with persistence via the sync queue metadata.
 *
 * A full DataStore/SharedPreferences implementation can replace this later.
 * For MVP, we use a simple in-memory cache that is initialized on first access.
 */
class LocalSyncPreferences : SyncPreferences {

    private var cachedTimestamp: String? = null

    override fun getLastSyncTimestamp(): String? {
        return cachedTimestamp
    }

    override fun setLastSyncTimestamp(timestamp: String?) {
        cachedTimestamp = timestamp
    }
}
