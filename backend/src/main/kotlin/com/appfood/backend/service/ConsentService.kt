package com.appfood.backend.service

import com.appfood.backend.database.dao.ConsentDao
import com.appfood.backend.database.dao.ConsentRow
import com.appfood.backend.database.tables.ConsentType
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.ConsentListResponse
import com.appfood.backend.routes.dto.ConsentResponse
import com.appfood.backend.routes.dto.InitialConsentRequest
import com.appfood.backend.routes.dto.UpdateConsentRequest
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.UUID

class ConsentService(
    private val consentDao: ConsentDao,
) {
    private val logger = LoggerFactory.getLogger("ConsentService")

    /**
     * Retourne tous les consentements d'un utilisateur.
     */
    suspend fun getConsents(userId: String): ConsentListResponse {
        val rows = consentDao.findByUserId(userId)
        return ConsentListResponse(
            data = rows.map { it.toResponse() },
        )
    }

    /**
     * Met a jour un consentement (upsert).
     */
    suspend fun updateConsent(userId: String, typeStr: String, request: UpdateConsentRequest): ConsentResponse {
        val type = parseConsentType(typeStr)

        val now = Clock.System.now()
        val existing = consentDao.findByUserAndType(userId, type)
        val id = existing?.id ?: UUID.randomUUID().toString()

        val row = ConsentRow(
            id = id,
            userId = userId,
            type = type,
            accepte = request.accepte,
            dateConsentement = now,
            versionPolitique = request.versionPolitique,
        )
        consentDao.upsert(row)

        logger.info("UpdateConsent: userId=$userId, type=$type, accepte=${request.accepte}")

        return row.toResponse()
    }

    /**
     * Enregistre les consentements initiaux (onboarding) — cree les 3 types d'un coup.
     */
    suspend fun initialConsents(userId: String, request: InitialConsentRequest): ConsentListResponse {
        val now = Clock.System.now()

        val consents = listOf(
            ConsentRow(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = ConsentType.ANALYTICS,
                accepte = request.analytics,
                dateConsentement = now,
                versionPolitique = request.versionPolitique,
            ),
            ConsentRow(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = ConsentType.PUBLICITE,
                accepte = request.publicite,
                dateConsentement = now,
                versionPolitique = request.versionPolitique,
            ),
            ConsentRow(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = ConsentType.AMELIORATION_SERVICE,
                accepte = request.ameliorationService,
                dateConsentement = now,
                versionPolitique = request.versionPolitique,
            ),
        )

        for (consent in consents) {
            consentDao.upsert(consent)
        }

        logger.info("InitialConsents: userId=$userId, analytics=${request.analytics}, publicite=${request.publicite}, amelioration=${request.ameliorationService}")

        return ConsentListResponse(
            data = consents.map { it.toResponse() },
        )
    }

    private fun ConsentRow.toResponse() = ConsentResponse(
        type = type.name,
        accepte = accepte,
        dateConsentement = dateConsentement.toString(),
        versionPolitique = versionPolitique,
    )

    private fun parseConsentType(typeStr: String): ConsentType {
        return try {
            enumValueOf<ConsentType>(typeStr)
        } catch (e: IllegalArgumentException) {
            throw ValidationException(
                "Valeur invalide pour 'type': '$typeStr'. Valeurs acceptees: ${ConsentType.entries.joinToString()}",
            )
        }
    }
}
