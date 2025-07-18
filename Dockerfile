# Multi-stage build für optimale Image-Größe
FROM maven:3.9-eclipse-temurin-17 AS build

# Arbeitsverzeichnis setzen
WORKDIR /app

# Kopiere pom.xml und Maven Wrapper
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Dependencies herunterladen (für besseres Caching)
RUN ./mvnw dependency:go-offline -B

# Quellcode kopieren
COPY src src

# Anwendung bauen
RUN ./mvnw clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine

# Arbeitsverzeichnis setzen
WORKDIR /app

# JAR-Datei aus Build-Stage kopieren
COPY --from=build /app/target/musiccast-controller-*.jar app.jar

# Port freigeben
EXPOSE 8080

# Healthcheck hinzufügen
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar"]
