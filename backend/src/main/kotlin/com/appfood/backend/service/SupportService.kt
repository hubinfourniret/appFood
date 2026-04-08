package com.appfood.backend.service

import com.appfood.backend.database.dao.FaqDao
import com.appfood.backend.database.dao.FaqRow
import com.appfood.backend.routes.dto.FaqListResponse
import com.appfood.backend.routes.dto.FaqResponse

class SupportService(
    private val faqDao: FaqDao,
) {
    /**
     * Retourne toutes les FAQ actives, triees par ordre.
     */
    suspend fun getFaq(): FaqListResponse {
        val rows = faqDao.findAllActive()
        return FaqListResponse(
            data = rows.map { it.toResponse() },
        )
    }

    private fun FaqRow.toResponse() =
        FaqResponse(
            id = id,
            theme = theme,
            question = question,
            reponse = reponse,
            ordre = ordre,
        )
}
