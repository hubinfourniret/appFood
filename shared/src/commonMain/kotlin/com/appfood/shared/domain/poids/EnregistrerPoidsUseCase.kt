package com.appfood.shared.domain.poids

import com.appfood.shared.data.repository.PoidsRepository
import com.appfood.shared.model.HistoriquePoids
import com.appfood.shared.util.AppResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EnregistrerPoidsUseCase(
    private val poidsRepository: PoidsRepository,
) {
    suspend operator fun invoke(userId: String, poidsKg: Double): AppResult<HistoriquePoids> {
        if (poidsKg < 20.0 || poidsKg > 500.0) {
            return AppResult.Error(
                code = "INVALID_POIDS",
                message = "Le poids doit etre entre 20 et 500 kg",
            )
        }
        val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return poidsRepository.addEntry(userId, today, poidsKg)
    }
}
