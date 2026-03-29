package com.appfood.shared.domain.recommandation

import com.appfood.shared.model.Aliment
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.QuotaStatus
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import com.appfood.shared.model.UserPreferences
import com.appfood.shared.util.AppResult
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RecommandationAlimentUseCaseTest {

    private val useCase = RecommandationAlimentUseCase()
    private val now = Clock.System.now()

    // --- Helpers ---

    private fun buildAliment(
        id: String = "aliment-1",
        nom: String = "Lentilles cuites",
        categorie: String = "Legumineuses",
        regimes: List<RegimeAlimentaire> = listOf(RegimeAlimentaire.VEGAN),
        nutriments: NutrimentValues = NutrimentValues(
            calories = 116.0,
            proteines = 9.0,
            glucides = 20.0,
            lipides = 0.4,
            fibres = 7.9,
            fer = 3.3,
            calcium = 19.0,
            zinc = 1.3,
            magnesium = 36.0,
            vitamineB12 = 0.0,
            vitamineD = 0.0,
            vitamineC = 1.5,
            omega3 = 0.1,
            omega6 = 0.2,
        ),
    ): Aliment = Aliment(
        id = id,
        nom = nom,
        marque = null,
        source = SourceAliment.CIQUAL,
        sourceId = null,
        codeBarres = null,
        categorie = categorie,
        regimesCompatibles = regimes,
        nutrimentsPour100g = nutriments,
    )

    private fun buildQuotaStatus(
        nutriment: NutrimentType,
        valeurCible: Double,
        valeurConsommee: Double,
    ): QuotaStatus = QuotaStatus(
        nutriment = nutriment,
        valeurCible = valeurCible,
        valeurConsommee = valeurConsommee,
        pourcentage = if (valeurCible > 0) (valeurConsommee / valeurCible) * 100.0 else 0.0,
        unite = "mg",
    )

    private val defaultPreferences = UserPreferences(
        userId = "test-user",
        alimentsExclus = emptyList(),
        allergies = emptyList(),
        alimentsFavoris = emptyList(),
        updatedAt = now,
    )

    // --- Deficit identification ---

    @Test
    fun `should identify strong deficit when below 70 percent`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 5.0), // 45%
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(1, deficits.size)
        assertEquals(RecommandationAlimentUseCase.NiveauDeficit.FORT, deficits[0].niveau)
        assertEquals(6.0, deficits[0].manque)
    }

    @Test
    fun `should identify moderate deficit when between 70 and 90 percent`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 760.0), // 80%
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(1, deficits.size)
        assertEquals(RecommandationAlimentUseCase.NiveauDeficit.MODERE, deficits[0].niveau)
    }

    @Test
    fun `should not identify deficit when above 90 percent`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 10.5), // 95%
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(0, deficits.size)
    }

    // --- Critical nutrients ---

    @Test
    fun `should return vegan critical nutrients`() {
        val critiques = useCase.getNutrimentsCritiques(RegimeAlimentaire.VEGAN)
        assertTrue(NutrimentType.VITAMINE_B12 in critiques)
        assertTrue(NutrimentType.FER in critiques)
        assertTrue(NutrimentType.ZINC in critiques)
        assertTrue(NutrimentType.OMEGA_3 in critiques)
        assertTrue(NutrimentType.CALCIUM in critiques)
        assertTrue(NutrimentType.PROTEINES in critiques)
    }

    @Test
    fun `should return empty critical nutrients for omnivore`() {
        val critiques = useCase.getNutrimentsCritiques(RegimeAlimentaire.OMNIVORE)
        assertTrue(critiques.isEmpty())
    }

    // --- Deficit weight calculation ---

    @Test
    fun `should assign weight 3 for strong deficit on critical vegan nutrient`() {
        // Given
        val deficits = listOf(
            RecommandationAlimentUseCase.Deficit(NutrimentType.FER, RecommandationAlimentUseCase.NiveauDeficit.FORT, 6.0, 45.0),
        )
        val critiques = setOf(NutrimentType.FER, NutrimentType.VITAMINE_B12)

        // When
        val poids = useCase.calculerPoidsDeficits(deficits, critiques)

        // Then
        assertEquals(3.0, poids[NutrimentType.FER])
    }

    @Test
    fun `should assign weight 2 for strong deficit on non-critical nutrient`() {
        // Given
        val deficits = listOf(
            RecommandationAlimentUseCase.Deficit(NutrimentType.MAGNESIUM, RecommandationAlimentUseCase.NiveauDeficit.FORT, 200.0, 40.0),
        )

        // When
        val poids = useCase.calculerPoidsDeficits(deficits, emptySet())

        // Then
        assertEquals(2.0, poids[NutrimentType.MAGNESIUM])
    }

    @Test
    fun `should assign weight 1 for moderate deficit`() {
        // Given
        val deficits = listOf(
            RecommandationAlimentUseCase.Deficit(NutrimentType.CALCIUM, RecommandationAlimentUseCase.NiveauDeficit.MODERE, 190.0, 80.0),
        )

        // When
        val poids = useCase.calculerPoidsDeficits(deficits, setOf(NutrimentType.CALCIUM))

        // Then
        assertEquals(1.0, poids[NutrimentType.CALCIUM])
    }

    // --- Food filtering ---

    @Test
    fun `should exclude foods from exclusion list`() {
        // Given
        val aliments = listOf(
            buildAliment(id = "a1", nom = "Lentilles"),
            buildAliment(id = "a2", nom = "Tofu"),
        )
        val prefs = defaultPreferences.copy(alimentsExclus = listOf("a1"))

        // When
        val filtered = useCase.filtrerAliments(aliments, RegimeAlimentaire.VEGAN, prefs)

        // Then
        assertEquals(1, filtered.size)
        assertEquals("a2", filtered[0].id)
    }

    @Test
    fun `should exclude foods with allergen category match`() {
        // Given
        val aliments = listOf(
            buildAliment(id = "a1", nom = "Tofu ferme", categorie = "Soja et derives"),
            buildAliment(id = "a2", nom = "Lentilles", categorie = "Legumineuses"),
        )
        val prefs = defaultPreferences.copy(allergies = listOf("soja"))

        // When
        val filtered = useCase.filtrerAliments(aliments, RegimeAlimentaire.VEGAN, prefs)

        // Then
        assertEquals(1, filtered.size)
        assertEquals("a2", filtered[0].id)
    }

    @Test
    fun `should not exclude sans gluten food for gluten allergy`() {
        // Given
        val aliment = buildAliment(
            id = "a1",
            nom = "Pain sans gluten",
            categorie = "Pains de ble",
        )

        // When
        val hasAllergene = useCase.hasAllergene(aliment, listOf("gluten"))

        // Then — "sans gluten" in name exempts it
        assertEquals(false, hasAllergene)
    }

    @Test
    fun `should filter by regime compatibility`() {
        // Given
        val aliments = listOf(
            buildAliment(id = "a1", regimes = listOf(RegimeAlimentaire.VEGAN)),
            buildAliment(id = "a2", regimes = listOf(RegimeAlimentaire.OMNIVORE)),
        )

        // When — user is VEGAN
        val filtered = useCase.filtrerAliments(aliments, RegimeAlimentaire.VEGAN, defaultPreferences)

        // Then — only VEGAN food should be included
        assertEquals(1, filtered.size)
        assertEquals("a1", filtered[0].id)
    }

    @Test
    fun `should allow vegan food for omnivore user`() {
        // Given
        val aliments = listOf(
            buildAliment(id = "a1", regimes = listOf(RegimeAlimentaire.VEGAN)),
        )

        // When — user is OMNIVORE
        val filtered = useCase.filtrerAliments(aliments, RegimeAlimentaire.OMNIVORE, defaultPreferences)

        // Then — vegan food is compatible with omnivore
        assertEquals(1, filtered.size)
    }

    // --- Suggested quantity ---

    @Test
    fun `should calculate suggested quantity capped at 300g`() {
        // Given — food with very low iron density, need would be > 300g
        val aliment = buildAliment(
            nutriments = NutrimentValues(fer = 0.5), // 0.5mg per 100g
        )
        val deficits = listOf(
            RecommandationAlimentUseCase.Deficit(NutrimentType.FER, RecommandationAlimentUseCase.NiveauDeficit.FORT, 10.0, 10.0),
        )
        // quantite = (10.0 / 0.5) * 100 = 2000g -> capped at 300g

        // When
        val quantite = useCase.calculerQuantiteSuggeree(aliment, deficits)

        // Then
        assertEquals(300.0, quantite)
    }

    @Test
    fun `should round suggested quantity to nearest 10g`() {
        // Given — food with good iron density
        val aliment = buildAliment(
            nutriments = NutrimentValues(fer = 3.3),
        )
        val deficits = listOf(
            RecommandationAlimentUseCase.Deficit(NutrimentType.FER, RecommandationAlimentUseCase.NiveauDeficit.FORT, 6.0, 45.0),
        )
        // quantite = (6.0 / 3.3) * 100 = 181.8g -> rounded to 180g

        // When
        val quantite = useCase.calculerQuantiteSuggeree(aliment, deficits)

        // Then
        assertEquals(180.0, quantite)
    }

    // --- Diversity ---

    @Test
    fun `should limit to max 2 foods per category`() {
        // Given
        val reco1 = RecommandationAlimentUseCase().let {
            buildAliment(id = "a1", categorie = "Legumineuses")
        }
        val reco2 = buildAliment(id = "a2", categorie = "Legumineuses")
        val reco3 = buildAliment(id = "a3", categorie = "Legumineuses")
        val reco4 = buildAliment(id = "a4", categorie = "Cereales")

        val scored = listOf(
            com.appfood.shared.model.RecommandationAliment(reco1, emptyList(), 100.0, emptyMap()) to 10.0,
            com.appfood.shared.model.RecommandationAliment(reco2, emptyList(), 100.0, emptyMap()) to 9.0,
            com.appfood.shared.model.RecommandationAliment(reco3, emptyList(), 100.0, emptyMap()) to 8.0,
            com.appfood.shared.model.RecommandationAliment(reco4, emptyList(), 100.0, emptyMap()) to 7.0,
        )

        // When
        val result = useCase.appliquerDiversite(scored)

        // Then — 2 Legumineuses + 1 Cereales = 3 total
        assertEquals(3, result.size)
        val categories = result.map { it.first.aliment.categorie }
        assertEquals(2, categories.count { it == "Legumineuses" })
        assertEquals(1, categories.count { it == "Cereales" })
    }

    // --- Full algorithm integration ---

    @Test
    fun `should return recommendations sorted by score descending`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0), // 27% — FORT deficit
        )

        val alimentRiche = buildAliment(
            id = "riche",
            nom = "Lentilles",
            categorie = "Legumineuses",
            nutriments = NutrimentValues(fer = 3.3),
        )
        val alimentPauvre = buildAliment(
            id = "pauvre",
            nom = "Riz blanc",
            categorie = "Cereales",
            nutriments = NutrimentValues(fer = 0.8),
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            aliments = listOf(alimentPauvre, alimentRiche),
            regime = RegimeAlimentaire.VEGAN,
            preferences = defaultPreferences,
        )

        // Then
        assertIs<AppResult.Success<*>>(result)
        val recos = (result as AppResult.Success).data
        assertTrue(recos.size >= 2, "Should have at least 2 recommendations")
        assertEquals("riche", recos[0].aliment.id, "Food with higher iron density should rank first")
    }

    @Test
    fun `should return empty list when no deficits`() {
        // Given — all quotas at 95%+
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 10.5),
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 920.0),
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            aliments = listOf(buildAliment()),
            regime = RegimeAlimentaire.OMNIVORE,
            preferences = defaultPreferences,
        )

        // Then
        assertIs<AppResult.Success<*>>(result)
        val recos = (result as AppResult.Success).data
        assertTrue(recos.isEmpty(), "No recommendations when no deficits")
    }

    @Test
    fun `should respect limit parameter`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 2.0),
        )

        val aliments = (1..20).map { i ->
            buildAliment(
                id = "a$i",
                nom = "Aliment $i",
                categorie = "Cat${i % 10}", // diverse categories
                nutriments = NutrimentValues(fer = 1.0 + i * 0.1),
            )
        }

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            aliments = aliments,
            regime = RegimeAlimentaire.OMNIVORE,
            preferences = defaultPreferences,
            limit = 5,
        )

        // Then
        assertIs<AppResult.Success<*>>(result)
        val recos = (result as AppResult.Success).data
        assertTrue(recos.size <= 5, "Should respect limit of 5")
    }

    @Test
    fun `should include pourcentageCouverture for each deficit nutriment`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0), // manque = 8mg
        )

        val aliment = buildAliment(
            nutriments = NutrimentValues(fer = 3.3), // 3.3mg per 100g
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            aliments = listOf(aliment),
            regime = RegimeAlimentaire.OMNIVORE,
            preferences = defaultPreferences,
        )

        // Then
        assertIs<AppResult.Success<*>>(result)
        val recos = (result as AppResult.Success).data
        assertTrue(recos.isNotEmpty())
        assertTrue(NutrimentType.FER in recos[0].pourcentageCouverture)
        assertTrue(recos[0].pourcentageCouverture[NutrimentType.FER]!! > 0.0)
    }
}
