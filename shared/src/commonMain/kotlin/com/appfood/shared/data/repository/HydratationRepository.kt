package com.appfood.shared.data.repository

import com.appfood.shared.model.HydratationJournaliere
import com.appfood.shared.util.AppResult

/**
 * Repository interface for hydration operations.
 * Combines remote API calls with local SQLDelight cache.
 */
interface HydratationRepository {

    /** Get hydration data for a specific date. */
    suspend fun getDaily(userId: String, date: String): AppResult<HydratationJournaliere>

    /** Add a water intake entry. */
    suspend fun addEntry(userId: String, date: String, quantiteMl: Int): AppResult<HydratationJournaliere>

    /** Update the daily hydration objective. */
    suspend fun updateObjectif(userId: String, objectifMl: Int): AppResult<Unit>

    /** Reset the objective to the calculated default. */
    suspend fun resetObjectif(userId: String): AppResult<Unit>

    /** Get weekly hydration data. weekOf = any date in the target week (backend computes the range). */
    suspend fun getWeekly(userId: String, weekOf: String): AppResult<List<HydratationJournaliere>>
}
