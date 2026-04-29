package com.appfood.backend.routes.dto

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class SyncPushRequest(
    val journalEntries: List<AddJournalEntryRequest> = emptyList(),
    val poidsEntries: List<AddPoidsRequest> = emptyList(),
    val hydratationEntries: List<AddHydratationRequest> = emptyList(),
    val timestamp: String,
)

// --- Response DTOs ---

@Serializable
data class SyncPushResponse(
    val accepted: Int,
    val conflicts: List<SyncConflict> = emptyList(),
    val errors: List<SyncError> = emptyList(),
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
    val timestamp: String,
)
