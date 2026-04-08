// settings-docker.gradle.kts
// Version simplifiee pour le build Docker : backend + shared (JVM target)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "appFood"
include(":backend")
include(":shared")
