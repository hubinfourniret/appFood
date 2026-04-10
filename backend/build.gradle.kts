plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("com.appfood.backend.ApplicationKt")
}

// Align with shared module JVM target — shared is compiled with Java 21 via toolchain
kotlin {
    jvmToolchain(21)
}

// Force kotlinx-datetime 0.6.2 — Exposed 0.61.0 requires real classes (not typealiases).
// Without this, Gradle resolves to 0.7.1 (via shared module) where Instant/Clock become
// typealiases to kotlin.time.*, breaking Exposed column type compatibility.
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
        force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.2")
    }
}

dependencies {
    // Shared KMP module (JVM target — quota calculation, models)
    implementation(project(":shared"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.rateLimit)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlinDatetime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.hikaricp)

    // Search (Meilisearch via Ktor Client HTTP — pas de SDK Java)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.contentNegotiation)

    // DI
    implementation(libs.koin.ktor)

    // Logging
    implementation(libs.logback.classic)

    // Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.h2)
}

// Force sequential test execution — H2 in-memory databases share the Exposed
// global connection, so parallel tests cause data isolation failures.
tasks.withType<Test> {
    maxParallelForks = 1
}
