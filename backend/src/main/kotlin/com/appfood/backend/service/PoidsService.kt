package com.appfood.backend.service

import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.database.dao.PoidsHistoryRow
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.AddPoidsRequest
import com.appfood.backend.routes.dto.AddPoidsResponse
import com.appfood.backend.routes.dto.PoidsListResponse
import com.appfood.backend.routes.dto.PoidsResponse
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.math.abs

class PoidsService(
    private val poidsHistoryDao: PoidsHistoryDao,
) {
    private val logger = LoggerFactory.getLogger("PoidsService")

    /**
     * Retourne l'historique de poids filtre par dates optionnelles.
     */
    suspend fun getHistory(userId: String, dateFrom: LocalDate?, dateTo: LocalDate?): PoidsListResponse {
        val rows = if (dateFrom != null && dateTo != null) {
            poidsHistoryDao.findByUserAndDateRange(userId, dateFrom, dateTo)
        } else {
            poidsHistoryDao.findByUserId(userId)
        }

        val data = rows.map { it.toResponse() }
        val poidsCourant = rows.firstOrNull()?.poidsKg // Deja trie DESC par date
        val poidsMin = rows.minOfOrNull { it.poidsKg }
        val poidsMax = rows.maxOfOrNull { it.poidsKg }

        return PoidsListResponse(
            data = data,
            total = data.size,
            poidsCourant = poidsCourant,
            poidsMin = poidsMin,
            poidsMax = poidsMax,
        )
    }

    /**
     * Ajoute une pesee. Detecte un changement significatif (>1kg) par rapport a la reference.
     */
    suspend fun addPoids(userId: String, request: AddPoidsRequest): AddPoidsResponse {
        val date = parseDate(request.date)
        if (request.poidsKg <= 0.0) {
            throw ValidationException("poidsKg doit etre > 0")
        }

        val id = request.id ?: UUID.randomUUID().toString()

        // Trouver la reference pour detecter un changement significatif
        val reference = poidsHistoryDao.findReference(userId)
            ?: poidsHistoryDao.findLatest(userId)

        val isFirst = reference == null
        val estReference = isFirst // La premiere pesee est la reference

        val row = poidsHistoryDao.insert(
            id = id,
            userId = userId,
            date = date,
            poidsKg = request.poidsKg,
            estReference = estReference,
        )

        // Detecter changement significatif (>1kg)
        val changementSignificatif = if (reference != null) {
            abs(request.poidsKg - reference.poidsKg) > SEUIL_CHANGEMENT_KG
        } else {
            false
        }

        val messageRecalcul = if (changementSignificatif) {
            val diff = request.poidsKg - reference!!.poidsKg
            val direction = if (diff > 0) "augmentation" else "diminution"
            "Changement significatif detecte: $direction de ${"%.1f".format(abs(diff))} kg. " +
                "Les quotas nutritionnels peuvent necessiter un recalcul."
        } else {
            null
        }

        logger.info("AddPoids: userId=$userId, date=$date, poidsKg=${request.poidsKg}, changement=$changementSignificatif")

        return AddPoidsResponse(
            poids = row.toResponse(),
            changementSignificatif = changementSignificatif,
            messageRecalcul = messageRecalcul,
        )
    }

    /**
     * Supprime une pesee.
     */
    suspend fun deletePoids(userId: String, poidsId: String) {
        val deleted = poidsHistoryDao.delete(poidsId, userId)
        if (!deleted) {
            throw NotFoundException("Pesee non trouvee: $poidsId")
        }
        logger.info("DeletePoids: userId=$userId, poidsId=$poidsId")
    }

    private fun PoidsHistoryRow.toResponse() = PoidsResponse(
        id = id,
        date = date.toString(),
        poidsKg = poidsKg,
        estReference = estReference,
        createdAt = createdAt.toString(),
    )

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
        }
    }

    companion object {
        private const val SEUIL_CHANGEMENT_KG = 1.0
    }
}
