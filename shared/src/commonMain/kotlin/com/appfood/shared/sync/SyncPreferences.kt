package com.appfood.shared.sync

/**
 * Abstraction for persisting sync-related preferences (e.g., lastSyncTimestamp).
 * Platform-specific implementations use SharedPreferences (Android) or NSUserDefaults (iOS).
 *
 * At MVP, a simple in-memory + SQLDelight-backed implementation is used via LocalSyncPreferences.
 */
interface SyncPreferences {
    fun getLastSyncTimestamp(): String?
    fun setLastSyncTimestamp(timestamp: String?)
}
