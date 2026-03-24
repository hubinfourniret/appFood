package com.appfood.backend.plugins

import com.appfood.backend.routes.healthRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        healthRoutes()
    }
}
