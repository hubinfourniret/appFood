package com.appfood.backend.routes

import com.appfood.backend.database.dao.UserProfileDao
import com.appfood.backend.plugins.userId
import com.appfood.backend.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

// --- Request DTOs ---

@Serializable
data class RegisterRequest(
    val firebaseToken: String,
    val email: String,
    val nom: String? = null,
    val prenom: String? = null,
)

@Serializable
data class LoginRequest(
    val firebaseToken: String,
)

// --- Response DTOs ---

@Serializable
data class AuthResponse(
    val user: UserResponse,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val onboardingComplete: Boolean,
    val createdAt: String,
)

@Serializable
data class ApiResponse<T>(
    val data: T,
)

fun Route.authRoutes() {
    val authService by inject<AuthService>()
    val userProfileDao by inject<UserProfileDao>()

    route("/api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val user = authService.register(
                firebaseToken = request.firebaseToken,
                email = request.email,
                nom = request.nom,
                prenom = request.prenom,
            )
            call.respond(
                HttpStatusCode.Created,
                ApiResponse(
                    data = AuthResponse(
                        user = user.toUserResponse(onboardingComplete = false),
                    ),
                ),
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = authService.login(firebaseToken = request.firebaseToken)
            val profile = userProfileDao.findByUserId(user.id)
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    data = AuthResponse(
                        user = user.toUserResponse(
                            onboardingComplete = profile?.onboardingComplete ?: false,
                        ),
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

internal fun com.appfood.backend.database.dao.UserRow.toUserResponse(
    onboardingComplete: Boolean,
) = UserResponse(
    id = id,
    email = email,
    nom = nom,
    prenom = prenom,
    onboardingComplete = onboardingComplete,
    createdAt = createdAt.toString(),
)
