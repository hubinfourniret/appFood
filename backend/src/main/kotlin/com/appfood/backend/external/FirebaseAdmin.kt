package com.appfood.backend.external

import org.slf4j.LoggerFactory

/**
 * Firebase token verification.
 * In dev mode (FIREBASE_MOCK=true), accepts all tokens and extracts userId from the token string.
 * In production, should verify tokens via Firebase Admin SDK.
 *
 * --- Migration vers la verification reelle en production ---
 * Pour activer la verification reelle des tokens Firebase :
 * 1. Ajouter la dependance Firebase Admin SDK Java dans backend/build.gradle.kts :
 *    implementation("com.google.firebase:firebase-admin:9.4.3")
 * 2. Placer le fichier de credentials (service account JSON) sur le serveur
 * 3. Configurer la variable d'environnement GOOGLE_APPLICATION_CREDENTIALS
 *    pointant vers le chemin du fichier JSON
 * 4. Passer FIREBASE_MOCK=false dans les variables d'environnement de production
 * 5. Decommenter le code de verification reelle dans verifyToken() et deleteUser()
 */
data class FirebaseTokenInfo(
    val uid: String,
    val email: String?,
)

class FirebaseAdmin(
    private val mockEnabled: Boolean = System.getenv("FIREBASE_MOCK")?.toBoolean() ?: true,
) {
    private val logger = LoggerFactory.getLogger("FirebaseAdmin")

    /**
     * Verifies a Firebase ID token and returns the user info.
     * In mock mode, the token is treated as a JSON-like string: "mock-uid:email@example.com"
     * or simply used as the uid.
     */
    suspend fun verifyToken(idToken: String): FirebaseTokenInfo {
        if (mockEnabled) {
            logger.info("Firebase mock: verifying token (mock mode)")
            return parseMockToken(idToken)
        }

        // TODO: Implement real Firebase Admin SDK verification
        // val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
        // return FirebaseTokenInfo(uid = decodedToken.uid, email = decodedToken.email)
        logger.warn("Firebase verification not implemented, falling back to mock")
        return parseMockToken(idToken)
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

        // TODO: Implement real Firebase Admin SDK user deletion
        // FirebaseAuth.getInstance().deleteUser(uid)
        logger.warn("Firebase user deletion not implemented (mock mode)")
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
