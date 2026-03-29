package com.appfood.shared.domain.hydratation

import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.model.HydratationJournaliere
import com.appfood.shared.util.AppResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Recupere les donnees d'hydratation du jour courant.
 */
class GetHydratationJourUseCase(
    private val hydratationRepository: HydratationRepository,
) {
    suspend operator fun invoke(userId: String): AppResult<HydratationJournaliere> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        return hydratationRepository.getDaily(userId, today)
    }
}
