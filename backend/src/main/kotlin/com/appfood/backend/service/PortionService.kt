package com.appfood.backend.service

import com.appfood.backend.database.dao.PortionDao
import com.appfood.backend.database.dao.PortionRow
import com.appfood.backend.plugins.ForbiddenException
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.PortionListResponse
import com.appfood.backend.routes.dto.PortionResponse
import com.appfood.backend.service.AlimentService.Companion.toPortionResponse
import org.slf4j.LoggerFactory
import java.util.UUID

class PortionService(
    private val portionDao: PortionDao,
) {
    private val logger = LoggerFactory.getLogger("PortionService")

    /**
     * List portions for an aliment.
     * Returns specific portions for the aliment + generic portions + user's custom portions.
     * If alimentId is null, returns only generic + user's custom portions.
     */
    suspend fun listPortions(
        alimentId: String?,
        userId: String,
    ): PortionListResponse {
        val portions =
            if (alimentId != null) {
                portionDao.findByAlimentId(alimentId, userId)
            } else {
                val generiques = portionDao.findGeneriques()
                val personnalisees = portionDao.findByUserId(userId)
                generiques + personnalisees
            }

        val responses = portions.map { it.toPortionResponse() }
        return PortionListResponse(
            data = responses,
            total = responses.size,
        )
    }

    /**
     * Create a custom portion for the user.
     */
    suspend fun createPortion(
        userId: String,
        alimentId: String?,
        nom: String,
        quantiteGrammes: Double,
    ): PortionResponse {
        if (nom.isBlank()) {
            throw ValidationException("Le nom de la portion ne peut pas etre vide")
        }
        if (quantiteGrammes <= 0) {
            throw ValidationException("La quantite en grammes doit etre superieure a 0")
        }

        val row =
            PortionRow(
                id = UUID.randomUUID().toString(),
                alimentId = alimentId,
                nom = nom,
                quantiteGrammes = quantiteGrammes,
                estGenerique = false,
                estPersonnalise = true,
                userId = userId,
            )

        val inserted = portionDao.insert(row)
        logger.info("Created custom portion id=${inserted.id} for userId=$userId")
        return inserted.toPortionResponse()
    }

    /**
     * Update a custom portion. Only the owner can modify their custom portions.
     */
    suspend fun updatePortion(
        portionId: String,
        userId: String,
        nom: String?,
        quantiteGrammes: Double?,
    ): PortionResponse {
        val existing =
            portionDao.findById(portionId)
                ?: throw NotFoundException("Portion non trouvee: $portionId")

        verifyOwnership(existing, userId)

        if (quantiteGrammes != null && quantiteGrammes <= 0) {
            throw ValidationException("La quantite en grammes doit etre superieure a 0")
        }
        if (nom != null && nom.isBlank()) {
            throw ValidationException("Le nom de la portion ne peut pas etre vide")
        }

        val updated =
            portionDao.update(portionId, nom, quantiteGrammes)
                ?: throw NotFoundException("Portion non trouvee apres mise a jour: $portionId")

        logger.info("Updated portion id=$portionId for userId=$userId")
        return updated.toPortionResponse()
    }

    /**
     * Delete a custom portion. Only the owner can delete their custom portions.
     */
    suspend fun deletePortion(
        portionId: String,
        userId: String,
    ) {
        val existing =
            portionDao.findById(portionId)
                ?: throw NotFoundException("Portion non trouvee: $portionId")

        verifyOwnership(existing, userId)

        portionDao.delete(portionId)
        logger.info("Deleted portion id=$portionId for userId=$userId")
    }

    /**
     * Verifies that the portion is owned by the user and is a custom portion.
     */
    private fun verifyOwnership(
        portion: PortionRow,
        userId: String,
    ) {
        if (!portion.estPersonnalise) {
            throw ForbiddenException("Seules les portions personnalisees peuvent etre modifiees")
        }
        if (portion.userId != userId) {
            throw ForbiddenException("Acces refuse a cette portion")
        }
    }
}
