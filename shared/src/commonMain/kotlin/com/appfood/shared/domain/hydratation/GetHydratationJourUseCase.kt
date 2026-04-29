package com.appfood.shared.domain.hydratation

import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.model.HydratationJournaliere
import com.appfood.shared.util.AppResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetHydratationJourUseCase(
    private val hydratationRepository: HydratationRepository,
) {
    suspend operator fun invoke(userId: String): AppResult<HydratationJournaliere> {
        val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return hydratationRepository.getDaily(userId, today)
    }
}
