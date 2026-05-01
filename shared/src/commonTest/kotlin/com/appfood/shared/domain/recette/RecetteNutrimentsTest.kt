package com.appfood.shared.domain.recette

import com.appfood.shared.model.Aliment
import com.appfood.shared.model.IngredientRecette
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecetteNutrimentsTest {

    // --- Helpers ---

    private fun buildAliment(
        id: String,
        nom: String = "Aliment $id",
        nutriments: NutrimentValues = NutrimentValues(),
    ): Aliment = Aliment(
        id = id,
        nom = nom,
        marque = null,
        source = SourceAliment.CIQUAL,
        sourceId = null,
        codeBarres = null,
        categorie = "test",
        regimesCompatibles = listOf(RegimeAlimentaire.OMNIVORE),
        nutrimentsPour100g = nutriments,
    )

    private fun buildIngredient(
        alimentId: String,
        quantiteGrammes: Double,
    ): IngredientRecette = IngredientRecette(
        id = "ing-$alimentId",
        alimentId = alimentId,
        alimentNom = "Ingredient $alimentId",
        quantiteGrammes = quantiteGrammes,
    )

    private fun assertApprox(
        expected: Double,
        actual: Double,
        tolerance: Double = 0.001,
        message: String = "",
    ) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "$message: expected ~$expected but was $actual",
        )
    }

    // ==========================================
    // 1. Nutriments totaux = somme(nutrimentsPour100g * quantite / 100)
    // ==========================================

    @Test
    fun `should return zero nutriments for empty ingredients list`() {
        // Given : aucun ingredient
        val ingredients = emptyList<IngredientRecette>()
        val alimentsById = emptyMap<String, Aliment>()

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : tous les nutriments a 0
        assertEquals(0.0, result.calories)
        assertEquals(0.0, result.proteines)
        assertEquals(0.0, result.glucides)
        assertEquals(0.0, result.lipides)
        assertEquals(0.0, result.fibres)
        assertEquals(0.0, result.fer)
        assertEquals(0.0, result.calcium)
        assertEquals(0.0, result.vitamineB12)
        assertEquals(0.0, result.vitamineC)
        assertEquals(0.0, result.vitamineD)
        assertEquals(0.0, result.omega3)
        assertEquals(0.0, result.omega6)
    }

    @Test
    fun `should calculate nutriments for single ingredient at 100g`() {
        // Given : 1 ingredient, 100g de tofu (valeurs pour 100g)
        val tofuNutriments = NutrimentValues(
            calories = 144.0,
            proteines = 15.6,
            glucides = 2.3,
            lipides = 8.7,
            fibres = 1.2,
            sel = 0.01,
            sucres = 0.5,
            fer = 2.7,
            calcium = 350.0,
            zinc = 1.0,
            magnesium = 58.0,
            vitamineB12 = 0.0,
            vitamineD = 0.0,
            vitamineC = 0.0,
            omega3 = 0.6,
            omega6 = 4.3,
        )
        val tofu = buildAliment("tofu-1", "Tofu ferme", tofuNutriments)
        val ingredients = listOf(buildIngredient("tofu-1", 100.0))
        val alimentsById = mapOf("tofu-1" to tofu)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : 100g => ratio = 1.0, nutriments identiques
        assertApprox(144.0, result.calories, message = "Calories")
        assertApprox(15.6, result.proteines, message = "Proteines")
        assertApprox(2.3, result.glucides, message = "Glucides")
        assertApprox(8.7, result.lipides, message = "Lipides")
        assertApprox(1.2, result.fibres, message = "Fibres")
        assertApprox(2.7, result.fer, message = "Fer")
        assertApprox(350.0, result.calcium, message = "Calcium")
        assertApprox(0.6, result.omega3, message = "Omega3")
    }

    @Test
    fun `should scale nutriments proportionally to quantity`() {
        // Given : 1 ingredient a 250g (ratio = 2.5)
        val lentilles = buildAliment(
            "lentilles-1",
            "Lentilles cuites",
            NutrimentValues(
                calories = 116.0,
                proteines = 9.0,
                glucides = 20.0,
                lipides = 0.4,
                fibres = 7.9,
                fer = 3.3,
            ),
        )
        val ingredients = listOf(buildIngredient("lentilles-1", 250.0))
        val alimentsById = mapOf("lentilles-1" to lentilles)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : 250g => ratio = 2.5
        assertApprox(116.0 * 2.5, result.calories, message = "Calories 250g")
        assertApprox(9.0 * 2.5, result.proteines, message = "Proteines 250g")
        assertApprox(20.0 * 2.5, result.glucides, message = "Glucides 250g")
        assertApprox(0.4 * 2.5, result.lipides, message = "Lipides 250g")
        assertApprox(7.9 * 2.5, result.fibres, message = "Fibres 250g")
        assertApprox(3.3 * 2.5, result.fer, message = "Fer 250g")
    }

    @Test
    fun `should sum nutriments from multiple ingredients`() {
        // Given : 3 ingredients pour un bol de salade
        val tofu = buildAliment(
            "tofu-1",
            "Tofu",
            NutrimentValues(calories = 144.0, proteines = 15.6, fer = 2.7),
        )
        val quinoa = buildAliment(
            "quinoa-1",
            "Quinoa cuit",
            NutrimentValues(calories = 120.0, proteines = 4.4, fer = 1.5),
        )
        val epinards = buildAliment(
            "epinards-1",
            "Epinards crus",
            NutrimentValues(calories = 23.0, proteines = 2.9, fer = 2.7),
        )

        val ingredients = listOf(
            buildIngredient("tofu-1", 150.0),    // ratio 1.5
            buildIngredient("quinoa-1", 200.0),  // ratio 2.0
            buildIngredient("epinards-1", 80.0), // ratio 0.8
        )
        val alimentsById = mapOf(
            "tofu-1" to tofu,
            "quinoa-1" to quinoa,
            "epinards-1" to epinards,
        )

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : somme des contributions
        // Calories = 144*1.5 + 120*2.0 + 23*0.8 = 216 + 240 + 18.4 = 474.4
        assertApprox(474.4, result.calories, message = "Calories totales")
        // Proteines = 15.6*1.5 + 4.4*2.0 + 2.9*0.8 = 23.4 + 8.8 + 2.32 = 34.52
        assertApprox(34.52, result.proteines, message = "Proteines totales")
        // Fer = 2.7*1.5 + 1.5*2.0 + 2.7*0.8 = 4.05 + 3.0 + 2.16 = 9.21
        assertApprox(9.21, result.fer, message = "Fer total")
    }

    @Test
    fun `should ignore ingredient when aliment not found in map`() {
        // Given : 2 ingredients, dont 1 absent de la map
        val tofu = buildAliment(
            "tofu-1",
            "Tofu",
            NutrimentValues(calories = 144.0, proteines = 15.6),
        )
        val ingredients = listOf(
            buildIngredient("tofu-1", 100.0),
            buildIngredient("unknown-999", 200.0), // pas dans alimentsById
        )
        val alimentsById = mapOf("tofu-1" to tofu)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : seul le tofu est comptabilise
        assertApprox(144.0, result.calories, message = "Calories sans ingredient inconnu")
        assertApprox(15.6, result.proteines, message = "Proteines sans ingredient inconnu")
    }

    @Test
    fun `should handle zero quantity ingredient`() {
        // Given : ingredient avec quantite 0
        val tofu = buildAliment(
            "tofu-1",
            "Tofu",
            NutrimentValues(calories = 144.0, proteines = 15.6),
        )
        val ingredients = listOf(buildIngredient("tofu-1", 0.0))
        val alimentsById = mapOf("tofu-1" to tofu)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : ratio = 0 => nutriments = 0
        assertEquals(0.0, result.calories)
        assertEquals(0.0, result.proteines)
    }

    @Test
    fun `should handle small fractional quantities`() {
        // Given : 5g d'epice (ratio = 0.05)
        val curcuma = buildAliment(
            "curcuma-1",
            "Curcuma moulu",
            NutrimentValues(calories = 312.0, fer = 55.0),
        )
        val ingredients = listOf(buildIngredient("curcuma-1", 5.0))
        val alimentsById = mapOf("curcuma-1" to curcuma)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)

        // Then : 5g => ratio = 0.05
        assertApprox(312.0 * 0.05, result.calories, message = "Calories 5g curcuma")
        assertApprox(55.0 * 0.05, result.fer, message = "Fer 5g curcuma")
    }

    // ==========================================
    // 2. Nutriments par portion = total / nbPortions
    // ==========================================

    @Test
    fun `should divide total nutriments by number of portions`() {
        // Given
        val totaux = NutrimentValues(
            calories = 800.0,
            proteines = 40.0,
            glucides = 100.0,
            lipides = 30.0,
            fibres = 12.0,
            sel = 2.0,
            sucres = 10.0,
            fer = 8.0,
            calcium = 400.0,
            zinc = 4.0,
            magnesium = 200.0,
            vitamineB12 = 2.0,
            vitamineD = 10.0,
            vitamineC = 60.0,
            omega3 = 3.0,
            omega6 = 6.0,
        )

        // When : 4 portions
        val result = RecetteNutrimentsCalculator.calculerNutrimentsParPortion(totaux, 4)

        // Then
        assertApprox(200.0, result.calories, message = "Calories par portion")
        assertApprox(10.0, result.proteines, message = "Proteines par portion")
        assertApprox(25.0, result.glucides, message = "Glucides par portion")
        assertApprox(7.5, result.lipides, message = "Lipides par portion")
        assertApprox(3.0, result.fibres, message = "Fibres par portion")
        assertApprox(0.5, result.sel, message = "Sel par portion")
        assertApprox(2.5, result.sucres, message = "Sucres par portion")
        assertApprox(2.0, result.fer, message = "Fer par portion")
        assertApprox(100.0, result.calcium, message = "Calcium par portion")
        assertApprox(1.0, result.zinc, message = "Zinc par portion")
        assertApprox(50.0, result.magnesium, message = "Magnesium par portion")
        assertApprox(0.5, result.vitamineB12, message = "Vitamine B12 par portion")
        assertApprox(2.5, result.vitamineD, message = "Vitamine D par portion")
        assertApprox(15.0, result.vitamineC, message = "Vitamine C par portion")
        assertApprox(0.75, result.omega3, message = "Omega3 par portion")
        assertApprox(1.5, result.omega6, message = "Omega6 par portion")
    }

    @Test
    fun `should return total nutriments when nbPortions is 1`() {
        // Given
        val totaux = NutrimentValues(calories = 600.0, proteines = 30.0)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsParPortion(totaux, 1)

        // Then : identique au total
        assertApprox(600.0, result.calories, message = "Calories 1 portion")
        assertApprox(30.0, result.proteines, message = "Proteines 1 portion")
    }

    @Test
    fun `should coerce nbPortions 0 to 1 and return total`() {
        // Given : edge case nbPortions = 0
        val totaux = NutrimentValues(calories = 600.0, proteines = 30.0, fer = 5.0)

        // When : nbPortions = 0 => coerce a 1
        val result = RecetteNutrimentsCalculator.calculerNutrimentsParPortion(totaux, 0)

        // Then : divise par 1 => identique au total
        assertApprox(600.0, result.calories, message = "Calories nbPortions=0")
        assertApprox(30.0, result.proteines, message = "Proteines nbPortions=0")
        assertApprox(5.0, result.fer, message = "Fer nbPortions=0")
    }

    @Test
    fun `should coerce negative nbPortions to 1`() {
        // Given : edge case nbPortions negatif
        val totaux = NutrimentValues(calories = 400.0)

        // When
        val result = RecetteNutrimentsCalculator.calculerNutrimentsParPortion(totaux, -3)

        // Then : coerce a 1
        assertApprox(400.0, result.calories, message = "Calories nbPortions=-3")
    }

    // ==========================================
    // 3. Integration : calcul total + par portion
    // ==========================================

    @Test
    fun `should compute correct per-portion values from ingredients`() {
        // Given : recette a 2 portions avec 2 ingredients
        val riz = buildAliment(
            "riz-1",
            "Riz blanc cuit",
            NutrimentValues(calories = 130.0, proteines = 2.7, glucides = 28.0),
        )
        val legumes = buildAliment(
            "legumes-1",
            "Legumes melanges",
            NutrimentValues(calories = 50.0, proteines = 3.0, glucides = 8.0),
        )

        val ingredients = listOf(
            buildIngredient("riz-1", 300.0),     // ratio 3.0
            buildIngredient("legumes-1", 200.0), // ratio 2.0
        )
        val alimentsById = mapOf("riz-1" to riz, "legumes-1" to legumes)

        // When : calcul total puis par portion
        val totaux = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)
        val parPortion = RecetteNutrimentsCalculator.calculerNutrimentsParPortion(totaux, 2)

        // Then
        // Total calories = 130*3.0 + 50*2.0 = 390 + 100 = 490
        assertApprox(490.0, totaux.calories, message = "Total calories")
        // Par portion = 490 / 2 = 245
        assertApprox(245.0, parPortion.calories, message = "Calories par portion")
        // Total proteines = 2.7*3.0 + 3.0*2.0 = 8.1 + 6.0 = 14.1
        assertApprox(14.1, totaux.proteines, message = "Total proteines")
        // Par portion = 14.1 / 2 = 7.05
        assertApprox(7.05, parPortion.proteines, message = "Proteines par portion")
        // Total glucides = 28*3.0 + 8*2.0 = 84 + 16 = 100
        assertApprox(100.0, totaux.glucides, message = "Total glucides")
        // Par portion = 100 / 2 = 50
        assertApprox(50.0, parPortion.glucides, message = "Glucides par portion")
    }

    @Test
    fun `should handle all nutriments in full recipe scenario`() {
        // Given : recette complete type salade vegan
        val tofu = buildAliment(
            "tofu-1",
            "Tofu",
            NutrimentValues(
                calories = 144.0, proteines = 15.6, glucides = 2.3, lipides = 8.7,
                fibres = 1.2, sel = 0.01, sucres = 0.5, fer = 2.7, calcium = 350.0,
                zinc = 1.0, magnesium = 58.0, vitamineB12 = 0.0, vitamineD = 0.0,
                vitamineC = 0.0, omega3 = 0.6, omega6 = 4.3,
            ),
        )
        val avocat = buildAliment(
            "avocat-1",
            "Avocat",
            NutrimentValues(
                calories = 160.0, proteines = 2.0, glucides = 8.5, lipides = 14.7,
                fibres = 6.7, sel = 0.007, sucres = 0.7, fer = 0.6, calcium = 12.0,
                zinc = 0.6, magnesium = 29.0, vitamineB12 = 0.0, vitamineD = 0.0,
                vitamineC = 10.0, omega3 = 0.1, omega6 = 1.7,
            ),
        )

        val ingredients = listOf(
            buildIngredient("tofu-1", 200.0),   // ratio 2.0
            buildIngredient("avocat-1", 100.0), // ratio 1.0
        )
        val alimentsById = mapOf("tofu-1" to tofu, "avocat-1" to avocat)
        val nbPortions = 2

        // When
        val totaux = RecetteNutrimentsCalculator.calculerNutrimentsTotaux(ingredients, alimentsById)
        val parPortion = RecetteNutrimentsCalculator.calculerNutrimentsParPortion(totaux, nbPortions)

        // Then — totaux
        // Calories = 144*2.0 + 160*1.0 = 448
        assertApprox(448.0, totaux.calories, message = "Total calories")
        // Proteines = 15.6*2.0 + 2.0*1.0 = 33.2
        assertApprox(33.2, totaux.proteines, message = "Total proteines")
        // Fer = 2.7*2.0 + 0.6*1.0 = 6.0
        assertApprox(6.0, totaux.fer, message = "Total fer")
        // Calcium = 350*2.0 + 12*1.0 = 712
        assertApprox(712.0, totaux.calcium, message = "Total calcium")
        // VitamineC = 0*2.0 + 10*1.0 = 10
        assertApprox(10.0, totaux.vitamineC, message = "Total vitamineC")

        // Then — par portion (/ 2)
        assertApprox(224.0, parPortion.calories, message = "Calories par portion")
        assertApprox(16.6, parPortion.proteines, message = "Proteines par portion")
        assertApprox(3.0, parPortion.fer, message = "Fer par portion")
        assertApprox(356.0, parPortion.calcium, message = "Calcium par portion")
    }
}
