package com.appfood.backend

import com.appfood.backend.database.configureDatabase
import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.RecetteDao
import com.appfood.backend.di.backendModule
import com.appfood.backend.external.CiqualImporter
import com.appfood.backend.external.RecetteImporter
import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.plugins.configureAuth
import com.appfood.backend.plugins.configureCORS
import com.appfood.backend.plugins.configureRouting
import com.appfood.backend.plugins.configureSerialization
import com.appfood.backend.plugins.configureStatusPages
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.search.configureMeilisearch
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config = environment.config
    val meilisearchUrl = config.property("appfood.meilisearch.url").getString()
    val meilisearchApiKey = config.property("appfood.meilisearch.apiKey").getString()
    environment.log.info("=== CONFIG DEBUG ===")
    environment.log.info("Meilisearch URL configuree: $meilisearchUrl")
    environment.log.info("Meilisearch API Key presente: ${meilisearchApiKey.isNotBlank()}")
    environment.log.info("=== FIN CONFIG DEBUG ===")

    val jwtSecret = config.property("appfood.jwt.secret").getString()
    val jwtIssuer = config.property("appfood.jwt.issuer").getString()
    val jwtAudience = config.property("appfood.jwt.audience").getString()

    val encryptionKey = config.propertyOrNull("appfood.encryption.key")?.getString()
    if (encryptionKey.isNullOrBlank()) {
        environment.log.warn(
            "ENCRYPTION_KEY non configuree — les donnees sensibles seront stockees en clair. " +
                "Configurez la variable d'environnement ENCRYPTION_KEY (32 bytes base64) pour la production.",
        )
    }

    install(Koin) {
        modules(
            backendModule(
                meilisearchUrl = meilisearchUrl,
                meilisearchApiKey = meilisearchApiKey,
                jwtSecret = jwtSecret,
                jwtIssuer = jwtIssuer,
                jwtAudience = jwtAudience,
                encryptionKeyBase64 = encryptionKey,
            ),
        )
    }

    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureAuth()
    configureDatabase()
    configureRouting()

    // Init Meilisearch (index + settings) au demarrage
    // Puis import Ciqual si la base d'aliments est vide
    launch {
        try {
            configureMeilisearch(get<MeilisearchClient>())
        } catch (e: Exception) {
            environment.log.warn("Meilisearch non disponible au demarrage: ${e.message}")
        }

        // Auto-import Ciqual si aucun aliment en base
        try {
            val alimentDao = get<AlimentDao>()
            val count = alimentDao.count()
            if (count == 0L) {
                environment.log.info("Base d'aliments vide — lancement de l'import Ciqual...")
                val ciqualImporter = get<CiqualImporter>()
                val csvStream = this@module.javaClass.classLoader.getResourceAsStream("data/ciqual.csv")
                    ?: java.io.File("data/ciqual.csv").takeIf { it.exists() }?.inputStream()
                if (csvStream != null) {
                    val result = ciqualImporter.importAndIndex(csvStream)
                    environment.log.info("Import Ciqual termine: ${result.rows.size} aliments importes")
                } else {
                    environment.log.warn("Fichier ciqual.csv introuvable — import ignore")
                }
            } else {
                environment.log.info("Base d'aliments: $count aliments en PostgreSQL")
                // Verifier que Meilisearch est aussi rempli — si l'index est vide,
                // reindexer depuis PostgreSQL (cas: crash Meilisearch apres import DB)
                try {
                    val meilisearch = get<MeilisearchClient>()
                    val testSearch = meilisearch.search("aliments", com.appfood.backend.search.SearchQuery(q = "a", limit = 1))
                    if (testSearch.estimatedTotalHits == 0) {
                        environment.log.info("Index Meilisearch vide — reindexation depuis PostgreSQL...")
                        val indexer = get<AlimentIndexer>()
                        indexer.indexAll()
                    } else {
                        environment.log.info("Meilisearch OK: ${testSearch.estimatedTotalHits} aliments indexes")
                    }
                } catch (e: Exception) {
                    environment.log.warn("Impossible de verifier/reindexer Meilisearch: ${e.message}")
                }
            }
        } catch (e: Exception) {
            environment.log.error("Erreur lors de l'import Ciqual: ${e.message}", e)
        }

        // Auto-import des recettes curees si la base de recettes est (quasi-)vide
        try {
            val recetteDao = get<RecetteDao>()
            val recettesCount = recetteDao.count()
            if (recettesCount < 10L) {
                environment.log.info(
                    "Base de recettes quasi-vide ($recettesCount recettes) — lancement de l'import des recettes...",
                )
                val recetteImporter = get<RecetteImporter>()
                val jsonStream = this@module.javaClass.classLoader.getResourceAsStream("data/recettes-initial.json")
                    ?: java.io.File("data/recettes-initial.json").takeIf { it.exists() }?.inputStream()
                if (jsonStream != null) {
                    val result = jsonStream.use { recetteImporter.importAll(it) }
                    environment.log.info(
                        "Import recettes termine : ${result.insertedCount} recettes importees, " +
                            "${result.skippedCount} ignorees, ${result.warnings.size} warnings",
                    )
                } else {
                    environment.log.warn("Fichier recettes-initial.json introuvable — import recettes ignore")
                }
            } else {
                environment.log.info("Base de recettes : $recettesCount recettes en PostgreSQL — import ignore")
            }
        } catch (e: Exception) {
            environment.log.error("Erreur lors de l'import des recettes: ${e.message}", e)
        }
    }
}
