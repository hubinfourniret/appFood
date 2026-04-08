package com.appfood.shared.data.repository

import com.appfood.shared.api.response.AlimentResponse
import com.appfood.shared.api.response.PortionListResponse
import com.appfood.shared.api.response.SearchAlimentResponse
import com.appfood.shared.util.AppResult

/**
 * Repository interface for aliment-related operations.
 * Search, retrieve by ID or barcode, and list portions.
 */
interface AlimentRepository {

    suspend fun search(
        query: String,
        regime: String? = null,
        page: Int? = null,
        size: Int? = null,
    ): AppResult<SearchAlimentResponse>

    suspend fun getById(id: String): AppResult<AlimentResponse>

    suspend fun getByBarcode(code: String): AppResult<AlimentResponse>

    suspend fun getPortions(alimentId: String? = null): AppResult<PortionListResponse>
}
