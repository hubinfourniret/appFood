package com.appfood.shared.data.impl

import com.appfood.shared.api.request.UpdateQuotaRequest
import com.appfood.shared.api.response.QuotaResponse
import com.appfood.shared.data.local.LocalQuotaDataSource
import com.appfood.shared.data.remote.QuotaApi
import com.appfood.shared.data.repository.QuotaRepository
import com.appfood.shared.db.Local_quota
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.model.QuotaStatus
import com.appfood.shared.util.AppResult
import kotlin.time.Clock

/**
 * Combines remote API calls with local SQLDelight cache.
 * Remote is the source of truth; local is a cache for offline access.
 */
class QuotaRepositoryImpl(
    private val quotaApi: QuotaApi,
    private val localQuotaDataSource: LocalQuotaDataSource,
) : QuotaRepository {

    override suspend fun getQuotas(userId: String): AppResult<List<QuotaJournalier>> {
        return try {
            val responses = quotaApi.getQuotas()
            val quotas = responses.map { it.toDomain(userId) }
            // Cache locally
            cacheQuotas(userId, quotas)
            AppResult.Success(quotas)
        } catch (e: Exception) {
            // Fallback to local cache
            val local = localQuotaDataSource.findByUser(userId)
            if (local.isNotEmpty()) {
                AppResult.Success(local.map { it.toDomain(userId) })
            } else {
                AppResult.Error(
                    code = "NETWORK_ERROR",
                    message = e.message ?: "Failed to fetch quotas",
                    cause = e,
                )
            }
        }
    }

    override suspend fun getQuotaStatus(userId: String, date: String): AppResult<List<QuotaStatus>> {
        return try {
            val response = quotaApi.getQuotaStatus(date)
            val statuses = response.data.map { statusResponse ->
                QuotaStatus(
                    nutriment = NutrimentType.valueOf(statusResponse.nutriment),
                    valeurCible = statusResponse.valeurCible,
                    valeurConsommee = statusResponse.valeurConsommee,
                    pourcentage = statusResponse.pourcentage,
                    unite = statusResponse.unite,
                )
            }
            AppResult.Success(statuses)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch quota status",
                cause = e,
            )
        }
    }

    override suspend fun saveQuotas(
        userId: String,
        quotas: List<QuotaJournalier>,
    ): AppResult<List<QuotaJournalier>> {
        // Save to local cache
        cacheQuotas(userId, quotas)
        return AppResult.Success(quotas)
    }

    override suspend fun updateQuota(
        userId: String,
        nutriment: NutrimentType,
        request: UpdateQuotaRequest,
    ): AppResult<QuotaJournalier> {
        return try {
            val response = quotaApi.updateQuota(nutriment.name, request)
            val quota = response.toDomain(userId)
            // Update local cache
            localQuotaDataSource.insertOrReplace(quota.toLocal())
            AppResult.Success(quota)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to update quota",
                cause = e,
            )
        }
    }

    override suspend fun resetQuota(
        userId: String,
        nutriment: NutrimentType,
    ): AppResult<QuotaJournalier> {
        return try {
            val response = quotaApi.resetQuota(nutriment.name)
            val quota = response.toDomain(userId)
            localQuotaDataSource.insertOrReplace(quota.toLocal())
            AppResult.Success(quota)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to reset quota",
                cause = e,
            )
        }
    }

    override suspend fun resetAllQuotas(userId: String): AppResult<List<QuotaJournalier>> {
        return try {
            val responses = quotaApi.resetAllQuotas()
            val quotas = responses.map { it.toDomain(userId) }
            cacheQuotas(userId, quotas)
            AppResult.Success(quotas)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to reset all quotas",
                cause = e,
            )
        }
    }

    override suspend fun recalculate(userId: String): AppResult<List<QuotaJournalier>> {
        return try {
            val responses = quotaApi.recalculate()
            val quotas = responses.map { it.toDomain(userId) }
            cacheQuotas(userId, quotas)
            AppResult.Success(quotas)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to recalculate quotas",
                cause = e,
            )
        }
    }

    // --- Mapping helpers ---

    private fun cacheQuotas(userId: String, quotas: List<QuotaJournalier>) {
        localQuotaDataSource.deleteByUser(userId)
        quotas.forEach { quota ->
            localQuotaDataSource.insertOrReplace(quota.toLocal())
        }
    }

    private fun QuotaResponse.toDomain(userId: String): QuotaJournalier {
        return QuotaJournalier(
            userId = userId,
            nutriment = NutrimentType.valueOf(nutriment),
            valeurCible = valeurCible,
            estPersonnalise = estPersonnalise,
            valeurCalculee = valeurCalculee,
            unite = unite,
            updatedAt =

                Clock.System.now(),
        )
    }

    private fun QuotaJournalier.toLocal(): Local_quota {
        return Local_quota(
            id = "${userId}_${nutriment.name}",
            user_id = userId,
            nutriment_type = nutriment.name,
            valeur_cible = valeurCible,
            unite = unite,
            est_personnalise = if (estPersonnalise) 1L else 0L,
        )
    }

    private fun Local_quota.toDomain(userId: String): QuotaJournalier {
        return QuotaJournalier(
            userId = userId,
            nutriment = NutrimentType.valueOf(nutriment_type),
            valeurCible = valeur_cible,
            estPersonnalise = est_personnalise == 1L,
            valeurCalculee = valeur_cible, // Local cache doesn't store valeurCalculee separately
            unite = unite,
            updatedAt = Clock.System.now(),
        )
    }
}
