package com.appfood.shared.data.repository

import com.appfood.shared.api.request.CreateRecetteRequest
import com.appfood.shared.model.Recette
import com.appfood.shared.util.AppResult

/**
 * Repository interface for recipe operations.
 * Fetches from remote API with local cache for offline access.
 */
interface RecetteRepository {

    /** List recipes with optional filtering and sorting. */
    suspend fun listRecettes(
        regime: String? = null,
        typeRepas: String? = null,
        sort: String? = null,
        query: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): AppResult<List<Recette>>

    /** Get a single recipe by ID. */
    suspend fun getRecette(id: String): AppResult<Recette>

    /** Create a custom recipe (RECETTES-03). */
    suspend fun createRecette(request: CreateRecetteRequest): AppResult<Recette>
}
