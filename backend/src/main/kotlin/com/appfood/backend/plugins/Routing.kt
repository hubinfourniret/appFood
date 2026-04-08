package com.appfood.backend.plugins

import com.appfood.backend.routes.alimentRoutes
import com.appfood.backend.routes.authRoutes
import com.appfood.backend.routes.dashboardRoutes
import com.appfood.backend.routes.healthRoutes
import com.appfood.backend.routes.hydratationRoutes
import com.appfood.backend.routes.journalRoutes
import com.appfood.backend.routes.consentRoutes
import com.appfood.backend.routes.notificationRoutes
import com.appfood.backend.routes.poidsRoutes
import com.appfood.backend.routes.portionRoutes
import com.appfood.backend.routes.quotaRoutes
import com.appfood.backend.routes.recetteRoutes
import com.appfood.backend.routes.recommandationRoutes
import com.appfood.backend.routes.supportRoutes
import com.appfood.backend.routes.userRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        healthRoutes()
        authRoutes()
        userRoutes()
        alimentRoutes()
        journalRoutes()
        quotaRoutes()
        portionRoutes()
        recommandationRoutes()
        dashboardRoutes()
        recetteRoutes()
        hydratationRoutes()
        poidsRoutes()
        notificationRoutes()
        consentRoutes()
        supportRoutes()
    }
}
