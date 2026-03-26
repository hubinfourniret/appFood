package com.appfood.backend.service

import com.appfood.backend.database.dao.ConsentDao
import com.appfood.backend.database.dao.FcmTokenDao
import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.NotificationDao
import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.database.dao.PortionDao
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.UserDao
import com.appfood.backend.database.dao.UserPreferencesDao
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.dao.UserRow
import com.appfood.backend.external.FirebaseAdmin
import com.appfood.backend.plugins.ConflictException
import com.appfood.backend.plugins.UnauthorizedException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.slf4j.LoggerFactory
import java.util.Date

data class LoginResult(
    val user: UserRow,
    val token: String,
    val onboardingComplete: Boolean,
)

data class RegisterResult(
    val user: UserRow,
    val token: String,
)

class AuthService(
    private val firebaseAdmin: FirebaseAdmin,
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val journalEntryDao: JournalEntryDao,
    private val quotaDao: QuotaDao,
    private val poidsHistoryDao: PoidsHistoryDao,
    private val hydratationDao: HydratationDao,
    private val consentDao: ConsentDao,
    private val fcmTokenDao: FcmTokenDao,
    private val notificationDao: NotificationDao,
    private val portionDao: PortionDao,
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String,
) {
    private val logger = LoggerFactory.getLogger("AuthService")

    companion object {
        private const val JWT_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    /**
     * Generates a custom HMAC256 JWT token with subject = userId.
     * This token is verified by Auth.kt middleware on protected routes.
     */
    fun generateJwt(userId: String): String {
        return JWT.create()
            .withSubject(userId)
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withExpiresAt(Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    suspend fun register(
        firebaseToken: String,
        email: String,
        nom: String?,
        prenom: String?,
    ): RegisterResult {
        // Verify Firebase token
        val tokenInfo = firebaseAdmin.verifyToken(firebaseToken)
        logger.info("Register: verified Firebase token for uid=${tokenInfo.uid}")

        // Check if email already exists
        val existingUser = userDao.findByEmail(email)
        if (existingUser != null) {
            throw ConflictException("Un compte avec cet email existe deja")
        }

        // Check if user with this Firebase UID already exists
        val existingById = userDao.findById(tokenInfo.uid)
        if (existingById != null) {
            throw ConflictException("Un compte avec cet identifiant existe deja")
        }

        // Create user in PostgreSQL
        val user = userDao.insert(
            id = tokenInfo.uid,
            email = email,
            nom = nom,
            prenom = prenom,
        )
        logger.info("Register: created user id=${user.id}, email=${user.email}")

        val token = generateJwt(user.id)
        return RegisterResult(user = user, token = token)
    }

    suspend fun login(firebaseToken: String): LoginResult {
        // Verify Firebase token
        val tokenInfo = firebaseAdmin.verifyToken(firebaseToken)
        logger.info("Login: verified Firebase token for uid=${tokenInfo.uid}")

        // Find user in PostgreSQL
        val user = userDao.findById(tokenInfo.uid)
            ?: throw UnauthorizedException("Utilisateur non trouve. Veuillez vous inscrire.")

        // Recuperer le profil pour onboardingComplete
        val profile = userProfileDao.findByUserId(user.id)

        logger.info("Login: user found id=${user.id}")

        val token = generateJwt(user.id)
        return LoginResult(
            user = user,
            token = token,
            onboardingComplete = profile?.onboardingComplete ?: false,
        )
    }

    /**
     * Deletes all user data (RGPD right to erasure).
     * Order matters: delete dependent tables first, then the user.
     */
    suspend fun deleteAccount(userId: String) {
        logger.info("DeleteAccount: starting RGPD deletion for userId=$userId")

        // Verify user exists
        userDao.findById(userId)
            ?: throw UnauthorizedException("Utilisateur non trouve")

        // Delete all dependent data
        notificationDao.deleteByUserId(userId)
        fcmTokenDao.deleteByUserId(userId)
        consentDao.deleteByUserId(userId)
        hydratationDao.deleteByUserId(userId)
        poidsHistoryDao.deleteByUserId(userId)
        portionDao.deleteByUserId(userId)
        quotaDao.deleteByUserId(userId)
        journalEntryDao.deleteByUserId(userId)
        userPreferencesDao.delete(userId)
        userProfileDao.delete(userId)

        // Delete the user
        userDao.delete(userId)

        // Delete Firebase account
        try {
            firebaseAdmin.deleteUser(userId)
        } catch (e: Exception) {
            logger.error("DeleteAccount: failed to delete Firebase user $userId", e)
            // Don't fail the request — local data is already deleted
        }

        logger.info("DeleteAccount: completed RGPD deletion for userId=$userId")
    }
}
