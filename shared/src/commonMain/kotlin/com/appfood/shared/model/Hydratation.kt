package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class HydratationJournaliere(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val quantiteMl: Int,
    val objectifMl: Int,
    val estObjectifPersonnalise: Boolean,
    @Transient
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val entrees: List<HydratationEntry>,
    @Contextual
    val updatedAt: Instant,
)

@Serializable
data class HydratationEntry(
    val id: String,
    @Contextual
    val heure: Instant,
    val quantiteMl: Int,
)
