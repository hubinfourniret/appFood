package com.appfood.shared.data.repository

import com.appfood.shared.model.HistoriquePoids
import com.appfood.shared.util.AppResult

/**
 * Repository interface for weight tracking operations.
 * Combines remote API calls with local SQLDelight cache.
 */
interface PoidsRepository {

    /** Get weight history with optional date range filtering. */
    suspend fun getHistory(userId: String, dateFrom: String? = null, dateTo: String? = null): AppResult<List<HistoriquePoids>>

    /** Add a new weight entry. */
    suspend fun addEntry(userId: String, date: String, poidsKg: Double): AppResult<HistoriquePoids>

    /** Get the most recent weight entry. */
    suspend fun getCurrent(userId: String): AppResult<HistoriquePoids?>
}
