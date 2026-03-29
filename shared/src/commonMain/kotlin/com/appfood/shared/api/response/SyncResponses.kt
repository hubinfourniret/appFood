package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushResponse(
    val accepted: Int,
    val conflicts: List<SyncConflict>,
    val errors: List<SyncError>,
)

@Serializable
data class SyncConflict(
    val entityType: String,
    val entityId: String,
    val clientVersion: String,
    val serverVersion: String,
    val resolution: String,
)

@Serializable
data class SyncError(
    val entityType: String,
    val entityId: String,
    val error: String,
)

@Serializable
data class SyncPullResponse(
    val journalEntries: List<JournalEntryResponse>,
    val poidsEntries: List<PoidsResponse>,
    val hydratationEntries: List<HydratationResponse>,
    val quotas: List<QuotaResponse>,
    val profile: ProfileResponse? = null,
    val preferences: PreferencesResponse? = null,
    val timestamp: String,
)
