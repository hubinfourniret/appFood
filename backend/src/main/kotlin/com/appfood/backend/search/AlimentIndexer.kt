package com.appfood.backend.search

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

class AlimentIndexer(
    private val meilisearchClient: MeilisearchClient,
    private val alimentDao: AlimentDao,
) {
    private val logger = LoggerFactory.getLogger(AlimentIndexer::class.java)

    companion object {
        const val INDEX_NAME = "aliments"
        const val BATCH_SIZE = 500
    }

    suspend fun indexAll() {
        val total = alimentDao.count()
        logger.info("Indexation de $total aliments dans Meilisearch...")

        var offset = 0L
        var indexed = 0L
        while (offset < total) {
            val batch = alimentDao.findAll(limit = BATCH_SIZE, offset = offset)
            if (batch.isEmpty()) break

            val documents = JsonArray(batch.map { it.toDocument() })
            meilisearchClient.addDocuments(INDEX_NAME, documents)

            indexed += batch.size
            offset += BATCH_SIZE
            logger.info("Indexe $indexed / $total aliments")
        }

        logger.info("Indexation terminee : $indexed aliments indexes")
    }

    suspend fun indexBatch(rows: List<AlimentRow>) {
        if (rows.isEmpty()) return
        val documents = JsonArray(rows.map { it.toDocument() })
        meilisearchClient.addDocuments(INDEX_NAME, documents)
    }

    private fun AlimentRow.toDocument() = buildJsonObject {
        put("id", id)
        put("nom", nom)
        put("categorie", categorie)
        put("marque", marque ?: "")
        put("source", source.name)
        put("sourceId", sourceId ?: "")
        put("regimesCompatibles", parseJsonArray(regimesCompatibles))
        put("calories", calories)
        put("proteines", proteines)
        put("glucides", glucides)
        put("lipides", lipides)
        put("fibres", fibres)
        put("fer", fer)
        put("calcium", calcium)
        put("zinc", zinc)
        put("magnesium", magnesium)
        put("vitamine_b12", vitamineB12)
        put("vitamine_d", vitamineD)
        put("vitamine_c", vitamineC)
        put("omega_3", omega3)
        put("omega_6", omega6)
    }

    private fun parseJsonArray(jsonString: String): JsonArray {
        return try {
            Json.parseToJsonElement(jsonString).jsonArray
        } catch (_: Exception) {
            JsonArray(emptyList())
        }
    }
}
