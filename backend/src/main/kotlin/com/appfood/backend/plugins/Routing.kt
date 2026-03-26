package com.appfood.backend.plugins

import com.appfood.backend.routes.alimentRoutes
import com.appfood.backend.routes.authRoutes
import com.appfood.backend.routes.healthRoutes
import com.appfood.backend.routes.portionRoutes
import com.appfood.backend.routes.userRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        healthRoutes()
        authRoutes()
        userRoutes()
        alimentRoutes()
        portionRoutes()
    }
}
