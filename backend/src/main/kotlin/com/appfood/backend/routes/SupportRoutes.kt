package com.appfood.backend.routes

import com.appfood.backend.service.SupportService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.supportRoutes() {
    val supportService by inject<SupportService>()

    // No authentication required for FAQ
    route("/api/v1/support") {

        // GET /api/v1/support/faq
        get("/faq") {
            val response = supportService.getFaq()
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
