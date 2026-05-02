package com.appfood.backend.service

import com.appfood.backend.database.dao.UserDao
import com.appfood.backend.database.dao.UserRow
import com.appfood.backend.database.tables.SocialVisibility
import com.appfood.backend.plugins.ConflictException
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.slf4j.LoggerFactory

/**
 * TACHE-600 : gestion du profil social (handle, bio, date de naissance, visibilite).
 *
 * Regles :
 * - Handle : 3..30 chars, [a-zA-Z0-9_], unique case-insensitive, normalise en lowercase.
 * - Bio : max 280 chars, trimmed.
 * - DateNaissance : >= 13 ans pour utiliser l'app. Immuable une fois posee.
 * - SocialEnabled : age >= 16 ans, calcule a la lecture.
 */
class SocialProfileService(
    private val userDao: UserDao,
) {
    private val logger = LoggerFactory.getLogger("SocialProfileService")

    suspend fun isHandleAvailable(handle: String, currentUserId: String? = null): Boolean {
        validateHandleFormat(handle)
        return userDao.isHandleAvailable(handle, excludeUserId = currentUserId)
    }

    suspend fun updateSocial(
        userId: String,
        handle: String,
        bio: String?,
        dateNaissanceIso: String?,
        socialVisibilityStr: String,
    ): UserRow {
        val user =
            userDao.findById(userId)
                ?: throw NotFoundException("Utilisateur non trouve")

        // Handle
        validateHandleFormat(handle)
        val normalizedHandle = handle.lowercase()
        if (!userDao.isHandleAvailable(normalizedHandle, excludeUserId = userId)) {
            throw ConflictException("Ce handle est deja pris")
        }

        // Bio
        val cleanBio = bio?.trim()?.takeIf { it.isNotEmpty() }
        if (cleanBio != null && cleanBio.length > 280) {
            throw ValidationException("La bio doit faire moins de 280 caracteres")
        }

        // DateNaissance : immuable une fois posee
        val effectiveDateNaissance: LocalDate? =
            if (user.dateNaissance != null) {
                // Deja posee : on ignore le champ envoye, on ne le change jamais
                null // signal au DAO de ne pas updater le champ
            } else {
                if (dateNaissanceIso.isNullOrBlank()) {
                    throw ValidationException("La date de naissance est obligatoire")
                }
                val parsed =
                    runCatching { LocalDate.parse(dateNaissanceIso) }
                        .getOrElse { throw ValidationException("Date de naissance invalide (format attendu YYYY-MM-DD)") }
                if (computeAge(parsed) < 13) {
                    throw ValidationException("Age minimum requis : 13 ans")
                }
                parsed
            }

        // Visibility
        val visibility =
            runCatching { SocialVisibility.valueOf(socialVisibilityStr) }
                .getOrElse { throw ValidationException("Visibilite invalide") }

        userDao.updateSocial(
            id = userId,
            handle = normalizedHandle,
            bio = cleanBio,
            dateNaissance = effectiveDateNaissance,
            socialVisibility = visibility,
        )

        logger.info("UpdateSocial: handle=$normalizedHandle visibility=$visibility for userId=$userId")
        return userDao.findById(userId) ?: error("User disparu apres update")
    }

    private fun validateHandleFormat(handle: String) {
        if (!HANDLE_REGEX.matches(handle)) {
            throw ValidationException(
                "Handle invalide : 3-30 caracteres, lettres/chiffres/underscore uniquement",
            )
        }
    }

    companion object {
        private val HANDLE_REGEX = Regex("^[a-zA-Z0-9_]{3,30}$")

        /**
         * Calcule l'age en annees pleines a partir de la date de naissance.
         */
        fun computeAge(dateNaissance: LocalDate, today: LocalDate = todayUtc()): Int {
            var age = today.year - dateNaissance.year
            val anniversaireNonAtteint =
                today.monthNumber < dateNaissance.monthNumber ||
                    (today.monthNumber == dateNaissance.monthNumber && today.dayOfMonth < dateNaissance.dayOfMonth)
            if (anniversaireNonAtteint) age--
            return age
        }

        fun todayUtc(): LocalDate =
            Clock.System.now().toLocalDateTime(TimeZone.UTC).date

        fun isSocialEligible(dateNaissance: LocalDate?): Boolean {
            return dateNaissance != null && computeAge(dateNaissance) >= 16
        }
    }
}
