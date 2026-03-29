package com.appfood.shared.domain.poids

import com.appfood.shared.data.repository.PoidsRepository
import com.appfood.shared.util.AppResult
import kotlin.math.abs

/**
 * Detecte un changement de poids significatif (>1kg) par rapport au poids de reference.
 * Propose un recalcul des quotas si necessaire (POIDS-02).
 */
class DetecterChangementPoidsUseCase(
    private val poidsRepository: PoidsRepository,
) {
    data class Result(
        val changementSignificatif: Boolean,
        val poidsActuel: Double?,
        val poidsReference: Double?,
        val differenceKg: Double,
    )

    suspend operator fun invoke(userId: String): AppResult<Result> {
        val currentResult = poidsRepository.getCurrent(userId)
        val current = when (currentResult) {
            is AppResult.Success -> currentResult.data
            is AppResult.Error -> return AppResult.Error(
                code = currentResult.code,
                message = currentResult.message,
            )
        }

        if (current == null) {
            return AppResult.Success(
                Result(
                    changementSignificatif = false,
                    poidsActuel = null,
                    poidsReference = null,
                    differenceKg = 0.0,
                ),
            )
        }

        // Fetch history to find reference weight
        val historyResult = poidsRepository.getHistory(userId)
        val history = when (historyResult) {
            is AppResult.Success -> historyResult.data
            is AppResult.Error -> return AppResult.Error(
                code = historyResult.code,
                message = historyResult.message,
            )
        }

        val reference = history.firstOrNull { it.estReference }
        val poidsRef = reference?.poidsKg ?: current.poidsKg
        val diff = current.poidsKg - poidsRef

        return AppResult.Success(
            Result(
                changementSignificatif = abs(diff) > SEUIL_SIGNIFICATIF_KG,
                poidsActuel = current.poidsKg,
                poidsReference = poidsRef,
                differenceKg = diff,
            ),
        )
    }

    companion object {
        const val SEUIL_SIGNIFICATIF_KG = 1.0
    }
}
