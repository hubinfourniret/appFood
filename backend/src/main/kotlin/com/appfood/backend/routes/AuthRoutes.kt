package com.appfood.backend.routes

import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.AuthResponse
import com.appfood.backend.routes.dto.LoginRequest
import com.appfood.backend.routes.dto.RegisterRequest
import com.appfood.backend.routes.dto.UserResponse
import com.appfood.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result =
                authService.register(
                    firebaseToken = request.firebaseToken,
                    email = request.email,
                    nom = request.nom,
                    prenom = request.prenom,
                )
            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    token = result.token,
                    user = result.user.toUserResponse(onboardingComplete = false),
                ),
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val loginResult = authService.login(firebaseToken = request.firebaseToken)
            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    token = loginResult.token,
                    user =
                        loginResult.user.toUserResponse(
                            onboardingComplete = loginResult.onboardingComplete,
                        ),
                ),
            )
        }

        authenticate("auth-jwt") {
            delete("/account") {
                val userId = call.userId()
                authService.deleteAccount(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

internal fun com.appfood.backend.database.dao.UserRow.toUserResponse(onboardingComplete: Boolean) =
    UserResponse(
        id = id,
        email = email,
        nom = nom,
        prenom = prenom,
        onboardingComplete = onboardingComplete,
        createdAt = createdAt.toString(),
    )
