package com.appfood.backend.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

/**
 * Configure l'authentification JWT.
 * Les parametres sont lus depuis la section appfood.jwt du fichier HOCON.
 */
fun Application.configureAuth() {
    val config = environment.config
    val jwtSecret = config.property("appfood.jwt.secret").getString()
    val jwtIssuer = config.property("appfood.jwt.issuer").getString()
    val jwtAudience = config.property("appfood.jwt.audience").getString()
    val jwtRealm = config.property("appfood.jwt.realm").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.subject
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(status = 401, message = "Token invalide ou expire"),
                )
            }
        }
    }
}

/**
 * Extrait le userId depuis le token JWT de l'appel courant.
 * Lance UnauthorizedException si le token est absent ou invalide.
 */
fun ApplicationCall.userId(): String {
    return principal<JWTPrincipal>()
        ?.payload
        ?.subject
        ?: throw UnauthorizedException("Token invalide")
}
