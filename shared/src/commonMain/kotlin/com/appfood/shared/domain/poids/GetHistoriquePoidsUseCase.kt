package com.appfood.shared.domain.poids

import com.appfood.shared.data.repository.PoidsRepository
import com.appfood.shared.model.HistoriquePoids
import com.appfood.shared.util.AppResult

/**
 * Recupere l'historique de poids avec filtrage par dates.
 */
class GetHistoriquePoidsUseCase(
    private val poidsRepository: PoidsRepository,
) {
    suspend operator fun invoke(
        userId: String,
        dateFrom: String? = null,
        dateTo: String? = null,
    ): AppResult<List<HistoriquePoids>> {
        return poidsRepository.getHistory(userId, dateFrom, dateTo)
    }
}
