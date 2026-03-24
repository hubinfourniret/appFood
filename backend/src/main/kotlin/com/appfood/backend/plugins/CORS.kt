package com.appfood.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    val appEnv = environment
    val isDev = appEnv.config.propertyOrNull("ktor.deployment.environment")?.getString() != "production"

    install(CORS) {
        if (isDev) {
            anyHost()
        } else {
            val allowedHosts = appEnv.config
                .propertyOrNull("appfood.cors.allowedHosts")
                ?.getList()
                ?: emptyList()
            allowedHosts.forEach { host ->
                allowHost(host, schemes = listOf("https"))
            }
        }

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        allowCredentials = true
    }
}
