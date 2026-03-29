package com.appfood.shared.domain.recette

import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.model.Recette
import com.appfood.shared.util.AppResult

/**
 * Recherche et filtre les recettes avec pagination.
 */
class RechercherRecettesUseCase(
    private val recetteRepository: RecetteRepository,
) {
    suspend operator fun invoke(
        regime: String? = null,
        typeRepas: String? = null,
        sort: String? = null,
        query: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): AppResult<List<Recette>> {
        return recetteRepository.listRecettes(regime, typeRepas, sort, query, page, limit)
    }
}
