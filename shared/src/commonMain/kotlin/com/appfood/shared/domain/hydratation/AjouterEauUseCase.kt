package com.appfood.shared.domain.hydratation

import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.model.HydratationJournaliere
import com.appfood.shared.util.AppResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AjouterEauUseCase(
    private val hydratationRepository: HydratationRepository,
) {
    suspend operator fun invoke(userId: String, quantiteMl: Int): AppResult<HydratationJournaliere> {
        val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return hydratationRepository.addEntry(userId, today, quantiteMl)
    }
}
