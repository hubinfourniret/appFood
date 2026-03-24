package com.appfood.backend.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

// Exceptions metier
class NotFoundException(override val message: String = "Ressource non trouvee") : RuntimeException(message)
class UnauthorizedException(override val message: String = "Non authentifie") : RuntimeException(message)
class ForbiddenException(override val message: String = "Acces interdit") : RuntimeException(message)
class BadRequestException(override val message: String = "Requete invalide") : RuntimeException(message)
class ValidationException(override val message: String = "Validation echouee") : RuntimeException(message)
class ConflictException(override val message: String = "Conflit") : RuntimeException(message)

@Serializable
data class ErrorResponse(
    val error: ErrorDetail,
) {
    constructor(status: Int, message: String) : this(
        error = ErrorDetail(
            code = when (status) {
                400 -> "BAD_REQUEST"
                401 -> "UNAUTHORIZED"
                403 -> "FORBIDDEN"
                404 -> "NOT_FOUND"
                409 -> "CONFLICT"
                else -> "INTERNAL_ERROR"
            },
            message = message,
        ),
    )
}

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(error = ErrorDetail("NOT_FOUND", cause.message)),
            )
        }
        exception<UnauthorizedException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(error = ErrorDetail("UNAUTHORIZED", cause.message)),
            )
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(error = ErrorDetail("FORBIDDEN", cause.message)),
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(error = ErrorDetail("BAD_REQUEST", cause.message)),
            )
        }
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(error = ErrorDetail("VALIDATION_ERROR", cause.message)),
            )
        }
        exception<ConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(error = ErrorDetail("CONFLICT", cause.message)),
            )
        }
        exception<Exception> { call, cause ->
            call.application.environment.log.error("Erreur interne non geree", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(error = ErrorDetail("INTERNAL_ERROR", "Erreur interne du serveur")),
            )
        }
    }
}
