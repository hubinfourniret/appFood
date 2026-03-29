package com.appfood.shared.domain.hydratation

import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.model.HydratationJournaliere
import com.appfood.shared.util.AppResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Ajoute une quantite d'eau au suivi d'hydratation du jour.
 */
class AjouterEauUseCase(
    private val hydratationRepository: HydratationRepository,
) {
    suspend operator fun invoke(userId: String, quantiteMl: Int): AppResult<HydratationJournaliere> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        return hydratationRepository.addEntry(userId, today, quantiteMl)
    }
}
