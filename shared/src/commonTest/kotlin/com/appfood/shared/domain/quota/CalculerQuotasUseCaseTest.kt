package com.appfood.shared.domain.quota

import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.ObjectifPoids
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.model.UserProfile
import com.appfood.shared.util.AppResult
import kotlinx.datetime.Clock
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CalculerQuotasUseCaseTest {

    private val useCase = CalculerQuotasUseCase()
    private val now = Clock.System.now()

    // --- Helper ---

    private fun buildProfile(
        sexe: Sexe = Sexe.HOMME,
        age: Int = 30,
        poidsKg: Double = 80.0,
        tailleCm: Int = 180,
        regime: RegimeAlimentaire = RegimeAlimentaire.OMNIVORE,
        activite: NiveauActivite = NiveauActivite.MODERE,
    ): UserProfile = UserProfile(
        userId = "test-user",
        sexe = sexe,
        age = age,
        poidsKg = poidsKg,
        tailleCm = tailleCm,
        regimeAlimentaire = regime,
        niveauActivite = activite,
        onboardingComplete = true,
        objectifPoids = null,
        updatedAt = now,
    )

    private fun List<QuotaJournalier>.findByNutriment(type: NutrimentType): QuotaJournalier =
        first { it.nutriment == type }

    private fun assertApprox(expected: Double, actual: Double, tolerance: Double = 1.0, message: String = "") {
        assertTrue(abs(expected - actual) <= tolerance, "$message: expected ~$expected but was $actual")
    }

    // --- QUOTAS-01: Mifflin-St Jeor formula ---

    @Test
    fun `should calculate correct BMR for homme`() {
        // Given: homme 80kg, 180cm, 30 ans
        // MB = (10 * 80) + (6.25 * 180) - (5 * 30) + 5 = 800 + 1125 - 150 + 5 = 1780
        val mb = useCase.calculerMetabolismeBase(Sexe.HOMME, 80.0, 180, 30)

        // Then
        assertEquals(1780.0, mb)
    }

    @Test
    fun `should calculate correct BMR for femme`() {
        // Given: femme 60kg, 165cm, 25 ans
        // MB = (10 * 60) + (6.25 * 165) - (5 * 25) - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        val mb = useCase.calculerMetabolismeBase(Sexe.FEMME, 60.0, 165, 25)

        // Then
        assertEquals(1345.25, mb)
    }

    @Test
    fun `should return correct activity coefficients`() {
        assertEquals(1.2, useCase.getCoeffActivite(NiveauActivite.SEDENTAIRE))
        assertEquals(1.375, useCase.getCoeffActivite(NiveauActivite.LEGER))
        assertEquals(1.55, useCase.getCoeffActivite(NiveauActivite.MODERE))
        assertEquals(1.725, useCase.getCoeffActivite(NiveauActivite.ACTIF))
        assertEquals(1.9, useCase.getCoeffActivite(NiveauActivite.TRES_ACTIF))
    }

    @Test
    fun `should calculate correct calories for homme actif 80kg`() {
        // Given
        val profile = buildProfile(
            sexe = Sexe.HOMME,
            age = 30,
            poidsKg = 80.0,
            tailleCm = 180,
            activite = NiveauActivite.MODERE,
        )

        // When
        val result = useCase.execute(profile)

        // Then
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val calories = result.data.findByNutriment(NutrimentType.CALORIES)
        // MB = 1780, DET = 1780 * 1.55 = 2759, rounded to integer
        assertApprox(2759.0, calories.valeurCible, 1.0, "Calories")
        assertEquals("kcal", calories.unite)
    }

    @Test
    fun `should calculate correct macros for homme modere`() {
        // Given
        val profile = buildProfile(activite = NiveauActivite.MODERE)
        // DET = 1780 * 1.55 = 2759

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val quotas = result.data

        // Then
        // PROTEINES = 2759 * 0.15 / 4 = 103.4625 -> 103.5
        assertApprox(103.5, quotas.findByNutriment(NutrimentType.PROTEINES).valeurCible, 0.2, "Proteines")

        // GLUCIDES = 2759 * 0.50 / 4 = 344.875 -> 344.9
        assertApprox(344.9, quotas.findByNutriment(NutrimentType.GLUCIDES).valeurCible, 0.2, "Glucides")

        // LIPIDES = 2759 * 0.35 / 9 = 107.295... -> 107.3
        assertApprox(107.3, quotas.findByNutriment(NutrimentType.LIPIDES).valeurCible, 0.2, "Lipides")

        // FIBRES = 30g (adulte)
        assertEquals(30.0, quotas.findByNutriment(NutrimentType.FIBRES).valeurCible)

        // SEL = 5g
        assertEquals(5.0, quotas.findByNutriment(NutrimentType.SEL).valeurCible)

        // SUCRES = 2759 * 0.10 / 4 = 68.975 -> 69.0
        assertApprox(69.0, quotas.findByNutriment(NutrimentType.SUCRES).valeurCible, 0.2, "Sucres")
    }

    @Test
    fun `should return all 16 nutriment quotas`() {
        // Given
        val profile = buildProfile()

        // When
        val result = useCase.execute(profile)

        // Then
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        assertEquals(NutrimentType.entries.size, result.data.size)

        // Verify all nutriment types are present
        val types = result.data.map { it.nutriment }.toSet()
        assertEquals(NutrimentType.entries.toSet(), types)
    }

    // --- Micronutrients ANSES ---

    @Test
    fun `should return correct micronutrients for homme adulte omnivore`() {
        // Given
        val profile = buildProfile(sexe = Sexe.HOMME, age = 30, regime = RegimeAlimentaire.OMNIVORE)

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val quotas = result.data

        // Then — ANSES values for homme 19-64
        assertEquals(11.0, quotas.findByNutriment(NutrimentType.FER).valeurCible)
        assertEquals(950.0, quotas.findByNutriment(NutrimentType.CALCIUM).valeurCible)
        assertEquals(11.0, quotas.findByNutriment(NutrimentType.ZINC).valeurCible)
        assertEquals(380.0, quotas.findByNutriment(NutrimentType.MAGNESIUM).valeurCible)
        assertEquals(4.0, quotas.findByNutriment(NutrimentType.VITAMINE_B12).valeurCible)
        assertEquals(15.0, quotas.findByNutriment(NutrimentType.VITAMINE_D).valeurCible)
        assertEquals(110.0, quotas.findByNutriment(NutrimentType.VITAMINE_C).valeurCible)
        assertEquals(2.5, quotas.findByNutriment(NutrimentType.OMEGA_3).valeurCible)
        assertEquals(10.0, quotas.findByNutriment(NutrimentType.OMEGA_6).valeurCible)
    }

    @Test
    fun `should return correct micronutrients for femme adulte omnivore`() {
        // Given
        val profile = buildProfile(
            sexe = Sexe.FEMME,
            age = 30,
            poidsKg = 60.0,
            tailleCm = 165,
            regime = RegimeAlimentaire.OMNIVORE,
        )

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val quotas = result.data

        // Then — ANSES values for femme 19-64
        assertEquals(16.0, quotas.findByNutriment(NutrimentType.FER).valeurCible)
        assertEquals(8.0, quotas.findByNutriment(NutrimentType.ZINC).valeurCible)
        assertEquals(300.0, quotas.findByNutriment(NutrimentType.MAGNESIUM).valeurCible)
        assertEquals(2.0, quotas.findByNutriment(NutrimentType.OMEGA_3).valeurCible)
        assertEquals(8.0, quotas.findByNutriment(NutrimentType.OMEGA_6).valeurCible)
    }

    // --- Age adjustments ---

    @Test
    fun `should apply adolescent adjustments for age 14-17`() {
        // Given
        val profile = buildProfile(sexe = Sexe.HOMME, age = 16)

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val quotas = result.data

        // Then — 14-18 bracket: CALCIUM=1000, FER_H=13, ZINC_H=13
        assertEquals(1000.0, quotas.findByNutriment(NutrimentType.CALCIUM).valeurCible)
        assertEquals(13.0, quotas.findByNutriment(NutrimentType.FER).valeurCible)
        assertEquals(13.0, quotas.findByNutriment(NutrimentType.ZINC).valeurCible)
    }

    @Test
    fun `should use 14-18 bracket for age under 14`() {
        // Given — age < 14 uses 14-18 values
        val profile = buildProfile(sexe = Sexe.HOMME, age = 12)

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val quotas = result.data

        // Then
        assertEquals(1000.0, quotas.findByNutriment(NutrimentType.CALCIUM).valeurCible)
        assertEquals(13.0, quotas.findByNutriment(NutrimentType.FER).valeurCible)
    }

    @Test
    fun `should use fibres 25g for age under 18`() {
        // Given
        val profile = buildProfile(age = 16)

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)

        // Then
        assertEquals(25.0, result.data.findByNutriment(NutrimentType.FIBRES).valeurCible)
    }

    @Test
    fun `should apply senior adjustments for age 65 plus`() {
        // Given
        val profile = buildProfile(sexe = Sexe.HOMME, age = 70, poidsKg = 75.0)

        // When
        val result = useCase.execute(profile)
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val quotas = result.data

        // Then — 65+: VITAMINE_D=20, CALCIUM=1200
        assertEquals(20.0, quotas.findByNutriment(NutrimentType.VITAMINE_D).valeurCible)
        assertEquals(1200.0, quotas.findByNutriment(NutrimentType.CALCIUM).valeurCible)

        // Proteines = max(DET * 0.15 / 4, 75 * 1.0) = max(calcule, 75)
        val proteines = quotas.findByNutriment(NutrimentType.PROTEINES).valeurCible
        assertTrue(proteines >= 75.0, "Proteines for 65+ should be at least 1g/kg: $proteines")
    }

    // --- Regime adjustments ---

    @Test
    fun `should apply vegan coefficients`() {
        // Given
        val profileOmnivore = buildProfile(regime = RegimeAlimentaire.OMNIVORE)
        val profileVegan = buildProfile(regime = RegimeAlimentaire.VEGAN)

        // When
        val resultOmnivore = useCase.execute(profileOmnivore) as AppResult.Success
        val resultVegan = useCase.execute(profileVegan) as AppResult.Success

        // Then — VEGAN: FER x1.8, ZINC x1.5, OMEGA_3 x1.5, PROTEINES x1.1
        val ferOmni = resultOmnivore.data.findByNutriment(NutrimentType.FER).valeurCible
        val ferVegan = resultVegan.data.findByNutriment(NutrimentType.FER).valeurCible
        assertApprox(ferOmni * 1.8, ferVegan, 1.0, "Fer vegan")

        val zincOmni = resultOmnivore.data.findByNutriment(NutrimentType.ZINC).valeurCible
        val zincVegan = resultVegan.data.findByNutriment(NutrimentType.ZINC).valeurCible
        assertApprox(zincOmni * 1.5, zincVegan, 1.0, "Zinc vegan")

        val omega3Omni = resultOmnivore.data.findByNutriment(NutrimentType.OMEGA_3).valeurCible
        val omega3Vegan = resultVegan.data.findByNutriment(NutrimentType.OMEGA_3).valeurCible
        assertApprox(omega3Omni * 1.5, omega3Vegan, 0.2, "Omega3 vegan")

        val protOmni = resultOmnivore.data.findByNutriment(NutrimentType.PROTEINES).valeurCible
        val protVegan = resultVegan.data.findByNutriment(NutrimentType.PROTEINES).valeurCible
        assertApprox(protOmni * 1.1, protVegan, 0.5, "Proteines vegan")
    }

    @Test
    fun `should apply vegetarien coefficients`() {
        // Given
        val profileOmnivore = buildProfile(regime = RegimeAlimentaire.OMNIVORE)
        val profileVege = buildProfile(regime = RegimeAlimentaire.VEGETARIEN)

        // When
        val resultOmni = useCase.execute(profileOmnivore) as AppResult.Success
        val resultVege = useCase.execute(profileVege) as AppResult.Success

        // Then — VEGETARIEN: FER x1.5, ZINC x1.3, OMEGA_3 x1.2
        val ferOmni = resultOmni.data.findByNutriment(NutrimentType.FER).valeurCible
        val ferVege = resultVege.data.findByNutriment(NutrimentType.FER).valeurCible
        assertApprox(ferOmni * 1.5, ferVege, 1.0, "Fer vegetarien")
    }

    // --- Units ---

    @Test
    fun `should assign correct units to all nutriments`() {
        // Given
        val profile = buildProfile()

        // When
        val result = useCase.execute(profile) as AppResult.Success
        val quotas = result.data

        // Then
        assertEquals("kcal", quotas.findByNutriment(NutrimentType.CALORIES).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.PROTEINES).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.GLUCIDES).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.LIPIDES).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.FIBRES).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.SEL).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.SUCRES).unite)
        assertEquals("mg", quotas.findByNutriment(NutrimentType.FER).unite)
        assertEquals("mg", quotas.findByNutriment(NutrimentType.CALCIUM).unite)
        assertEquals("mg", quotas.findByNutriment(NutrimentType.ZINC).unite)
        assertEquals("mg", quotas.findByNutriment(NutrimentType.MAGNESIUM).unite)
        assertEquals("\u00b5g", quotas.findByNutriment(NutrimentType.VITAMINE_B12).unite)
        assertEquals("\u00b5g", quotas.findByNutriment(NutrimentType.VITAMINE_D).unite)
        assertEquals("mg", quotas.findByNutriment(NutrimentType.VITAMINE_C).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.OMEGA_3).unite)
        assertEquals("g", quotas.findByNutriment(NutrimentType.OMEGA_6).unite)
    }

    // --- estPersonnalise ---

    @Test
    fun `should set estPersonnalise to false for all calculated quotas`() {
        // Given
        val profile = buildProfile()

        // When
        val result = useCase.execute(profile) as AppResult.Success

        // Then
        result.data.forEach { quota ->
            assertEquals(false, quota.estPersonnalise, "Quota ${quota.nutriment} should not be personalized")
            assertEquals(quota.valeurCible, quota.valeurCalculee, "valeurCible should equal valeurCalculee")
        }
    }

    // --- Default profile ---

    @Test
    fun `should provide sensible default profile`() {
        // Given
        val defaultProfile = CalculerQuotasUseCase.defaultProfile("user-123")

        // When
        val result = useCase.execute(defaultProfile)

        // Then — should succeed with reasonable values
        assertIs<AppResult.Success<List<QuotaJournalier>>>(result)
        val calories = result.data.findByNutriment(NutrimentType.CALORIES).valeurCible
        // Default: homme 30 ans, 75kg, 175cm, MODERE
        // MB = (10*75) + (6.25*175) - (5*30) + 5 = 750 + 1093.75 - 150 + 5 = 1698.75
        // DET = 1698.75 * 1.55 = 2633.0625
        assertApprox(2633.0, calories, 1.0, "Default profile calories")
    }
}
