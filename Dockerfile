# Multi-stage build für optimale Image-Größe und Multi-Arch Support
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS build

# Build-Argumente für Multi-Arch
ARG BUILDPLATFORM
ARG TARGETPLATFORM
ARG TARGETOS
ARG TARGETARCH

# Arbeitsverzeichnis setzen
WORKDIR /app

# Kopiere pom.xml und Maven Wrapper
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Maven Wrapper ausführbar machen
RUN chmod +x ./mvnw

# Dependencies herunterladen (für besseres Caching)
RUN ./mvnw dependency:go-offline -B

# Quellcode kopieren
COPY src src

# Anwendung bauen
RUN ./mvnw clean package -DskipTests

# Runtime Stage - Multi-Arch Base Image
FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jre-alpine

# Build-Argumente für Runtime Stage
ARG TARGETPLATFORM
ARG TARGETOS
ARG TARGETARCH

# Arbeitsverzeichnis setzen
WORKDIR /app

# Curl für Healthcheck installieren (funktioniert besser als wget auf allen Architekturen)
RUN apk add --no-cache curl

# JAR-Datei aus Build-Stage kopieren
COPY --from=build /app/target/musiccast-controller-*.jar app.jar

# Port freigeben
EXPOSE 8080

# Healthcheck hinzufügen (mit curl statt wget für bessere Multi-Arch Kompatibilität)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar"]
