package com.appfood.backend.external

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream

/**
 * Firebase token verification and user management.
 *
 * Modes :
 * - FIREBASE_MOCK=true (defaut) : accepte tous les tokens, extrait uid/email du token string.
 *   Utilise pour le dev local et les tests.
 * - FIREBASE_MOCK=false : verifie les tokens via Firebase Admin SDK.
 *   Necessite FIREBASE_CREDENTIALS (contenu JSON du service account) ou
 *   GOOGLE_APPLICATION_CREDENTIALS (chemin vers le fichier JSON).
 */
data class FirebaseTokenInfo(
    val uid: String,
    val email: String?,
)

class FirebaseAdmin(
    private val mockEnabled: Boolean = System.getenv("FIREBASE_MOCK")?.toBoolean() ?: true,
) {
    private val logger = LoggerFactory.getLogger("FirebaseAdmin")

    init {
        if (!mockEnabled) {
            initializeFirebaseApp()
            logger.info("Firebase Admin SDK initialise avec succes")
        } else {
            logger.info("Firebase Admin en mode mock (FIREBASE_MOCK=true)")
        }
    }

    private fun initializeFirebaseApp() {
        // Eviter la double initialisation
        try {
            FirebaseApp.getInstance()
            logger.info("FirebaseApp deja initialise, reutilisation de l'instance existante")
            return
        } catch (_: IllegalStateException) {
            // Pas encore initialise, on continue
        }

        val credentials = loadCredentials()
        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()
        FirebaseApp.initializeApp(options)
    }

    private fun loadCredentials(): GoogleCredentials {
        // 1) Essayer FIREBASE_CREDENTIALS (contenu JSON inline)
        val credentialsJson = System.getenv("FIREBASE_CREDENTIALS")
        if (!credentialsJson.isNullOrBlank() && credentialsJson.trimStart().startsWith("{")) {
            logger.info("Chargement des credentials Firebase depuis FIREBASE_CREDENTIALS (JSON inline)")
            return GoogleCredentials.fromStream(ByteArrayInputStream(credentialsJson.toByteArray()))
        }

        // 2) Essayer GOOGLE_APPLICATION_CREDENTIALS (chemin fichier — methode standard Google)
        val credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if (!credentialsPath.isNullOrBlank()) {
            logger.info("Chargement des credentials Firebase depuis GOOGLE_APPLICATION_CREDENTIALS: $credentialsPath")
            return GoogleCredentials.getApplicationDefault()
        }

        // 3) Aucune credentials configuree → fail-fast
        throw IllegalStateException(
            "Firebase Admin SDK : credentials non configurees. " +
                "Definissez FIREBASE_CREDENTIALS (contenu JSON du service account) ou " +
                "GOOGLE_APPLICATION_CREDENTIALS (chemin vers le fichier JSON), " +
                "ou bien activez le mode mock avec FIREBASE_MOCK=true.",
        )
    }

    /**
     * Verifies a Firebase ID token and returns the user info.
     * In mock mode, the token is treated as "uid:email" or simply "uid".
     */
    suspend fun verifyToken(idToken: String): FirebaseTokenInfo {
        if (mockEnabled) {
            logger.info("Firebase mock: verifying token (mock mode)")
            return parseMockToken(idToken)
        }

        return try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            FirebaseTokenInfo(uid = decodedToken.uid, email = decodedToken.email)
        } catch (e: FirebaseAuthException) {
            logger.warn("Firebase token verification failed: ${e.message}")
            throw IllegalArgumentException("Token Firebase invalide ou expire: ${e.message}", e)
        }
    }

    /**
     * Deletes a Firebase user account.
     * Used for RGPD account deletion.
     */
    suspend fun deleteUser(uid: String) {
        if (mockEnabled) {
            logger.info("Firebase mock: deleting user $uid (mock mode)")
            return
        }

        try {
            FirebaseAuth.getInstance().deleteUser(uid)
            logger.info("Firebase user deleted: $uid")
        } catch (e: FirebaseAuthException) {
            logger.error("Failed to delete Firebase user $uid: ${e.message}")
            throw IllegalStateException("Erreur lors de la suppression du compte Firebase: ${e.message}", e)
        }
    }

    private fun parseMockToken(token: String): FirebaseTokenInfo {
        // Support format "uid:email" or just "uid"
        val parts = token.split(":", limit = 2)
        return if (parts.size == 2) {
            FirebaseTokenInfo(uid = parts[0], email = parts[1])
        } else {
            FirebaseTokenInfo(uid = token, email = null)
        }
    }
}
