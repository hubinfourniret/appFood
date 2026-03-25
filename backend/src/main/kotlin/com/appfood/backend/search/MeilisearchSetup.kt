package com.appfood.backend.search

import io.ktor.server.application.Application
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("MeilisearchSetup")

suspend fun Application.configureMeilisearch(client: MeilisearchClient) {
    logger.info("Configuration de Meilisearch...")

    // Creation de l'index aliments
    try {
        client.createIndex(AlimentIndexer.INDEX_NAME, "id")
        logger.info("Index '${AlimentIndexer.INDEX_NAME}' cree")
    } catch (e: Exception) {
        logger.info("Index '${AlimentIndexer.INDEX_NAME}' existe deja ou erreur: ${e.message}")
    }

    // Configuration des settings
    val settings = buildJsonObject {
        put("searchableAttributes", JsonArray(listOf(
            JsonPrimitive("nom"),
            JsonPrimitive("categorie"),
            JsonPrimitive("marque"),
        )))
        put("filterableAttributes", JsonArray(listOf(
            JsonPrimitive("regimesCompatibles"),
            JsonPrimitive("categorie"),
            JsonPrimitive("source"),
        )))
        put("sortableAttributes", JsonArray(listOf(
            JsonPrimitive("nom"),
        )))
        put("stopWords", JsonArray(listOf(
            "de", "du", "des", "le", "la", "les", "au", "aux",
            "un", "une", "en", "et",
        ).map { JsonPrimitive(it) }))
        put("synonyms", buildJsonObject {
            put("tomate", JsonArray(listOf(JsonPrimitive("tomates"))))
            put("pomme", JsonArray(listOf(JsonPrimitive("pommes"))))
            put("carotte", JsonArray(listOf(JsonPrimitive("carottes"))))
            put("haricot", JsonArray(listOf(JsonPrimitive("haricots"))))
            put("lentille", JsonArray(listOf(JsonPrimitive("lentilles"))))
            put("pois chiche", JsonArray(listOf(JsonPrimitive("pois chiches"))))
            put("noix", JsonArray(listOf(JsonPrimitive("noix de grenoble"))))
            put("riz", JsonArray(listOf(
                JsonPrimitive("riz blanc"),
                JsonPrimitive("riz complet"),
                JsonPrimitive("riz basmati"),
            )))
        })
    }

    try {
        client.updateSettings(AlimentIndexer.INDEX_NAME, settings)
        logger.info("Settings Meilisearch configures (searchable, filterable, synonymes, stop words)")
    } catch (e: Exception) {
        logger.error("Erreur configuration Meilisearch: ${e.message}")
    }
}
