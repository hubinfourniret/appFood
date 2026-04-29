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
     * Priority order:
     * 1. Portions linked to this specific alimentId
     * 2. Portions matching the aliment name (ex: "orange" → "Une orange (~200g)")
     * 3. User's custom portions
     * 4. Generic portions (cuillere, bol, etc.) — only if no specific/name-matched portions found
     *
     * @param alimentId aliment ID (optional)
     * @param alimentNom aliment name for keyword matching (optional)
     */
    suspend fun listPortions(
        alimentId: String?,
        userId: String,
        alimentNom: String? = null,
    ): PortionListResponse {
        val seenIds = mutableSetOf<String>()
        val result = mutableListOf<PortionRow>()

        // 1. Portions specifiques a l'aliment
        if (alimentId != null) {
            val specific = portionDao.findByAlimentId(alimentId, userId)
                .filter { it.alimentId == alimentId }
            specific.forEach { if (seenIds.add(it.id)) result.add(it) }
        }

        // 2. Portions matchees par nom
        if (alimentNom != null) {
            val keywords = extractKeywords(alimentNom)
            if (keywords.isNotEmpty()) {
                val matched = portionDao.findByNameKeywords(keywords)
                matched.forEach { if (seenIds.add(it.id)) result.add(it) }
            }
        }

        // 3. Portions personnalisees de l'utilisateur
        val personnalisees = portionDao.findByUserId(userId)
        personnalisees.forEach { if (seenIds.add(it.id)) result.add(it) }

        // 4. Portions generiques (seulement si aucune portion specifique/matchee)
        if (result.isEmpty() || result.all { it.estPersonnalise }) {
            val generiques = portionDao.findGeneriques()
            generiques.forEach { if (seenIds.add(it.id)) result.add(it) }
        }

        val responses = result.map { it.toPortionResponse() }
        return PortionListResponse(
            data = responses,
            total = responses.size,
        )
    }

    /**
     * Extrait les mots-cles significatifs du nom d'un aliment.
     * Ignore les mots courts et les mots generiques.
     */
    private fun extractKeywords(nom: String): List<String> {
        val stopWords = setOf("de", "du", "la", "le", "les", "des", "un", "une", "au", "aux", "en", "et", "ou", "avec", "sans", "pour", "sur", "cru", "crue", "crus", "crues", "cuit", "cuite", "cuits", "cuites", "frais", "fraiche", "sec", "seche", "entier", "entiere", "pulpe", "jus", "pur")
        return nom.lowercase()
            .replace(",", " ")
            .replace("(", " ")
            .replace(")", " ")
            .split(" ")
            .map { it.trim() }
            .filter { it.length >= 3 && it !in stopWords }
            .take(3)
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
