// settings-docker.gradle.kts
// Version simplifiee pour le build Docker : n'inclut que :backend
// (evite les erreurs Gradle liees aux modules :shared et :androidApp absents)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
