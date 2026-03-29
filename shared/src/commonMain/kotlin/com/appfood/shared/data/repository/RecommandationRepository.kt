package com.appfood.shared.data.repository

import com.appfood.shared.model.RecommandationAliment
import com.appfood.shared.util.AppResult

/**
 * Repository interface for recommendation operations.
 * Fetches recommendations from the remote API.
 */
interface RecommandationRepository {

    /** Fetch food recommendations for a given date. */
    suspend fun getAlimentRecommandations(
        userId: String,
        date: String,
        limit: Int = 10,
    ): AppResult<List<RecommandationAliment>>
}
