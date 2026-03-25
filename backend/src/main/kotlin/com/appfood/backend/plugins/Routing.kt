package com.appfood.backend.plugins

import com.appfood.backend.routes.healthRoutes
import com.appfood.backend.routes.searchRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        healthRoutes()
        searchRoutes()
    }
}
