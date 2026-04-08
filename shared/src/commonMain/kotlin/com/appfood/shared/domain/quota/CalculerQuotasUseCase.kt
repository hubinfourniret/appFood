package com.appfood.shared.domain.quota

import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.model.UserProfile
import com.appfood.shared.util.AppResult
import kotlin.time.Clock
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Calculates personalized daily quotas based on the user profile.
 * Uses the Mifflin-St Jeor formula for BMR, ANSES micronutrient references,
 * and dietary regime adjustments.
 *
 * Returns AppResult<List<QuotaJournalier>> — never throws.
 */
class CalculerQuotasUseCase {

    fun execute(profile: UserProfile): AppResult<List<QuotaJournalier>> {
        return try {
            val quotas = calculerQuotas(profile)
            AppResult.Success(quotas)
        } catch (e: Exception) {
            AppResult.Error(
                code = "QUOTA_CALCULATION_ERROR",
                message = "Failed to calculate quotas: ${e.message}",
                cause = e,
            )
        }
    }

    /**
     * Core calculation logic — pure function, no side effects.
     * Visible for testing.
     */
    fun calculerQuotas(profile: UserProfile): List<QuotaJournalier> {
        // 1. Basal Metabolic Rate (Mifflin-St Jeor)
        val mb = calculerMetabolismeBase(profile.sexe, profile.poidsKg, profile.tailleCm, profile.age)

        // 2. Total Daily Energy Expenditure
        val coeffActivite = getCoeffActivite(profile.niveauActivite)
        val det = mb * coeffActivite

        // 3. Macronutrients from DET
        val caloriesRaw = det
        var proteinesRaw = det * 0.15 / 4.0
        val glucidesRaw = det * 0.50 / 4.0
        val lipidesRaw = det * 0.35 / 9.0
        val fibresRaw = if (profile.age < 18) 25.0 else 30.0
        val sucresRaw = det * 0.10 / 4.0
        val selRaw = 5.0

        // 4. Base micronutrients (ANSES references)
        val microBase = getMicroNutrimentsBase(profile.sexe, profile.age)

        // 5. Dietary regime adjustments
        val coefficients = getCoefficientsRegime(profile.regimeAlimentaire)
        proteinesRaw *= (coefficients[NutrimentType.PROTEINES] ?: 1.0)

        val ferRaw = microBase.getValue(NutrimentType.FER) * (coefficients[NutrimentType.FER] ?: 1.0)
        val zincRaw = microBase.getValue(NutrimentType.ZINC) * (coefficients[NutrimentType.ZINC] ?: 1.0)
        val omega3Raw = microBase.getValue(NutrimentType.OMEGA_3) * (coefficients[NutrimentType.OMEGA_3] ?: 1.0)
        val calciumRaw = microBase.getValue(NutrimentType.CALCIUM)
        val magnesiumRaw = microBase.getValue(NutrimentType.MAGNESIUM)
        val vitamineB12Raw = microBase.getValue(NutrimentType.VITAMINE_B12)
        val vitamineDRaw = microBase.getValue(NutrimentType.VITAMINE_D)
        val vitamineCRaw = microBase.getValue(NutrimentType.VITAMINE_C)
        val omega6Raw = microBase.getValue(NutrimentType.OMEGA_6)

        // 6. Age 65+ protein adjustment: min 1g/kg body weight
        if (profile.age >= 65) {
            proteinesRaw = max(proteinesRaw, profile.poidsKg * 1.0)
        }

        // 7. Round and build quota list
        val now = Clock.System.now()
        val quotas = listOf(
            buildQuota(profile.userId, NutrimentType.CALORIES, arrondirCalories(caloriesRaw), "kcal", now),
            buildQuota(profile.userId, NutrimentType.PROTEINES, arrondirMacro(proteinesRaw), "g", now),
            buildQuota(profile.userId, NutrimentType.GLUCIDES, arrondirMacro(glucidesRaw), "g", now),
            buildQuota(profile.userId, NutrimentType.LIPIDES, arrondirMacro(lipidesRaw), "g", now),
            buildQuota(profile.userId, NutrimentType.FIBRES, arrondirMacro(fibresRaw), "g", now),
            buildQuota(profile.userId, NutrimentType.SUCRES, arrondirMacro(sucresRaw), "g", now),
            buildQuota(profile.userId, NutrimentType.SEL, arrondirMacro(selRaw), "g", now),
            buildQuota(profile.userId, NutrimentType.FER, arrondirMicro(ferRaw), "mg", now),
            buildQuota(profile.userId, NutrimentType.CALCIUM, arrondirMicro(calciumRaw), "mg", now),
            buildQuota(profile.userId, NutrimentType.ZINC, arrondirMicro(zincRaw), "mg", now),
            buildQuota(profile.userId, NutrimentType.MAGNESIUM, arrondirMicro(magnesiumRaw), "mg", now),
            buildQuota(profile.userId, NutrimentType.VITAMINE_B12, arrondirMicroDecimal(vitamineB12Raw), "\u00b5g", now),
            buildQuota(profile.userId, NutrimentType.VITAMINE_D, arrondirMicroDecimal(vitamineDRaw), "\u00b5g", now),
            buildQuota(profile.userId, NutrimentType.VITAMINE_C, arrondirMicro(vitamineCRaw), "mg", now),
            buildQuota(profile.userId, NutrimentType.OMEGA_3, arrondirMacro(omega3Raw), "g", now),
            buildQuota(profile.userId, NutrimentType.OMEGA_6, arrondirMacro(omega6Raw), "g", now),
        )
        return quotas
    }

    // --- Calculation helpers ---

    internal fun calculerMetabolismeBase(sexe: Sexe, poidsKg: Double, tailleCm: Int, age: Int): Double {
        return when (sexe) {
            Sexe.HOMME -> (10.0 * poidsKg) + (6.25 * tailleCm) - (5.0 * age) + 5.0
            Sexe.FEMME -> (10.0 * poidsKg) + (6.25 * tailleCm) - (5.0 * age) - 161.0
        }
    }

    internal fun getCoeffActivite(niveauActivite: NiveauActivite): Double {
        return when (niveauActivite) {
            NiveauActivite.SEDENTAIRE -> 1.2
            NiveauActivite.LEGER -> 1.375
            NiveauActivite.MODERE -> 1.55
            NiveauActivite.ACTIF -> 1.725
            NiveauActivite.TRES_ACTIF -> 1.9
        }
    }

    /**
     * Returns the base micronutrient values (ANSES) for the given sex and age.
     * Handles age brackets: 14-18, 19-64, 65+.
     * Age < 14 uses the 14-18 bracket values.
     */
    internal fun getMicroNutrimentsBase(sexe: Sexe, age: Int): Map<NutrimentType, Double> {
        // Start with adult 19-64 base values
        val base = when (sexe) {
            Sexe.HOMME -> mutableMapOf(
                NutrimentType.FER to 11.0,
                NutrimentType.CALCIUM to 950.0,
                NutrimentType.ZINC to 11.0,
                NutrimentType.MAGNESIUM to 380.0,
                NutrimentType.VITAMINE_B12 to 4.0,
                NutrimentType.VITAMINE_D to 15.0,
                NutrimentType.VITAMINE_C to 110.0,
                NutrimentType.OMEGA_3 to 2.5,
                NutrimentType.OMEGA_6 to 10.0,
            )
            Sexe.FEMME -> mutableMapOf(
                NutrimentType.FER to 16.0,
                NutrimentType.CALCIUM to 950.0,
                NutrimentType.ZINC to 8.0,
                NutrimentType.MAGNESIUM to 300.0,
                NutrimentType.VITAMINE_B12 to 4.0,
                NutrimentType.VITAMINE_D to 15.0,
                NutrimentType.VITAMINE_C to 110.0,
                NutrimentType.OMEGA_3 to 2.0,
                NutrimentType.OMEGA_6 to 8.0,
            )
        }

        // Age adjustments
        val effectiveAge = if (age < 14) 14 else age

        if (effectiveAge in 14..17) {
            base[NutrimentType.CALCIUM] = 1000.0
            if (sexe == Sexe.HOMME) {
                base[NutrimentType.FER] = 13.0
                base[NutrimentType.ZINC] = 13.0
            }
        } else if (effectiveAge >= 65) {
            base[NutrimentType.VITAMINE_D] = 20.0
            base[NutrimentType.CALCIUM] = 1200.0
        }

        return base
    }

    /**
     * Returns the adjustment coefficients for the given dietary regime.
     * Only nutriments that need adjustment (coefficient != 1.0) are returned.
     */
    internal fun getCoefficientsRegime(regime: RegimeAlimentaire): Map<NutrimentType, Double> {
        return when (regime) {
            RegimeAlimentaire.VEGAN -> mapOf(
                NutrimentType.FER to 1.8,
                NutrimentType.ZINC to 1.5,
                NutrimentType.OMEGA_3 to 1.5,
                NutrimentType.PROTEINES to 1.1,
            )
            RegimeAlimentaire.VEGETARIEN -> mapOf(
                NutrimentType.FER to 1.5,
                NutrimentType.ZINC to 1.3,
                NutrimentType.OMEGA_3 to 1.2,
            )
            RegimeAlimentaire.FLEXITARIEN,
            RegimeAlimentaire.OMNIVORE -> emptyMap()
        }
    }

    // --- Rounding helpers ---

    /** Calories: rounded to integer */
    private fun arrondirCalories(value: Double): Double = value.roundToLong().toDouble()

    /** Macros (g): 1 decimal place */
    private fun arrondirMacro(value: Double): Double = (value * 10).roundToLong() / 10.0

    /** Micros (mg): rounded to integer */
    private fun arrondirMicro(value: Double): Double = value.roundToInt().toDouble()

    /** Micros with decimal (B12, D): 1 decimal place */
    private fun arrondirMicroDecimal(value: Double): Double = (value * 10).roundToLong() / 10.0

    private fun buildQuota(
        userId: String,
        nutriment: NutrimentType,
        valeur: Double,
        unite: String,
        now: kotlin.time.Instant,
    ): QuotaJournalier = QuotaJournalier(
        userId = userId,
        nutriment = nutriment,
        valeurCible = valeur,
        estPersonnalise = false,
        valeurCalculee = valeur,
        unite = unite,
        updatedAt = now,
    )

    companion object {
        /**
         * Default profile values for incomplete profiles (onboarding skipped).
         */
        fun defaultProfile(userId: String): UserProfile {
            return UserProfile(
                userId = userId,
                sexe = Sexe.HOMME,
                age = 30,
                poidsKg = 75.0,
                tailleCm = 175,
                regimeAlimentaire = RegimeAlimentaire.OMNIVORE,
                niveauActivite = NiveauActivite.MODERE,
                onboardingComplete = false,
                objectifPoids = null,
                updatedAt = Clock.System.now(),
            )
        }
    }
}
