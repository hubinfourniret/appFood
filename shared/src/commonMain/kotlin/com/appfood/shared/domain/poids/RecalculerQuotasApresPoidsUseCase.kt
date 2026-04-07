package com.appfood.shared.domain.poids

import com.appfood.shared.api.request.UpdateProfileRequest
import com.appfood.shared.data.repository.QuotaRepository
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.util.AppResult

/**
 * Detects significant weight change and triggers quota recalculation if needed (POIDS-02).
 *
 * Flow:
 * 1. Call DetecterChangementPoidsUseCase
 * 2. If change > 1kg, update the reference weight in the profile
 * 3. Trigger quota recalculation via QuotaRepository.recalculate()
 * 4. Return a result with an explanatory message
 */
class RecalculerQuotasApresPoidsUseCase(
    private val detecterChangementPoidsUseCase: DetecterChangementPoidsUseCase,
    private val quotaRepository: QuotaRepository,
    private val userRepository: UserRepository,
) {
    data class Result(
        val recalculated: Boolean,
        val message: String,
        val newQuotas: List<QuotaJournalier>?,
    )

    suspend operator fun invoke(userId: String): AppResult<Result> {
        // 1. Detect weight change
        val detectionResult = detecterChangementPoidsUseCase(userId)
        val detection = when (detectionResult) {
            is AppResult.Success -> detectionResult.data
            is AppResult.Error -> return AppResult.Error(
                code = detectionResult.code,
                message = detectionResult.message,
            )
        }

        // 2. If no significant change, return early
        if (!detection.changementSignificatif) {
            return AppResult.Success(
                Result(
                    recalculated = false,
                    message = "Pas de changement de poids significatif detecte " +
                        "(variation de ${formatDiff(detection.differenceKg)} kg).",
                    newQuotas = null,
                ),
            )
        }

        // 3. Update the reference weight in the user profile
        val currentWeight = detection.poidsActuel
            ?: return AppResult.Error(
                code = "NO_CURRENT_WEIGHT",
                message = "Aucun poids actuel disponible pour mettre a jour le profil.",
            )

        val updateResult = userRepository.updateProfile(
            UpdateProfileRequest(poidsKg = currentWeight),
        )
        when (updateResult) {
            is AppResult.Success -> { /* profile updated */ }
            is AppResult.Error -> return AppResult.Error(
                code = "PROFILE_UPDATE_ERROR",
                message = "Impossible de mettre a jour le poids de reference: ${updateResult.message}",
            )
        }

        // 4. Trigger quota recalculation via the API
        val recalculateResult = quotaRepository.recalculate(userId)
        val newQuotas = when (recalculateResult) {
            is AppResult.Success -> recalculateResult.data
            is AppResult.Error -> return AppResult.Error(
                code = "RECALCULATE_ERROR",
                message = "Impossible de recalculer les quotas: ${recalculateResult.message}",
            )
        }

        // 5. Return success with explanatory message
        val direction = if (detection.differenceKg > 0) "augmente" else "diminue"
        return AppResult.Success(
            Result(
                recalculated = true,
                message = "Votre poids a $direction de ${formatDiff(detection.differenceKg)} kg " +
                    "(${detection.poidsReference} -> ${detection.poidsActuel} kg). " +
                    "Vos quotas nutritionnels ont ete recalcules.",
                newQuotas = newQuotas,
            ),
        )
    }

    private fun formatDiff(diff: Double): String {
        val absDiff = kotlin.math.abs(diff)
        return ((absDiff * 10).toInt() / 10.0).toString()
    }
}
