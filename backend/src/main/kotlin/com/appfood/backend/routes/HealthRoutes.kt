package com.appfood.backend.routes

import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
)

fun Routing.healthRoutes() {
    route("/api") {
        get("/health") {
            call.respond(HealthResponse(status = "ok", version = "1.0.0"))
        }
    }
}
