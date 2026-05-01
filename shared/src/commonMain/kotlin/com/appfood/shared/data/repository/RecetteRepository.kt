package com.appfood.shared.data.repository

import com.appfood.shared.api.request.CreateRecetteRequest
import com.appfood.shared.api.request.UpdateRecetteRequest
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

    /** TACHE-516 : list user's personal recipes. */
    suspend fun listMyRecettes(): AppResult<List<Recette>>

    /** TACHE-516 : update a personal recipe. */
    suspend fun updateRecette(id: String, request: UpdateRecetteRequest): AppResult<Recette>

    /** TACHE-516 : delete a personal recipe. */
    suspend fun deleteRecette(id: String): AppResult<Unit>
}
