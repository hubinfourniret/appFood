package com.appfood.shared.data.impl

import com.appfood.shared.api.response.AlimentResponse
import com.appfood.shared.api.response.PortionListResponse
import com.appfood.shared.api.response.SearchAlimentResponse
import com.appfood.shared.data.remote.AlimentApi
import com.appfood.shared.data.remote.PortionApi
import com.appfood.shared.data.repository.AlimentRepository
import com.appfood.shared.util.AppResult

/**
 * Fetches aliment and portion data from the remote API.
 * Wraps all API calls in try/catch, returning AppResult.
 */
class AlimentRepositoryImpl(
    private val alimentApi: AlimentApi,
    private val portionApi: PortionApi,
) : AlimentRepository {

    override suspend fun search(
        query: String,
        regime: String?,
        page: Int?,
        size: Int?,
    ): AppResult<SearchAlimentResponse> {
        return try {
            val response = alimentApi.search(query, regime, page, size)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to search aliments",
                cause = e,
            )
        }
    }

    override suspend fun getById(id: String): AppResult<AlimentResponse> {
        return try {
            val response = alimentApi.getById(id)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch aliment",
                cause = e,
            )
        }
    }

    override suspend fun getByBarcode(code: String): AppResult<AlimentResponse> {
        return try {
            val response = alimentApi.getByBarcode(code)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch aliment by barcode",
                cause = e,
            )
        }
    }

    override suspend fun getPortions(alimentId: String?): AppResult<PortionListResponse> {
        return try {
            val response = portionApi.getPortions(alimentId)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch portions",
                cause = e,
            )
        }
    }
}
