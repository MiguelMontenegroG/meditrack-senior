# Backend - MediTrack Senior

API REST de MediTrack Senior. Monolito modular con arquitectura hexagonal por
modulo de negocio. Java 21 + Spring Boot 3.4.1 + Maven + PostgreSQL + Flyway.

## Requisitos

- Java 21 (LTS)
- Maven 3.9.x
- Docker Desktop (para PostgreSQL local)

## Levantar la base de datos

Desde la raiz del monorepo (donde esta `docker-compose.yml`):

```powershell
docker compose up -d
```

Esto levanta PostgreSQL 16 como contenedor `meditrack-postgres`, con un volumen
persistente y un healthcheck. Las credenciales se leen del archivo `.env` de la
raiz (no versionado; toma como plantilla `.env.example`).

Para detenerla:

```powershell
docker compose down
```

## Correr el backend

El perfil `dev` es el activo por defecto. Carga las credenciales desde el `.env`
de la raiz mediante `spring.config.import`.

```powershell
cd backend
mvn spring-boot:run
```

Al arrancar, Flyway aplica las migraciones de `src/main/resources/db/migration`.
La API queda en `http://localhost:8080`.

## Ver Swagger (solo en dev)

Con el perfil `dev`:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

En produccion Swagger UI esta deshabilitado.

## Endpoint de salud

```powershell
curl http://localhost:8080/actuator/health
```

Debe responder `{"status":"UP"}`. Es el unico endpoint de Actuator expuesto.

## Estructura de paquetes

Paquete base: `co.edu.uniquindio.meditrack`.

Cada modulo de negocio sigue arquitectura hexagonal:

```
<modulo>/
  domain/                     # modelo y reglas de negocio
  application/                # casos de uso y servicios de aplicacion
  infrastructure/
    web/                      # controladores REST y DTOs de entrada
    persistence/              # repositorios y adaptadores de base de datos
```

Modulos: `usuario`, `paciente`, `medicacion`, `cita`, `bitacora`, `alerta`,
`reporte`, `dashboard`. Paquetes transversales: `config` y `shared`.

## Pruebas

```powershell
mvn clean verify
```

La prueba de contexto (`MeditrackApplicationTests`) excluye las
autoconfiguraciones de base de datos, por lo que no necesita PostgreSQL.
