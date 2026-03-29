package com.appfood.backend.service

import com.appfood.backend.database.dao.JournalEntryDao
import com.appfood.backend.database.dao.QuotaDao
import com.appfood.backend.database.dao.QuotaRow
import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.database.tables.NiveauActivite
import com.appfood.backend.database.tables.NutrimentType
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.Sexe
import com.appfood.backend.plugins.NotFoundException
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.security.toEnumOrThrow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.roundToInt

class QuotaService(
    private val quotaDao: QuotaDao,
    private val userProfileDao: UserProfileDao,
    private val journalEntryDao: JournalEntryDao,
) {
    private val logger = LoggerFactory.getLogger("QuotaService")

    suspend fun getAllQuotas(userId: String): List<QuotaRow> {
        return quotaDao.findByUserId(userId)
    }

    suspend fun getQuotaStatus(userId: String, date: LocalDate): List<QuotaStatusResult> {
        val quotas = quotaDao.findByUserId(userId)
        if (quotas.isEmpty()) {
            throw NotFoundException("Aucun quota trouve. Utilisez POST /quotas/recalculate pour les generer.")
        }

        val entries = journalEntryDao.findByUserAndDate(userId, date)
        val consumed = sumConsumedNutrients(entries)

        return quotas.map { quota ->
            val valeurConsommee = getConsumedValue(consumed, quota.nutriment)
            val pourcentage = if (quota.valeurCible > 0.0) {
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

    suspend fun updateQuota(userId: String, nutrimentStr: String, valeurCible: Double): QuotaRow {
        val nutriment = nutrimentStr.toEnumOrThrow<NutrimentType>("nutriment")

        if (valeurCible <= 0.0) {
            throw ValidationException("valeurCible doit etre > 0")
        }

        val existing = quotaDao.findByUserAndNutriment(userId, nutriment)
            ?: throw NotFoundException("Quota non trouve pour $nutrimentStr. Utilisez POST /quotas/recalculate d'abord.")

        val updated = existing.copy(
            valeurCible = valeurCible,
            estPersonnalise = true,
            updatedAt = Clock.System.now(),
        )
        quotaDao.upsert(updated)
        logger.info("UpdateQuota: nutriment=$nutrimentStr, valeur=$valeurCible, userId=$userId")
        return quotaDao.findByUserAndNutriment(userId, nutriment)!!
    }

    suspend fun resetQuota(userId: String, nutrimentStr: String): QuotaRow {
        val nutriment = nutrimentStr.toEnumOrThrow<NutrimentType>("nutriment")

        val existing = quotaDao.findByUserAndNutriment(userId, nutriment)
            ?: throw NotFoundException("Quota non trouve pour $nutrimentStr")

        val reset = existing.copy(
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

        val reset = existing.map {
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
        val profile = userProfileDao.findByUserId(userId)
            ?: throw NotFoundException("Profil non trouve. Creez d'abord un profil via POST /users/me/profile.")

        val quotas = calculateQuotas(
            userId = userId,
            sexe = profile.sexe,
            age = profile.age,
            poidsKg = profile.poidsKg,
            tailleCm = profile.tailleCm,
            regimeAlimentaire = profile.regimeAlimentaire,
            niveauActivite = profile.niveauActivite,
        )

        // Preserve personalized quotas: if a quota was personalized, keep the user's valeurCible
        val existingQuotas = quotaDao.findByUserId(userId)
        val existingMap = existingQuotas.associateBy { it.nutriment }

        val finalQuotas = quotas.map { calculated ->
            val existing = existingMap[calculated.nutriment]
            if (existing != null && existing.estPersonnalise) {
                // Keep user's custom value but update the calculated reference
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

    // --- Full Mifflin-St Jeor algorithm ---

    private fun calculateQuotas(
        userId: String,
        sexe: Sexe,
        age: Int,
        poidsKg: Double,
        tailleCm: Int,
        regimeAlimentaire: RegimeAlimentaire,
        niveauActivite: NiveauActivite,
    ): List<QuotaRow> {
        val now = Clock.System.now()

        // 1. Metabolisme de base (Mifflin-St Jeor)
        val mb = when (sexe) {
            Sexe.HOMME -> (10.0 * poidsKg) + (6.25 * tailleCm) - (5.0 * age) + 5.0
            Sexe.FEMME -> (10.0 * poidsKg) + (6.25 * tailleCm) - (5.0 * age) - 161.0
        }

        // 2. Depense energetique totale
        val coeffActivite = when (niveauActivite) {
            NiveauActivite.SEDENTAIRE -> 1.2
            NiveauActivite.LEGER -> 1.375
            NiveauActivite.MODERE -> 1.55
            NiveauActivite.ACTIF -> 1.725
            NiveauActivite.TRES_ACTIF -> 1.9
        }
        val det = mb * coeffActivite

        // 3. Macronutrients derived from DET
        val calories = det
        var proteines = det * 0.15 / 4.0
        val glucides = det * 0.50 / 4.0
        val lipides = det * 0.35 / 9.0
        val fibres = if (age < 18) 25.0 else 30.0
        val sucres = det * 0.10 / 4.0
        val sel = 5.0

        // 4. Micronutrients base values (ANSES)
        val microBase = getMicroNutrimentsBase(sexe, age)

        // 5. Diet regime coefficients
        val coeffRegime = getCoefficientsRegime(regimeAlimentaire)

        // Apply coefficients
        val fer = microBase.fer * coeffRegime.fer
        val zinc = microBase.zinc * coeffRegime.zinc
        val omega3 = microBase.omega3 * coeffRegime.omega3
        val omega6 = microBase.omega6
        proteines *= coeffRegime.proteines
        val calcium = microBase.calcium
        val magnesium = microBase.magnesium
        val vitamineB12 = microBase.vitamineB12
        val vitamineD = microBase.vitamineD
        val vitamineC = microBase.vitamineC

        // 6. Age 65+ adjustment for proteins
        if (age >= 65) {
            proteines = max(proteines, poidsKg * 1.0)
        }

        // 7. Build quota list with rounding
        return listOf(
            buildQuota(userId, NutrimentType.CALORIES, roundCalories(calories), "kcal", now),
            buildQuota(userId, NutrimentType.PROTEINES, roundMacro(proteines), "g", now),
            buildQuota(userId, NutrimentType.GLUCIDES, roundMacro(glucides), "g", now),
            buildQuota(userId, NutrimentType.LIPIDES, roundMacro(lipides), "g", now),
            buildQuota(userId, NutrimentType.FIBRES, roundMacro(fibres), "g", now),
            buildQuota(userId, NutrimentType.SEL, roundMacro(sel), "g", now),
            buildQuota(userId, NutrimentType.SUCRES, roundMacro(sucres), "g", now),
            buildQuota(userId, NutrimentType.FER, roundMicro(fer), "mg", now),
            buildQuota(userId, NutrimentType.CALCIUM, roundMicro(calcium), "mg", now),
            buildQuota(userId, NutrimentType.ZINC, roundMicro(zinc), "mg", now),
            buildQuota(userId, NutrimentType.MAGNESIUM, roundMicro(magnesium), "mg", now),
            buildQuota(userId, NutrimentType.VITAMINE_B12, roundMicroDecimal(vitamineB12), "\u00b5g", now),
            buildQuota(userId, NutrimentType.VITAMINE_D, roundMicroDecimal(vitamineD), "\u00b5g", now),
            buildQuota(userId, NutrimentType.VITAMINE_C, roundMicro(vitamineC), "mg", now),
            buildQuota(userId, NutrimentType.OMEGA_3, roundMacro(omega3), "g", now),
            buildQuota(userId, NutrimentType.OMEGA_6, roundMacro(omega6), "g", now),
        )
    }

    private fun buildQuota(
        userId: String,
        nutriment: NutrimentType,
        valeur: Double,
        unite: String,
        now: kotlinx.datetime.Instant,
    ): QuotaRow = QuotaRow(
        userId = userId,
        nutriment = nutriment,
        valeurCible = valeur,
        estPersonnalise = false,
        valeurCalculee = valeur,
        unite = unite,
        updatedAt = now,
    )

    // Rounding helpers: calories = integer, macros = 1 decimal, micros = integer, B12/D = 1 decimal
    private fun roundCalories(v: Double): Double = v.roundToInt().toDouble()
    private fun roundMacro(v: Double): Double = (v * 10).roundToInt() / 10.0
    private fun roundMicro(v: Double): Double = v.roundToInt().toDouble()
    private fun roundMicroDecimal(v: Double): Double = (v * 10).roundToInt() / 10.0

    /**
     * ANSES reference values by sex and age.
     */
    private fun getMicroNutrimentsBase(sexe: Sexe, age: Int): MicroBase {
        val base = when (sexe) {
            Sexe.HOMME -> MicroBase(
                fer = 11.0,
                calcium = 950.0,
                zinc = 11.0,
                magnesium = 380.0,
                vitamineB12 = 4.0,
                vitamineD = 15.0,
                vitamineC = 110.0,
                omega3 = 2.5,
                omega6 = 10.0,
            )
            Sexe.FEMME -> MicroBase(
                fer = 16.0,
                calcium = 950.0,
                zinc = 8.0,
                magnesium = 300.0,
                vitamineB12 = 4.0,
                vitamineD = 15.0,
                vitamineC = 110.0,
                omega3 = 2.0,
                omega6 = 8.0,
            )
        }

        // Age-based adjustments
        return when {
            age in 14..17 -> base.copy(
                calcium = 1000.0,
                fer = if (sexe == Sexe.HOMME) 13.0 else base.fer,
                zinc = if (sexe == Sexe.HOMME) 13.0 else base.zinc,
            )
            age >= 65 -> base.copy(
                vitamineD = 20.0,
                calcium = 1200.0,
            )
            else -> base
        }
    }

    /**
     * Diet regime adjustment coefficients per the specification.
     */
    private fun getCoefficientsRegime(regime: RegimeAlimentaire): RegimeCoefficients {
        return when (regime) {
            RegimeAlimentaire.VEGAN -> RegimeCoefficients(
                fer = 1.8,
                zinc = 1.5,
                omega3 = 1.5,
                proteines = 1.1,
            )
            RegimeAlimentaire.VEGETARIEN -> RegimeCoefficients(
                fer = 1.5,
                zinc = 1.3,
                omega3 = 1.2,
                proteines = 1.0,
            )
            RegimeAlimentaire.FLEXITARIEN -> RegimeCoefficients()
            RegimeAlimentaire.OMNIVORE -> RegimeCoefficients()
        }
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

    private fun getConsumedValue(consumed: NutrientSums, nutriment: NutrimentType): Double {
        return when (nutriment) {
            NutrimentType.CALORIES -> consumed.calories
            NutrimentType.PROTEINES -> consumed.proteines
            NutrimentType.GLUCIDES -> consumed.glucides
            NutrimentType.LIPIDES -> consumed.lipides
            NutrimentType.FIBRES -> consumed.fibres
            NutrimentType.SEL -> consumed.sel
            NutrimentType.SUCRES -> consumed.sucres
            NutrimentType.FER -> consumed.fer
            NutrimentType.CALCIUM -> consumed.calcium
            NutrimentType.ZINC -> consumed.zinc
            NutrimentType.MAGNESIUM -> consumed.magnesium
            NutrimentType.VITAMINE_B12 -> consumed.vitamineB12
            NutrimentType.VITAMINE_D -> consumed.vitamineD
            NutrimentType.VITAMINE_C -> consumed.vitamineC
            NutrimentType.OMEGA_3 -> consumed.omega3
            NutrimentType.OMEGA_6 -> consumed.omega6
        }
    }
}

data class QuotaStatusResult(
    val nutriment: NutrimentType,
    val valeurCible: Double,
    val valeurConsommee: Double,
    val pourcentage: Double,
    val unite: String,
)

private data class MicroBase(
    val fer: Double,
    val calcium: Double,
    val zinc: Double,
    val magnesium: Double,
    val vitamineB12: Double,
    val vitamineD: Double,
    val vitamineC: Double,
    val omega3: Double,
    val omega6: Double,
)

private data class RegimeCoefficients(
    val fer: Double = 1.0,
    val zinc: Double = 1.0,
    val omega3: Double = 1.0,
    val proteines: Double = 1.0,
)
