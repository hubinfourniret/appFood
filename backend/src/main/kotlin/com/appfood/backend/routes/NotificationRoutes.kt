package com.appfood.backend.routes

import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.RegisterFcmTokenRequest
import com.appfood.backend.service.NotificationService
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

fun Route.notificationRoutes() {
    val notificationService by inject<NotificationService>()

    authenticate("auth-jwt") {
        route("/api/v1/notifications") {
            // Routes specifiques AVANT les routes parametrees

            // POST /api/v1/notifications/read-all
            post("/read-all") {
                val userId = call.userId()
                val response = notificationService.markAllAsRead(userId)
                call.respond(HttpStatusCode.OK, response)
            }

            // POST /api/v1/notifications/register-token
            post("/register-token") {
                val userId = call.userId()
                val request = call.receive<RegisterFcmTokenRequest>()
                val response = notificationService.registerToken(userId, request)
                call.respond(HttpStatusCode.OK, response)
            }

            // PUT /api/v1/notifications/{id}/read
            put("/{id}/read") {
                val userId = call.userId()
                val notificationId = call.parameters["id"]!!
                val response = notificationService.markAsRead(userId, notificationId)
                call.respond(HttpStatusCode.OK, response)
            }

            // GET /api/v1/notifications?page={page}&size={size}&nonLuesUniquement={bool}
            get {
                val userId = call.userId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val nonLuesUniquement = call.request.queryParameters["nonLuesUniquement"]?.toBoolean() ?: false

                val response = notificationService.list(userId, page, size, nonLuesUniquement)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
