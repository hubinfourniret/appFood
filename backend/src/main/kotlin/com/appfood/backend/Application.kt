package com.appfood.backend

import com.appfood.backend.database.configureDatabase
import com.appfood.backend.di.backendModule
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

    install(Koin) {
        modules(
            backendModule(
                meilisearchUrl = meilisearchUrl,
                meilisearchApiKey = meilisearchApiKey,
                jwtSecret = jwtSecret,
                jwtIssuer = jwtIssuer,
                jwtAudience = jwtAudience,
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
    launch {
        try {
            configureMeilisearch(get<MeilisearchClient>())
        } catch (e: Exception) {
            environment.log.warn("Meilisearch non disponible au demarrage: ${e.message}")
        }
    }
}
