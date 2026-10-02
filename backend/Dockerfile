# Dockerfile multi-stage para el backend de MediTrack Senior.
# Etapa 1: compila con Maven + JDK 21. Etapa 2: runtime minimo JRE 21.

# ---- Etapa de construccion ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Primero el pom para aprovechar la cache de capas de dependencias.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Luego el codigo fuente y el empaquetado (sin pruebas en la imagen).
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Etapa de ejecucion ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root para ejecutar la aplicacion.
RUN addgroup -S app && adduser -S app -G app
USER app

# El jar generado por Spring Boot (nombre fijo segun pom.xml).
COPY --from=build /build/target/meditrack-backend-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la API.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
