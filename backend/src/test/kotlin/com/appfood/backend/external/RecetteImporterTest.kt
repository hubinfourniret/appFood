package com.appfood.backend.external

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.database.tables.AlimentsTable
import com.appfood.backend.database.tables.IngredientsTable
import com.appfood.backend.database.tables.RecettesTable
import com.appfood.backend.database.tables.SourceAliment
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecetteImporterTest {
    private val alimentDao = AlimentDao()
    private val recetteDao = RecetteDao()
    private val importer = RecetteImporter(alimentDao, recetteDao)

    private data class AlimentSeed(
        val sourceId: String,
        val nom: String,
        val calories: Double,
        val proteines: Double,
    )

    private val seeds =
        listOf(
            AlimentSeed("ciq-001", "Tofu nature", 144.0, 15.0),
            AlimentSeed("ciq-002", "Lentilles vertes crues", 320.0, 24.0),
            AlimentSeed("ciq-003", "Riz basmati cru", 355.0, 7.5),
            AlimentSeed("ciq-004", "Pois chiche en conserve", 130.0, 7.0),
            AlimentSeed("ciq-005", "Huile d'olive", 899.0, 0.0),
            AlimentSeed("ciq-006", "Oignon cru", 34.0, 1.2),
            AlimentSeed("ciq-007", "Tomate crue", 18.0, 0.9),
            AlimentSeed("ciq-008", "Banane crue", 94.0, 1.1),
            AlimentSeed("ciq-009", "Avocat cru", 169.0, 2.0),
            AlimentSeed("ciq-010", "Flocons d'avoine", 375.0, 13.0),
        )

    private fun seedAliments() {
        transaction {
            for (seed in seeds) {
                AlimentsTable.insert {
                    it[AlimentsTable.id] = "aliment-${seed.sourceId}"
                    it[AlimentsTable.nom] = seed.nom
                    it[AlimentsTable.marque] = null
                    it[AlimentsTable.sourceAliment] = SourceAliment.CIQUAL
                    it[AlimentsTable.sourceId] = seed.sourceId
                    it[AlimentsTable.codeBarres] = null
                    it[AlimentsTable.categorie] = "Test"
                    it[AlimentsTable.regimesCompatibles] = "VEGAN,VEGETARIEN"
                    it[AlimentsTable.calories] = seed.calories
                    it[AlimentsTable.proteines] = seed.proteines
                    it[AlimentsTable.glucides] = 10.0
                    it[AlimentsTable.lipides] = 5.0
                    it[AlimentsTable.fibres] = 2.0
                    it[AlimentsTable.sel] = 0.1
                    it[AlimentsTable.sucres] = 1.0
                    it[AlimentsTable.fer] = 1.0
                    it[AlimentsTable.calcium] = 30.0
                    it[AlimentsTable.zinc] = 0.5
                    it[AlimentsTable.magnesium] = 20.0
                    it[AlimentsTable.vitamineB12] = 0.0
                    it[AlimentsTable.vitamineD] = 0.0
                    it[AlimentsTable.vitamineC] = 0.0
                    it[AlimentsTable.omega3] = 0.1
                    it[AlimentsTable.omega6] = 0.5
                }
            }
        }
    }

    @BeforeTest
    fun setUp() {
        // Fixed DB name per test class so chacune repartition test efface/recree proprement
        // la meme base, evitant les conflits multi-DB de Database.connect().
        Database.connect(
            url = "jdbc:h2:mem:recette_importer_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
            user = "sa",
            password = "",
        )
        transaction {
            SchemaUtils.drop(IngredientsTable, RecettesTable, AlimentsTable, inBatch = true)
            SchemaUtils.create(AlimentsTable, RecettesTable, IngredientsTable)
        }
        seedAliments()
    }

    @Test
    fun `should import recettes with matched ingredients and compute totals`() =
        runBlocking {
            val json =
                """
                [
                  {
                    "nom": "Test Bol de riz au tofu",
                    "description": "Test recette",
                    "tempsPreparationMin": 10,
                    "tempsCuissonMin": 15,
                    "nbPortions": 2,
                    "regimes": ["VEGAN", "VEGETARIEN"],
                    "typeRepas": ["DEJEUNER"],
                    "imageUrl": null,
                    "ingredients": [
                      { "nom": "Riz basmati cru", "quantiteGrammes": 200.0 },
                      { "nom": "Tofu nature", "quantiteGrammes": 150.0 },
                      { "nom": "Huile d'olive", "quantiteGrammes": 10.0 }
                    ],
                    "etapes": ["Etape 1", "Etape 2"]
                  },
                  {
                    "nom": "Test Salade de lentilles",
                    "description": "Test recette 2",
                    "tempsPreparationMin": 5,
                    "tempsCuissonMin": 25,
                    "nbPortions": 4,
                    "regimes": ["VEGAN"],
                    "typeRepas": ["DEJEUNER", "DINER"],
                    "imageUrl": null,
                    "ingredients": [
                      { "nom": "Lentilles vertes crues", "quantiteGrammes": 200.0 },
                      { "nom": "Tomate crue", "quantiteGrammes": 150.0 },
                      { "nom": "Oignon cru", "quantiteGrammes": 50.0 }
                    ],
                    "etapes": ["Cuire les lentilles", "Melanger"]
                  }
                ]
                """.trimIndent()

            val stream = ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8))
            val result = importer.importAll(stream)

            assertEquals(2, result.insertedCount, "Deux recettes doivent etre inserees")
            assertEquals(0, result.skippedCount)

            // Verify recettes in DB
            val recettes = recetteDao.findAllPublished()
            assertEquals(2, recettes.size)

            val bolRiz = recettes.first { it.nom == "Test Bol de riz au tofu" }
            assertTrue(bolRiz.publie)
            assertTrue(bolRiz.calories > 0, "Les calories doivent etre calculees")
            assertEquals("VEGAN,VEGETARIEN", bolRiz.regimesCompatibles)
            assertEquals("DEJEUNER", bolRiz.typeRepas)
            assertTrue(bolRiz.etapes.contains("|||"), "Etapes doivent etre jointes avec |||")

            // Sanity check calculation: riz 355 kcal/100g * 200g = 710, tofu 144 * 1.5 = 216,
            // huile 899 * 0.1 = 89.9 → total env 1016 kcal
            assertTrue(
                bolRiz.calories in 900.0..1100.0,
                "Calories attendues environ 1016, obtenu ${bolRiz.calories}",
            )

            // Ingredients lies correctement
            val ingredientsBol = recetteDao.findIngredientsByRecetteId(bolRiz.id)
            assertEquals(3, ingredientsBol.size)
            assertTrue(ingredientsBol.all { it.alimentId.isNotBlank() })
            assertTrue(ingredientsBol.any { it.alimentNom == "Riz basmati cru" })
        }

    @Test
    fun `should log warnings and skip ingredients with no match`() =
        runBlocking {
            val json =
                """
                [
                  {
                    "nom": "Test Recette avec ingredients exotiques",
                    "description": "Test",
                    "tempsPreparationMin": 5,
                    "tempsCuissonMin": 5,
                    "nbPortions": 1,
                    "regimes": ["VEGAN"],
                    "typeRepas": ["COLLATION"],
                    "imageUrl": null,
                    "ingredients": [
                      { "nom": "Banane crue", "quantiteGrammes": 100.0 },
                      { "nom": "Xyzzy impossible fictif", "quantiteGrammes": 50.0 }
                    ],
                    "etapes": ["Etape unique"]
                  }
                ]
                """.trimIndent()

            val stream = ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8))
            val result = importer.importAll(stream)

            assertEquals(1, result.insertedCount, "La recette doit etre inseree malgre l'ingredient non matche")
            assertTrue(
                result.warnings.any { it.contains("Xyzzy") },
                "Un warning doit mentionner l'ingredient non matche",
            )

            // Seul l'ingredient matche (banane) doit etre insere
            val recettes = recetteDao.findAllPublished()
            val recette = recettes.first { it.nom == "Test Recette avec ingredients exotiques" }
            val ingredients = recetteDao.findIngredientsByRecetteId(recette.id)
            assertEquals(1, ingredients.size)
            assertEquals("Banane crue", ingredients.first().alimentNom)
            assertTrue(recette.calories > 0)
        }

    @Test
    fun `should be idempotent and skip already imported recettes`() =
        runBlocking {
            val json =
                """
                [
                  {
                    "nom": "Test Recette unique",
                    "description": "Test",
                    "tempsPreparationMin": 5,
                    "tempsCuissonMin": 5,
                    "nbPortions": 1,
                    "regimes": ["VEGAN"],
                    "typeRepas": ["COLLATION"],
                    "imageUrl": null,
                    "ingredients": [
                      { "nom": "Banane crue", "quantiteGrammes": 100.0 }
                    ],
                    "etapes": ["Eplucher"]
                  }
                ]
                """.trimIndent()

            // First run → 1 inserted
            val result1 = importer.importAll(ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8)))
            assertEquals(1, result1.insertedCount)
            assertEquals(0, result1.skippedCount)

            // Second run → 0 inserted, 1 skipped
            val result2 = importer.importAll(ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8)))
            assertEquals(0, result2.insertedCount)
            assertEquals(1, result2.skippedCount)

            // Toujours une seule recette en base
            val recettes = recetteDao.findAllPublished()
            assertEquals(1, recettes.count { it.nom == "Test Recette unique" })

            // Pas de doublons d'ingredients
            val totalIngredients = transaction { IngredientsTable.selectAll().count() }
            assertEquals(1L, totalIngredients)
        }

    @Test
    fun `fuzzy matching should handle accents and stopwords`() {
        val aliments =
            seeds.map { seed ->
                AlimentRow(
                    id = "id-${seed.sourceId}",
                    nom = seed.nom,
                    marque = null,
                    source = SourceAliment.CIQUAL,
                    sourceId = seed.sourceId,
                    codeBarres = null,
                    categorie = "Test",
                    regimesCompatibles = "VEGAN",
                    calories = seed.calories,
                    proteines = seed.proteines,
                    glucides = 0.0, lipides = 0.0, fibres = 0.0, sel = 0.0, sucres = 0.0,
                    fer = 0.0, calcium = 0.0, zinc = 0.0, magnesium = 0.0,
                    vitamineB12 = 0.0, vitamineD = 0.0, vitamineC = 0.0,
                    omega3 = 0.0, omega6 = 0.0,
                )
            }
        val normalized = aliments.map { it to it.nom.lowercase() }

        // Accents
        val match1 = importer.findBestMatch("Tomaté crué", normalized)
        assertNotNull(match1)
        assertEquals("Tomate crue", match1.nom)

        // Stopwords ("de") ignores
        val match2 = importer.findBestMatch("Huile olive vierge", normalized)
        assertNotNull(match2)
        assertEquals("Huile d'olive", match2.nom)
    }
}
