package com.appfood.shared.api.request

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushRequest(
    val journalEntries: List<AddJournalEntryRequest>,
    val poidsEntries: List<AddPoidsRequest>,
    val hydratationEntries: List<AddHydratationRequest>,
    val timestamp: String,
)
