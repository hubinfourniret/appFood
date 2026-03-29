package com.appfood.shared.data.repository

import com.appfood.shared.api.request.UpdateQuotaRequest
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.model.QuotaStatus
import com.appfood.shared.util.AppResult

/**
 * Repository interface for quota operations.
 * Combines remote API calls with local SQLDelight cache.
 */
interface QuotaRepository {

    /** Fetch all quotas for the user (from local cache or remote). */
    suspend fun getQuotas(userId: String): AppResult<List<QuotaJournalier>>

    /** Fetch quota status (consumed vs target) for a given date. */
    suspend fun getQuotaStatus(userId: String, date: String): AppResult<List<QuotaStatus>>

    /** Save computed or updated quotas locally and remotely. */
    suspend fun saveQuotas(userId: String, quotas: List<QuotaJournalier>): AppResult<List<QuotaJournalier>>

    /** Update a single quota manually (customization). */
    suspend fun updateQuota(userId: String, nutriment: NutrimentType, request: UpdateQuotaRequest): AppResult<QuotaJournalier>

    /** Reset a single quota to its calculated value. */
    suspend fun resetQuota(userId: String, nutriment: NutrimentType): AppResult<QuotaJournalier>

    /** Reset all quotas to their calculated values. */
    suspend fun resetAllQuotas(userId: String): AppResult<List<QuotaJournalier>>

    /** Force recalculate all quotas from the current profile. */
    suspend fun recalculate(userId: String): AppResult<List<QuotaJournalier>>
}
