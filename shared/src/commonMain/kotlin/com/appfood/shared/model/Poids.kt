package com.appfood.shared.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Note: no updatedAt — weight entries are IMMUTABLE after creation.
// To correct a weight, delete and recreate the entry.
@Serializable
data class HistoriquePoids(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val poidsKg: Double,
    val estReference: Boolean,
    @Transient
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    @Contextual
    val createdAt: Instant,
)
