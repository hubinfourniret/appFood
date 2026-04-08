package com.appfood.backend.routes

import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.InitialConsentRequest
import com.appfood.backend.routes.dto.UpdateConsentRequest
import com.appfood.backend.service.ConsentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.consentRoutes() {
    val consentService by inject<ConsentService>()

    authenticate("auth-jwt") {
        route("/api/v1/consents") {

            // POST /api/v1/consents/initial — BEFORE /{type}
            post("/initial") {
                val userId = call.userId()
                val request = call.receive<InitialConsentRequest>()
                val response = consentService.initialConsents(userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            // GET /api/v1/consents
            get {
                val userId = call.userId()
                val response = consentService.getConsents(userId)
                call.respond(HttpStatusCode.OK, response)
            }

            // PUT /api/v1/consents/{type}
            put("/{type}") {
                val userId = call.userId()
                val type = call.parameters["type"]!!
                val request = call.receive<UpdateConsentRequest>()
                val response = consentService.updateConsent(userId, type, request)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
            }
        }
    }
}
