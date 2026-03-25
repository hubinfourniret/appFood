# Stage 1 : Build
# Lancer depuis la racine du projet : docker build -f backend/Dockerfile .
FROM gradle:8.10-jdk21 AS build

WORKDIR /app
COPY gradle ./gradle
COPY build.gradle.kts ./build.gradle.kts
COPY settings.gradle.kts ./settings.gradle.kts
COPY gradle.properties ./gradle.properties
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
COPY backend/ ./backend/

RUN gradle :backend:buildFatJar --no-daemon

# Stage 2 : Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/build/libs/*-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
