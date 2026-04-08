package com.appfood.backend.service

import com.appfood.backend.database.dao.ConsentDao
import com.appfood.backend.database.dao.ConsentRow
import com.appfood.backend.database.dao.HydratationDao
import com.appfood.backend.database.dao.HydratationRow
import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.JournalEntryRow
import com.appfood.backend.database.dao.PoidsHistoryDao
import com.appfood.backend.database.dao.PoidsHistoryRow
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.QuotaRow
import com.appfood.backend.database.dao.UserDao
import com.appfood.backend.database.dao.UserPreferencesDao
import com.appfood.backend.database.dao.UserPreferencesRow
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.dao.UserProfileRow
import com.appfood.backend.database.dao.UserRow
import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.database.tables.ObjectifPoids
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.plugins.ConflictException
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.security.toEnumOrThrow
import org.slf4j.LoggerFactory

data class UserProfileData(
    val user: UserRow,
    val profile: UserProfileRow?,
    val preferences: UserPreferencesRow?,
)

data class ExportData(
    val userProfileData: UserProfileData,
    val journalEntries: List<JournalEntryRow>,
    val quotas: List<QuotaRow>,
    val poidsHistory: List<PoidsHistoryRow>,
    val hydratation: List<HydratationRow>,
    val consentements: List<ConsentRow>,
)

class ProfileService(
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val journalEntryDao: JournalEntryDao,
    private val quotaDao: QuotaDao,
    private val poidsHistoryDao: PoidsHistoryDao,
    private val hydratationDao: HydratationDao,
    private val consentDao: ConsentDao,
) {
    private val logger = LoggerFactory.getLogger("ProfileService")

    suspend fun getUserProfile(userId: String): UserProfileData {
        val user = userDao.findById(userId)
            ?: throw NotFoundException("Utilisateur non trouve")
        val profile = userProfileDao.findByUserId(userId)
        val preferences = userPreferencesDao.findByUserId(userId)
        return UserProfileData(user, profile, preferences)
    }

    suspend fun createProfile(
        userId: String,
        sexeStr: String,
        age: Int,
        poidsKg: Double,
        tailleCm: Int,
        regimeAlimentaireStr: String,
        niveauActiviteStr: String,
    ): UserProfileRow {
        // Validate user exists
        userDao.findById(userId)
            ?: throw NotFoundException("Utilisateur non trouve")

        // Check profile doesn't already exist
        val existing = userProfileDao.findByUserId(userId)
        if (existing != null) {
            throw ConflictException("Le profil existe deja. Utilisez PUT pour le mettre a jour.")
        }

        // Validate enums
        val sexe = sexeStr.toEnumOrThrow<Sexe>("sexe")
        val regimeAlimentaire = regimeAlimentaireStr.toEnumOrThrow<RegimeAlimentaire>("regimeAlimentaire")
        val niveauActivite = niveauActiviteStr.toEnumOrThrow<NiveauActivite>("niveauActivite")

        // Validate ranges
        validateAge(age)
        validatePoids(poidsKg)
        validateTaille(tailleCm)

        val profile = userProfileDao.insert(
            userId = userId,
            sexe = sexe,
            age = age,
            poidsKg = poidsKg,
            tailleCm = tailleCm,
            regimeAlimentaire = regimeAlimentaire,
            niveauActivite = niveauActivite,
        )

        logger.info("CreateProfile: profile created for userId=$userId")
        return profile
    }

    suspend fun updateProfile(
        userId: String,
        sexeStr: String?,
        age: Int?,
        poidsKg: Double?,
        tailleCm: Int?,
        regimeAlimentaireStr: String?,
        niveauActiviteStr: String?,
        objectifPoidsStr: String?,
    ): UserProfileRow {
        val existing = userProfileDao.findByUserId(userId)
            ?: throw NotFoundException("Profil non trouve. Creez d'abord un profil via POST.")

        // Validate provided fields
        val sexe = sexeStr?.toEnumOrThrow<Sexe>("sexe") ?: existing.sexe
        val regimeAlimentaire = regimeAlimentaireStr?.toEnumOrThrow<RegimeAlimentaire>("regimeAlimentaire")
            ?: existing.regimeAlimentaire
        val niveauActivite = niveauActiviteStr?.toEnumOrThrow<NiveauActivite>("niveauActivite")
            ?: existing.niveauActivite
        val objectifPoids = objectifPoidsStr?.toEnumOrThrow<ObjectifPoids>("objectifPoids")
            ?: existing.objectifPoids

        val newAge = age ?: existing.age
        val newPoids = poidsKg ?: existing.poidsKg
        val newTaille = tailleCm ?: existing.tailleCm

        // Validate ranges for provided fields
        if (age != null) validateAge(age)
        if (poidsKg != null) validatePoids(poidsKg)
        if (tailleCm != null) validateTaille(tailleCm)

        userProfileDao.update(
            userId = userId,
            sexe = sexe,
            age = newAge,
            poidsKg = newPoids,
            tailleCm = newTaille,
            regimeAlimentaire = regimeAlimentaire,
            niveauActivite = niveauActivite,
            onboardingComplete = true,
            objectifPoids = objectifPoids,
        )

        logger.info("UpdateProfile: profile updated for userId=$userId")
        return userProfileDao.findByUserId(userId)!!
    }

    suspend fun updatePreferences(
        userId: String,
        alimentsExclus: List<String>?,
        allergies: List<String>?,
        alimentsFavoris: List<String>?,
    ): UserPreferencesRow {
        // Validate user exists
        userDao.findById(userId)
            ?: throw NotFoundException("Utilisateur non trouve")

        val existing = userPreferencesDao.findByUserId(userId)

        if (existing == null) {
            // Create new preferences
            val row = userPreferencesDao.insert(
                userId = userId,
                alimentsExclus = serializeList(alimentsExclus ?: emptyList()),
                allergies = serializeList(allergies ?: emptyList()),
                alimentsFavoris = serializeList(alimentsFavoris ?: emptyList()),
            )
            logger.info("UpdatePreferences: preferences created for userId=$userId")
            return row
        } else {
            // Update existing — merge with patch semantics
            val newAlimentsExclus = alimentsExclus?.let { serializeList(it) } ?: existing.alimentsExclus
            val newAllergies = allergies?.let { serializeList(it) } ?: existing.allergies
            val newAlimentsFavoris = alimentsFavoris?.let { serializeList(it) } ?: existing.alimentsFavoris

            userPreferencesDao.update(
                userId = userId,
                alimentsExclus = newAlimentsExclus,
                allergies = newAllergies,
                alimentsFavoris = newAlimentsFavoris,
            )
            logger.info("UpdatePreferences: preferences updated for userId=$userId")
            return userPreferencesDao.findByUserId(userId)!!
        }
    }

    // --- Favoris helpers (JOURNAL-03) ---

    suspend fun getFavorisIds(userId: String): List<String> {
        val prefs = userPreferencesDao.findByUserId(userId) ?: return emptyList()
        return deserializeList(prefs.alimentsFavoris)
    }

    suspend fun addFavori(userId: String, alimentId: String) {
        val prefs = userPreferencesDao.findByUserId(userId)
        if (prefs == null) {
            userPreferencesDao.insert(
                userId = userId,
                alimentsFavoris = serializeList(listOf(alimentId)),
            )
        } else {
            val current = deserializeList(prefs.alimentsFavoris).toMutableList()
            if (alimentId !in current) {
                current.add(alimentId)
                userPreferencesDao.update(
                    userId = userId,
                    alimentsExclus = prefs.alimentsExclus,
                    allergies = prefs.allergies,
                    alimentsFavoris = serializeList(current),
                )
            }
        }
        logger.info("AddFavori: alimentId=$alimentId for userId=$userId")
    }

    suspend fun removeFavori(userId: String, alimentId: String) {
        val prefs = userPreferencesDao.findByUserId(userId) ?: return
        val current = deserializeList(prefs.alimentsFavoris).toMutableList()
        if (current.remove(alimentId)) {
            userPreferencesDao.update(
                userId = userId,
                alimentsExclus = prefs.alimentsExclus,
                allergies = prefs.allergies,
                alimentsFavoris = serializeList(current),
            )
        }
        logger.info("RemoveFavori: alimentId=$alimentId for userId=$userId")
    }

    suspend fun exportUserData(userId: String): ExportData {
        val userProfileData = getUserProfile(userId)
        val journalEntries = journalEntryDao.findByUserAll(userId)
        val quotas = quotaDao.findByUserId(userId)
        val poidsHistory = poidsHistoryDao.findByUserId(userId)
        val hydratation = hydratationDao.findByUserId(userId)
        val consentements = consentDao.findByUserId(userId)

        return ExportData(
            userProfileData = userProfileData,
            journalEntries = journalEntries,
            quotas = quotas,
            poidsHistory = poidsHistory,
            hydratation = hydratation,
            consentements = consentements,
        )
    }

    private fun validateAge(age: Int) {
        if (age < 1 || age > 120) {
            throw ValidationException("L'age doit etre entre 1 et 120 ans (recu: $age)")
        }
    }

    private fun validatePoids(poidsKg: Double) {
        if (poidsKg < 20.0 || poidsKg > 500.0) {
            throw ValidationException("Le poids doit etre entre 20 et 500 kg (recu: $poidsKg)")
        }
    }

    private fun validateTaille(tailleCm: Int) {
        if (tailleCm < 50 || tailleCm > 300) {
            throw ValidationException("La taille doit etre entre 50 et 300 cm (recu: $tailleCm)")
        }
    }

    companion object {
        private val json = kotlinx.serialization.json.Json

        fun serializeList(list: List<String>): String =
            json.encodeToString(list)

        fun deserializeList(jsonStr: String): List<String> =
            try {
                json.decodeFromString<List<String>>(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
    }
}
