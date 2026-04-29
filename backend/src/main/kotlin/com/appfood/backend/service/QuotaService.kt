package com.appfood.backend.service

import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.QuotaRow
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.dao.getByType
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.security.toEnumOrThrow
import com.appfood.shared.domain.quota.CalculerQuotasUseCase
import com.appfood.shared.model.UserProfile
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory

class QuotaService(
    private val quotaDao: QuotaDao,
    private val userProfileDao: UserProfileDao,
    private val journalEntryDao: JournalEntryDao,
) {
    private val logger = LoggerFactory.getLogger("QuotaService")
    private val calculerQuotasUseCase = CalculerQuotasUseCase()

    suspend fun getAllQuotas(userId: String): List<QuotaRow> {
        return quotaDao.findByUserId(userId)
    }

    suspend fun getQuotaStatus(
        userId: String,
        date: LocalDate,
    ): List<QuotaStatusResult> {
        val quotas = quotaDao.findByUserId(userId)
        if (quotas.isEmpty()) {
            throw NotFoundException("Aucun quota trouve. Utilisez POST /quotas/recalculate pour les generer.")
        }

        val entries = journalEntryDao.findByUserAndDate(userId, date)
        val consumed = sumConsumedNutrients(entries)

        return quotas.map { quota ->
            val valeurConsommee = getConsumedValue(consumed, quota.nutriment)
            val pourcentage =
                if (quota.valeurCible > 0.0) {
                    valeurConsommee / quota.valeurCible * 100.0
                } else {
                    0.0
                }
            QuotaStatusResult(
                nutriment = quota.nutriment,
                valeurCible = quota.valeurCible,
                valeurConsommee = valeurConsommee,
                pourcentage = pourcentage,
                unite = quota.unite,
            )
        }
    }

    suspend fun updateQuota(
        userId: String,
        nutrimentStr: String,
        valeurCible: Double,
    ): QuotaRow {
        val nutriment = nutrimentStr.toEnumOrThrow<NutrimentType>("nutriment")

        if (valeurCible <= 0.0) {
            throw ValidationException("valeurCible doit etre > 0")
        }

        val existing =
            quotaDao.findByUserAndNutriment(userId, nutriment)
                ?: throw NotFoundException("Quota non trouve pour $nutrimentStr. Utilisez POST /quotas/recalculate d'abord.")

        val updated =
            existing.copy(
                valeurCible = valeurCible,
                estPersonnalise = true,
                updatedAt = Clock.System.now(),
            )
        quotaDao.upsert(updated)
        logger.info("UpdateQuota: nutriment=$nutrimentStr, valeur=$valeurCible, userId=$userId")
        return quotaDao.findByUserAndNutriment(userId, nutriment)!!
    }

    suspend fun resetQuota(
        userId: String,
        nutrimentStr: String,
    ): QuotaRow {
        val nutriment = nutrimentStr.toEnumOrThrow<NutrimentType>("nutriment")

        val existing =
            quotaDao.findByUserAndNutriment(userId, nutriment)
                ?: throw NotFoundException("Quota non trouve pour $nutrimentStr")

        val reset =
            existing.copy(
                valeurCible = existing.valeurCalculee,
                estPersonnalise = false,
                updatedAt = Clock.System.now(),
            )
        quotaDao.upsert(reset)
        logger.info("ResetQuota: nutriment=$nutrimentStr, userId=$userId")
        return quotaDao.findByUserAndNutriment(userId, nutriment)!!
    }

    suspend fun resetAllQuotas(userId: String): List<QuotaRow> {
        val existing = quotaDao.findByUserId(userId)
        if (existing.isEmpty()) {
            throw NotFoundException("Aucun quota a reinitialiser")
        }

        val reset =
            existing.map {
                it.copy(
                    valeurCible = it.valeurCalculee,
                    estPersonnalise = false,
                    updatedAt = Clock.System.now(),
                )
            }
        quotaDao.upsertAll(reset)
        logger.info("ResetAllQuotas: userId=$userId")
        return quotaDao.findByUserId(userId)
    }

    suspend fun recalculateQuotas(userId: String): List<QuotaRow> {
        val profile =
            userProfileDao.findByUserId(userId)
                ?: throw NotFoundException("Profil non trouve. Creez d'abord un profil via POST /users/me/profile.")

        // Delegate to the shared CalculerQuotasUseCase (single source of truth for formulas)
        // Plus besoin de conversion : les enums backend sont des typealias vers shared
        val sharedProfile =
            UserProfile(
                userId = userId,
                sexe = profile.sexe,
                age = profile.age,
                poidsKg = profile.poidsKg,
                tailleCm = profile.tailleCm,
                regimeAlimentaire = profile.regimeAlimentaire,
                niveauActivite = profile.niveauActivite,
                onboardingComplete = true,
                objectifPoids = null,
                updatedAt = kotlin.time.Clock.System.now(),
            )

        val sharedQuotas = calculerQuotasUseCase.calculerQuotas(sharedProfile)
        val now = Clock.System.now()
        val quotas =
            sharedQuotas.map { q ->
                QuotaRow(
                    userId = userId,
                    nutriment = q.nutriment,
                    valeurCible = q.valeurCible,
                    estPersonnalise = false,
                    valeurCalculee = q.valeurCalculee,
                    unite = q.unite,
                    updatedAt = now,
                )
            }

        // Preserve personalized quotas: if a quota was personalized, keep the user's valeurCible
        val existingQuotas = quotaDao.findByUserId(userId)
        val existingMap = existingQuotas.associateBy { it.nutriment }

        val finalQuotas =
            quotas.map { calculated ->
                val existing = existingMap[calculated.nutriment]
                if (existing != null && existing.estPersonnalise) {
                    calculated.copy(
                        valeurCible = existing.valeurCible,
                        estPersonnalise = true,
                    )
                } else {
                    calculated
                }
            }

        quotaDao.upsertAll(finalQuotas)
        logger.info("RecalculateQuotas: userId=$userId, sexe=${profile.sexe}, age=${profile.age}")
        return quotaDao.findByUserId(userId)
    }

    private fun sumConsumedNutrients(entries: List<com.appfood.backend.database.dao.JournalEntryRow>): NutrientSums {
        return entries.fold(NutrientSums()) { acc, e ->
            NutrientSums(
                calories = acc.calories + e.calories,
                proteines = acc.proteines + e.proteines,
                glucides = acc.glucides + e.glucides,
                lipides = acc.lipides + e.lipides,
                fibres = acc.fibres + e.fibres,
                sel = acc.sel + e.sel,
                sucres = acc.sucres + e.sucres,
                fer = acc.fer + e.fer,
                calcium = acc.calcium + e.calcium,
                zinc = acc.zinc + e.zinc,
                magnesium = acc.magnesium + e.magnesium,
                vitamineB12 = acc.vitamineB12 + e.vitamineB12,
                vitamineD = acc.vitamineD + e.vitamineD,
                vitamineC = acc.vitamineC + e.vitamineC,
                omega3 = acc.omega3 + e.omega3,
                omega6 = acc.omega6 + e.omega6,
            )
        }
    }

    private fun getConsumedValue(consumed: NutrientSums, nutriment: NutrimentType) = consumed.getByType(nutriment)
}

data class QuotaStatusResult(
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,
    val unite: String,
)
