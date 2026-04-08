package com.appfood.backend.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.slf4j.LoggerFactory

/**
 * Service de chiffrement AES-256-GCM pour les donnees sensibles en base.
 *
 * Si aucune cle n'est configuree (ENCRYPTION_KEY absente), le service fonctionne
 * en mode "clair" avec un warning — adapte au developpement local uniquement.
 */
class EncryptionService(encryptionKeyBase64: String?) {

    private val logger = LoggerFactory.getLogger("EncryptionService")

    private val secretKey: SecretKeySpec?
    val isEnabled: Boolean

    init {
        if (encryptionKeyBase64.isNullOrBlank()) {
            logger.warn("ENCRYPTION_KEY non configuree — mode CLAIR actif. NE PAS utiliser en production.")
            secretKey = null
            isEnabled = false
        } else {
            val keyBytes = Base64.getDecoder().decode(encryptionKeyBase64)
            require(keyBytes.size == 32) {
                "ENCRYPTION_KEY doit etre exactement 32 bytes (256 bits) encode en Base64. " +
                    "Taille actuelle: ${keyBytes.size} bytes."
            }
            secretKey = SecretKeySpec(keyBytes, ALGORITHM)
            isEnabled = true
            logger.info("EncryptionService initialise — chiffrement AES-256-GCM actif")
        }
    }

    /**
     * Chiffre une chaine de caracteres.
     * Retourne une chaine Base64 contenant [IV (12 bytes) || ciphertext || auth tag].
     * En mode clair, retourne la valeur telle quelle.
     */
    fun encrypt(plaintext: String): String {
        if (!isEnabled || secretKey == null) return plaintext

        val iv = ByteArray(IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Concatenate IV + ciphertext (includes auth tag)
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Dechiffre une chaine Base64 produite par [encrypt].
     * En mode clair, retourne la valeur telle quelle.
     */
    fun decrypt(cipherBase64: String): String {
        if (!isEnabled || secretKey == null) return cipherBase64

        val combined = Base64.getDecoder().decode(cipherBase64)
        require(combined.size > IV_LENGTH_BYTES) { "Donnee chiffree invalide — trop courte" }

        val iv = combined.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))

        val plainBytes = cipher.doFinal(ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Chiffre un Double en le convertissant en String.
     */
    fun encryptDouble(value: Double): String = encrypt(value.toString())

    /**
     * Dechiffre un String vers un Double.
     */
    fun decryptDouble(cipherBase64: String): Double = decrypt(cipherBase64).toDouble()

    /**
     * Chiffre un Int en le convertissant en String.
     */
    fun encryptInt(value: Int): String = encrypt(value.toString())

    /**
     * Dechiffre un String vers un Int.
     */
    fun decryptInt(cipherBase64: String): Int = decrypt(cipherBase64).toInt()

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH_BYTES = 12
        private const val TAG_LENGTH_BITS = 128
    }
}
