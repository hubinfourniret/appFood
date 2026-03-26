# Stage 1 : Build
# Lancer depuis la racine du projet : docker build -f backend/Dockerfile .
FROM gradle:8.10-jdk21 AS build

WORKDIR /app

# Copier les fichiers de configuration Gradle en premier (meilleur cache Docker)
COPY gradle.properties ./gradle.properties
COPY backend/settings-docker.gradle.kts ./settings.gradle.kts
COPY build.gradle.kts ./build.gradle.kts
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml

# Copier le build du backend
COPY backend/build.gradle.kts ./backend/build.gradle.kts

# Telecharger les dependances (couche cachee si build.gradle.kts ne change pas)
RUN gradle :backend:dependencies --no-daemon

# Copier le code source
COPY backend/src ./backend/src

# Build le fat JAR
RUN gradle :backend:buildFatJar --no-daemon

# Stage 2 : Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/build/libs/*-all.jar app.jar

EXPOSE 8080

# Health check integre
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
