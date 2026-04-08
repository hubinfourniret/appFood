package com.appfood.shared.domain.recommandation

import com.appfood.shared.model.IngredientRecette
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.QuotaStatus
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RecommandationRecette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceRecette
import com.appfood.shared.util.AppResult
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class RecommandationRecetteUseCaseTest {

    private val useCase = RecommandationRecetteUseCase()
    private val now = Clock.System.now()

    // --- Helpers ---

    private fun buildRecette(
        id: String = "recette-1",
        nom: String = "Dhal de lentilles",
        nbPortions: Int = 4,
        regimes: List<RegimeAlimentaire> = listOf(RegimeAlimentaire.VEGAN),
        nutrimentsTotaux: NutrimentValues = NutrimentValues(
            calories = 800.0,
            proteines = 40.0,
            glucides = 120.0,
            lipides = 10.0,
            fibres = 30.0,
            fer = 16.0,
            calcium = 100.0,
            zinc = 6.0,
            magnesium = 120.0,
            vitamineB12 = 0.0,
            vitamineD = 0.0,
            vitamineC = 20.0,
            omega3 = 2.0,
            omega6 = 4.0,
        ),
    ): Recette = Recette(
        id = id,
        nom = nom,
        description = "Test recipe",
        tempsPreparationMin = 15,
        tempsCuissonMin = 30,
        nbPortions = nbPortions,
        regimesCompatibles = regimes,
        source = SourceRecette.MANUELLE,
        typeRepas = listOf(MealType.DEJEUNER),
        ingredients = listOf(
            IngredientRecette("aliment-1", "Lentilles corail", 200.0),
        ),
        etapes = listOf("Step 1"),
        nutrimentsTotaux = nutrimentsTotaux,
        imageUrl = null,
        publie = true,
        createdAt = now,
        updatedAt = now,
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

    private fun assertApprox(expected: Double, actual: Double, tolerance: Double = 1.0, message: String = "") {
        assertTrue(abs(expected - actual) <= tolerance, "$message: expected ~$expected but was $actual")
    }

    // --- Deficit identification ---

    @Test
    fun `should identify deficit when below 90 percent`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 8.0), // 72.7%
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(1, deficits.size)
        assertEquals(NutrimentType.FER, deficits[0].nutriment)
        assertApprox(3.0, deficits[0].manque, 0.1, "Iron deficit amount")
    }

    @Test
    fun `should not identify deficit when at or above 90 percent`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 10.0), // 90.9%
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(0, deficits.size)
    }

    @Test
    fun `should ignore nutrients with zero target`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 0.0, 5.0),
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(0, deficits.size)
    }

    @Test
    fun `should identify multiple deficits`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 5.0),     // 45%
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 800.0), // 84%
            buildQuotaStatus(NutrimentType.ZINC, 11.0, 10.5),   // 95% — no deficit
        )

        // When
        val deficits = useCase.identifierDeficits(statuses)

        // Then
        assertEquals(2, deficits.size)
        val nutriments = deficits.map { it.nutriment }.toSet()
        assertTrue(NutrimentType.FER in nutriments)
        assertTrue(NutrimentType.CALCIUM in nutriments)
    }

    // --- Regime filtering ---

    @Test
    fun `should filter recipes by regime compatibility`() {
        // Given
        val recettes = listOf(
            buildRecette(id = "r1", regimes = listOf(RegimeAlimentaire.VEGAN)),
            buildRecette(id = "r2", regimes = listOf(RegimeAlimentaire.OMNIVORE)),
        )

        // When — user is VEGAN
        val filtered = useCase.filtrerParRegime(recettes, RegimeAlimentaire.VEGAN)

        // Then — only VEGAN recipe
        assertEquals(1, filtered.size)
        assertEquals("r1", filtered[0].id)
    }

    @Test
    fun `should allow vegan recipe for omnivore user`() {
        // Given
        val recettes = listOf(
            buildRecette(id = "r1", regimes = listOf(RegimeAlimentaire.VEGAN)),
        )

        // When — user is OMNIVORE
        val filtered = useCase.filtrerParRegime(recettes, RegimeAlimentaire.OMNIVORE)

        // Then — vegan is compatible with omnivore
        assertEquals(1, filtered.size)
    }

    @Test
    fun `should allow vegetarien recipe for flexitarien user`() {
        // Given
        val recettes = listOf(
            buildRecette(id = "r1", regimes = listOf(RegimeAlimentaire.VEGETARIEN)),
        )

        // When
        val filtered = useCase.filtrerParRegime(recettes, RegimeAlimentaire.FLEXITARIEN)

        // Then
        assertEquals(1, filtered.size)
    }

    @Test
    fun `should not allow omnivore recipe for vegan user`() {
        // Given
        val recettes = listOf(
            buildRecette(id = "r1", regimes = listOf(RegimeAlimentaire.OMNIVORE)),
        )

        // When
        val filtered = useCase.filtrerParRegime(recettes, RegimeAlimentaire.VEGAN)

        // Then
        assertEquals(0, filtered.size)
    }

    // --- Scoring ---

    @Test
    fun `should score recipe based on deficit coverage per portion`() {
        // Given — recipe with 4 portions, total iron = 16mg => 4mg per portion
        val recette = buildRecette(
            nbPortions = 4,
            nutrimentsTotaux = NutrimentValues(fer = 16.0),
        )
        val deficits = listOf(
            RecommandationRecetteUseCase.Deficit(NutrimentType.FER, 8.0, 30.0),
        )
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0),
        )

        // When
        val scored = useCase.scoreRecette(recette, deficits, statuses)

        // Then
        assertTrue(scored != null, "Recipe should have a score")
        val (reco, score) = scored!!
        // 4mg per portion / 8mg manque = 50% coverage
        assertApprox(50.0, reco.pourcentageCouverture[NutrimentType.FER]!!, 0.1, "Iron coverage")
        // Global coverage = 50% / 1 deficit = 50%
        assertApprox(50.0, score, 0.1, "Global coverage score")
    }

    @Test
    fun `should cap coverage at 100 percent per nutrient`() {
        // Given — recipe provides more than the deficit
        val recette = buildRecette(
            nbPortions = 1,
            nutrimentsTotaux = NutrimentValues(fer = 20.0),
        )
        val deficits = listOf(
            RecommandationRecetteUseCase.Deficit(NutrimentType.FER, 5.0, 50.0),
        )
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 6.0),
        )

        // When
        val scored = useCase.scoreRecette(recette, deficits, statuses)

        // Then — capped at 100%
        assertTrue(scored != null)
        assertEquals(100.0, scored!!.first.pourcentageCouverture[NutrimentType.FER]!!)
    }

    @Test
    fun `should return null score when recipe contributes to no deficit`() {
        // Given — recipe only has calories, deficit is on fer
        val recette = buildRecette(
            nutrimentsTotaux = NutrimentValues(calories = 500.0),
        )
        val deficits = listOf(
            RecommandationRecetteUseCase.Deficit(NutrimentType.FER, 8.0, 30.0),
        )
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0),
        )

        // When
        val scored = useCase.scoreRecette(recette, deficits, statuses)

        // Then
        assertEquals(null, scored)
    }

    @Test
    fun `should calculate global coverage as mean of all deficit coverages`() {
        // Given — recipe covers 2 out of 3 deficits
        val recette = buildRecette(
            nbPortions = 2,
            nutrimentsTotaux = NutrimentValues(
                fer = 10.0,    // 5mg per portion
                calcium = 400.0, // 200mg per portion
            ),
        )
        val deficits = listOf(
            RecommandationRecetteUseCase.Deficit(NutrimentType.FER, 10.0, 10.0),
            RecommandationRecetteUseCase.Deficit(NutrimentType.CALCIUM, 500.0, 50.0),
            RecommandationRecetteUseCase.Deficit(NutrimentType.ZINC, 5.0, 50.0),
        )
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 1.0),
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 450.0),
            buildQuotaStatus(NutrimentType.ZINC, 11.0, 6.0),
        )

        // When
        val scored = useCase.scoreRecette(recette, deficits, statuses)

        // Then
        assertTrue(scored != null)
        val (reco, score) = scored!!
        // Fer coverage = 5/10 * 100 = 50%
        // Calcium coverage = 200/500 * 100 = 40%
        // Zinc = 0 (not in recipe)
        // Global = (50 + 40) / 3 deficits = 30%
        assertApprox(50.0, reco.pourcentageCouverture[NutrimentType.FER]!!, 0.1, "Fer coverage")
        assertApprox(40.0, reco.pourcentageCouverture[NutrimentType.CALCIUM]!!, 0.1, "Calcium coverage")
        assertApprox(30.0, score, 0.1, "Global coverage")
    }

    @Test
    fun `should handle nbPortions of zero by coercing to 1`() {
        // Given — nbPortions = 0 (edge case)
        val recette = buildRecette(
            nbPortions = 0,
            nutrimentsTotaux = NutrimentValues(fer = 10.0),
        )
        val deficits = listOf(
            RecommandationRecetteUseCase.Deficit(NutrimentType.FER, 5.0, 50.0),
        )
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 6.0),
        )

        // When
        val scored = useCase.scoreRecette(recette, deficits, statuses)

        // Then — should not crash, coerceAtLeast(1) applied
        assertTrue(scored != null)
        // 10mg per portion (nbPortions coerced to 1) / 5mg manque = 200% => capped at 100%
        assertEquals(100.0, scored!!.first.pourcentageCouverture[NutrimentType.FER]!!)
    }

    // --- Full algorithm integration ---

    @Test
    fun `should return empty list when no deficits`() {
        // Given — all quotas above 90%
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 10.5),
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 920.0),
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = listOf(buildRecette()),
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        assertTrue((result as AppResult.Success).data.isEmpty(), "No recommendations when no deficits")
    }

    @Test
    fun `should return recommendations sorted by global coverage descending`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0), // 27% — deficit of 8mg
        )

        val recetteRiche = buildRecette(
            id = "r-riche",
            nom = "Dhal riche en fer",
            nbPortions = 2,
            nutrimentsTotaux = NutrimentValues(fer = 20.0), // 10mg per portion
        )
        val recettePauvre = buildRecette(
            id = "r-pauvre",
            nom = "Salade legere",
            nbPortions = 2,
            nutrimentsTotaux = NutrimentValues(fer = 4.0), // 2mg per portion
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = listOf(recettePauvre, recetteRiche),
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        val recos = (result as AppResult.Success).data
        assertEquals(2, recos.size)
        assertEquals("r-riche", recos[0].recette.id, "Recipe with higher iron per portion should rank first")
    }

    @Test
    fun `should respect limit parameter`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 2.0),
        )

        val recettes = (1..10).map { i ->
            buildRecette(
                id = "r$i",
                nom = "Recette $i",
                nutrimentsTotaux = NutrimentValues(fer = (5 + i).toDouble()),
            )
        }

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = recettes,
            regime = RegimeAlimentaire.VEGAN,
            limit = 3,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        val recos = (result as AppResult.Success).data
        assertTrue(recos.size <= 3, "Should respect limit of 3: got ${recos.size}")
    }

    @Test
    fun `should exclude incompatible regime recipes`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0),
        )

        val recetteVegan = buildRecette(
            id = "r-vegan",
            regimes = listOf(RegimeAlimentaire.VEGAN),
            nutrimentsTotaux = NutrimentValues(fer = 10.0),
        )
        val recetteOmni = buildRecette(
            id = "r-omni",
            regimes = listOf(RegimeAlimentaire.OMNIVORE),
            nutrimentsTotaux = NutrimentValues(fer = 20.0),
        )

        // When — user is VEGAN
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = listOf(recetteVegan, recetteOmni),
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then — only vegan recipe included
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        val recos = (result as AppResult.Success).data
        assertEquals(1, recos.size)
        assertEquals("r-vegan", recos[0].recette.id)
    }

    @Test
    fun `should include nutrimentsCibles for covered deficits`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0),
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 400.0),
        )

        val recette = buildRecette(
            nutrimentsTotaux = NutrimentValues(fer = 12.0, calcium = 200.0),
            nbPortions = 2,
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = listOf(recette),
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        val recos = (result as AppResult.Success).data
        assertEquals(1, recos.size)
        assertTrue(NutrimentType.FER in recos[0].nutrimentsCibles)
        assertTrue(NutrimentType.CALCIUM in recos[0].nutrimentsCibles)
        assertTrue(NutrimentType.FER in recos[0].pourcentageCouverture)
        assertTrue(NutrimentType.CALCIUM in recos[0].pourcentageCouverture)
    }

    @Test
    fun `should handle empty recettes list`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 3.0),
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = emptyList(),
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        assertTrue((result as AppResult.Success).data.isEmpty())
    }

    @Test
    fun `should handle recipe with multiple deficit nutrients`() {
        // Given — deficits in fer and calcium
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 5.0),     // 45%, manque = 6
            buildQuotaStatus(NutrimentType.CALCIUM, 950.0, 500.0), // 52.6%, manque = 450
        )

        val recette = buildRecette(
            nbPortions = 1,
            nutrimentsTotaux = NutrimentValues(
                fer = 6.0,      // covers 100% of fer deficit
                calcium = 225.0, // covers 50% of calcium deficit
            ),
        )

        // When
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = listOf(recette),
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        val recos = (result as AppResult.Success).data
        assertEquals(1, recos.size)
        assertApprox(100.0, recos[0].pourcentageCouverture[NutrimentType.FER]!!, 0.1, "Fer coverage")
        assertApprox(50.0, recos[0].pourcentageCouverture[NutrimentType.CALCIUM]!!, 0.1, "Calcium coverage")
        // Global = (100 + 50) / 2 = 75%
        assertApprox(75.0, recos[0].pourcentageCouvertureGlobal, 0.1, "Global coverage")
    }

    @Test
    fun `should use default limit of 5`() {
        // Given
        val statuses = listOf(
            buildQuotaStatus(NutrimentType.FER, 11.0, 1.0),
        )

        val recettes = (1..10).map { i ->
            buildRecette(
                id = "r$i",
                nom = "Recette $i",
                regimes = listOf(RegimeAlimentaire.VEGAN),
                nutrimentsTotaux = NutrimentValues(fer = (2 + i).toDouble()),
            )
        }

        // When — no explicit limit
        val result = useCase.execute(
            quotaStatuses = statuses,
            recettes = recettes,
            regime = RegimeAlimentaire.VEGAN,
        )

        // Then
        assertIs<AppResult.Success<List<RecommandationRecette>>>(result)
        val recos = (result as AppResult.Success).data
        assertTrue(recos.size <= 5, "Default limit should be 5: got ${recos.size}")
    }
}
