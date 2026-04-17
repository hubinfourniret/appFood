package com.appfood.backend.external

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.search.MeilisearchClient
import io.ktor.client.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class CiqualImporterParseTest {

    @Test
    fun `parse banane plantain crue should have calories 132 kcal`() {
        val csvFile = File("src/main/resources/data/ciqual.csv")
        assertTrue(csvFile.exists(), "ciqual.csv missing at ${csvFile.absolutePath}")

        val dummyMeili = MeilisearchClient(HttpClient(), "http://nowhere", "")
        val dummyIndexer = AlimentIndexer(dummyMeili, AlimentDao())
        val importer = CiqualImporter(AlimentDao(), dummyIndexer)

        val result = importer.parse(csvFile.absolutePath)

        val banane = result.rows.firstOrNull { it.nom.contains("Banane plantain, crue", ignoreCase = true) }
        assertTrue(banane != null, "Banane plantain, crue not found")
        println("=== DEBUG banane: calories=${banane!!.calories}, proteines=${banane.proteines}, glucides=${banane.glucides}, lipides=${banane.lipides}")

        val zeroCalCount = result.rows.count { it.calories == 0.0 }
        val total = result.rows.size
        println("=== DEBUG: $zeroCalCount / $total aliments with calories == 0.0 (${zeroCalCount * 100 / total}%)")

        assertTrue(banane.proteines > 0.0, "proteines should be > 0")
        assertEquals(132.0, banane.calories, 1.0, "Banane plantain should be 132 kcal")
    }
}
