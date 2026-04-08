package com.appfood.backend.service

import com.appfood.backend.database.dao.FcmTokenDao
import com.appfood.backend.database.dao.NotificationDao
import com.appfood.backend.database.dao.NotificationRow
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.routes.dto.NotificationListResponse
import com.appfood.backend.routes.dto.NotificationResponse
import com.appfood.backend.routes.dto.ReadAllResponse
import com.appfood.backend.routes.dto.RegisterFcmTokenRequest
import com.appfood.backend.routes.dto.RegisterTokenResponse
import org.slf4j.LoggerFactory
import java.util.UUID

class NotificationService(
    private val notificationDao: NotificationDao,
    private val fcmTokenDao: FcmTokenDao,
) {
    private val logger = LoggerFactory.getLogger("NotificationService")

    /**
     * Liste les notifications de l'utilisateur avec pagination et filtre optionnel.
     */
    suspend fun list(
        userId: String,
        page: Int,
        size: Int,
        nonLuesUniquement: Boolean,
    ): NotificationListResponse {
        val validPage = page.coerceAtLeast(1)
        val validSize = size.coerceIn(1, MAX_PAGE_SIZE)
        val offset = ((validPage - 1) * validSize).toLong()

        val rows = if (nonLuesUniquement) {
            notificationDao.findByUserIdUnreadOnly(userId, validSize, offset)
        } else {
            notificationDao.findByUserId(userId, validSize, offset)
        }

        val total = if (nonLuesUniquement) {
            notificationDao.countUnread(userId).toInt()
        } else {
            notificationDao.countByUserId(userId).toInt()
        }

        val nonLues = notificationDao.countUnread(userId).toInt()

        return NotificationListResponse(
            data = rows.map { it.toResponse() },
            total = total,
            nonLues = nonLues,
        )
    }

    /**
     * Marque une notification comme lue et la retourne.
     */
    suspend fun markAsRead(userId: String, notificationId: String): NotificationResponse {
        val updated = notificationDao.markAsRead(notificationId, userId)
        if (!updated) {
            throw NotFoundException("Notification non trouvee: $notificationId")
        }
        logger.info("MarkAsRead: userId=$userId, notificationId=$notificationId")

        val row = notificationDao.findById(notificationId, userId)
            ?: throw NotFoundException("Notification non trouvee: $notificationId")
        return row.toResponse()
    }

    /**
     * Marque toutes les notifications de l'utilisateur comme lues.
     */
    suspend fun markAllAsRead(userId: String): ReadAllResponse {
        val count = notificationDao.markAllAsRead(userId)
        logger.info("MarkAllAsRead: userId=$userId, count=$count")
        return ReadAllResponse(marqueesCommeLues = count)
    }

    /**
     * Enregistre un token FCM pour recevoir les notifications push.
     */
    suspend fun registerToken(userId: String, request: RegisterFcmTokenRequest): RegisterTokenResponse {
        if (request.token.isBlank()) {
            throw ValidationException("Le token FCM ne peut pas etre vide")
        }

        val validPlatforms = setOf("ANDROID", "IOS")
        if (request.platform.uppercase() !in validPlatforms) {
            throw ValidationException(
                "Plateforme invalide: '${request.platform}'. Valeurs acceptees: ${validPlatforms.joinToString()}",
            )
        }

        val id = UUID.randomUUID().toString()
        fcmTokenDao.upsert(
            id = id,
            userId = userId,
            token = request.token,
            platform = request.platform.uppercase(),
        )

        logger.info("RegisterFcmToken: userId=$userId, platform=${request.platform}")
        return RegisterTokenResponse(registered = true)
    }

    private fun NotificationRow.toResponse() = NotificationResponse(
        id = id,
        type = type.name,
        titre = titre,
        contenu = contenu,
        dateEnvoi = dateEnvoi.toString(),
        lue = lue,
    )

    companion object {
        private const val MAX_PAGE_SIZE = 100
    }
}
