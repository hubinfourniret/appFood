package com.appfood.shared.domain.quota

import com.appfood.shared.data.repository.QuotaRepository
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.ObjectifPoids
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.model.UserProfile
import com.appfood.shared.util.AppResult
import kotlin.time.Clock

/**
 * Recalculates all quotas from the current user profile.
 * Preserves manually customized quotas (estPersonnalise = true) by keeping
 * their valeurCible but updating the valeurCalculee.
 *
 * Triggered when the user profile changes (weight, activity level, etc.).
 */
class RecalculerQuotasUseCase(
    private val calculerQuotasUseCase: CalculerQuotasUseCase,
    private val quotaRepository: QuotaRepository,
    private val userRepository: UserRepository,
) {

    suspend fun execute(userId: String): AppResult<List<QuotaJournalier>> {
        // 1. Get the current user profile
        val profileResult = userRepository.getCurrentUser()
        val userProfileResponse = when (profileResult) {
            is AppResult.Success -> profileResult.data
            is AppResult.Error -> return AppResult.Error(
                code = "PROFILE_NOT_FOUND",
                message = "Cannot recalculate quotas: user profile not available",
            )
        }

        val profile = userProfileResponse.profile
            ?: return AppResult.Error(
                code = "PROFILE_INCOMPLETE",
                message = "Cannot recalculate quotas: user profile is incomplete",
            )

        // Build a UserProfile from the response to use in calculation
        val userProfile = UserProfile(
            userId = userId,
            sexe = Sexe.valueOf(profile.sexe),
            age = profile.age,
            poidsKg = profile.poidsKg,
            tailleCm = profile.tailleCm,
            regimeAlimentaire = RegimeAlimentaire.valueOf(profile.regimeAlimentaire),
            niveauActivite = NiveauActivite.valueOf(profile.niveauActivite),
            onboardingComplete = profile.onboardingComplete,
            objectifPoids = profile.objectifPoids?.let {
                ObjectifPoids.valueOf(it)
            },
            updatedAt = Clock.System.now(),
        )

        // 2. Calculate new quotas from the profile
        val calculationResult = calculerQuotasUseCase.execute(userProfile)
        val newQuotas = when (calculationResult) {
            is AppResult.Success -> calculationResult.data
            is AppResult.Error -> return calculationResult
        }

        // 3. Fetch existing quotas to check for manual customizations
        val existingResult = quotaRepository.getQuotas(userId)
        val existingQuotas = when (existingResult) {
            is AppResult.Success -> existingResult.data
            is AppResult.Error -> emptyList() // First time — no existing quotas
        }

        // 4. Merge: preserve customized valeurCible, update valeurCalculee
        val mergedQuotas = newQuotas.map { newQuota ->
            val existing = existingQuotas.find { it.nutriment == newQuota.nutriment }
            if (existing != null && existing.estPersonnalise) {
                // Keep the user's custom value but update the calculated reference
                newQuota.copy(
                    valeurCible = existing.valeurCible,
                    estPersonnalise = true,
                    valeurCalculee = newQuota.valeurCalculee,
                )
            } else {
                newQuota
            }
        }

        // 5. Save the merged quotas
        return quotaRepository.saveQuotas(userId, mergedQuotas)
    }
}
