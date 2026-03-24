package com.appfood.backend

import io.ktor.server.application.Application

data class AppConfig(
    val database: DatabaseConfig,
    val meilisearch: MeilisearchConfig,
    val jwt: JwtConfig,
)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
)

data class MeilisearchConfig(
    val url: String,
    val apiKey: String,
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
)

fun Application.loadConfig(): AppConfig {
    val config = environment.config

    return AppConfig(
        database = DatabaseConfig(
            url = config.property("appfood.database.url").getString(),
            user = config.property("appfood.database.user").getString(),
            password = config.property("appfood.database.password").getString(),
        ),
        meilisearch = MeilisearchConfig(
            url = config.property("appfood.meilisearch.url").getString(),
            apiKey = config.property("appfood.meilisearch.apiKey").getString(),
        ),
        jwt = JwtConfig(
            secret = config.property("appfood.jwt.secret").getString(),
            issuer = config.property("appfood.jwt.issuer").getString(),
            audience = config.property("appfood.jwt.audience").getString(),
            realm = config.property("appfood.jwt.realm").getString(),
        ),
    )
}
