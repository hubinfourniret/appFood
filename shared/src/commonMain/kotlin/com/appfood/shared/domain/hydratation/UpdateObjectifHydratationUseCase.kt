package com.appfood.shared.domain.hydratation

import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.util.AppResult

/**
 * Met a jour l'objectif journalier d'hydratation.
 */
class UpdateObjectifHydratationUseCase(
    private val hydratationRepository: HydratationRepository,
) {
    suspend operator fun invoke(userId: String, objectifMl: Int): AppResult<Unit> {
        if (objectifMl < 500 || objectifMl > 10000) {
            return AppResult.Error(
                code = "INVALID_OBJECTIF",
                message = "L'objectif doit etre entre 500 ml et 10 000 ml",
            )
        }
        return hydratationRepository.updateObjectif(userId, objectifMl)
    }

    suspend fun reset(userId: String): AppResult<Unit> {
        return hydratationRepository.resetObjectif(userId)
    }
}
