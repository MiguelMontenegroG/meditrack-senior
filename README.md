# MediTrack Senior

Aplicación web responsive para la gestión clínica de centros de atención
geriátrica: residentes (adultos mayores), esquemas de medicación y registro de
tomas, citas médicas, bitácora de signos vitales, alertas automáticas y reportes
clínicos en PDF.

Proyecto del curso **Ingeniería de Software III** — Universidad del Quindío.

> El sistema trata datos personales de salud (Ley 1581 de 2012): aplica control de
> acceso por rol, minimización de datos y auditoría de accesos.

---

## Índice

1. [Qué hace el sistema](#1-qué-hace-el-sistema)
2. [Arquitectura](#2-arquitectura)
3. [Tecnologías](#3-tecnologías)
4. [Estructura del repositorio](#4-estructura-del-repositorio)
5. [Puesta en marcha (local)](#5-puesta-en-marcha-local)
6. [Variables de entorno](#6-variables-de-entorno)
7. [API REST](#7-api-rest)
8. [Modelo de datos](#8-modelo-de-datos)
9. [Seguridad](#9-seguridad)
10. [Pruebas](#10-pruebas)
11. [CI/CD y calidad](#11-cicd-y-calidad)
12. [Estado del proyecto](#12-estado-del-proyecto)
13. [Documentación](#13-documentación)
14. [Equipo](#14-equipo)

---

## 1. Qué hace el sistema

### Procesos de negocio

| Proceso | Contenido | Estado |
| --- | --- | --- |
| **Proceso 1** | Perfiles de residentes y esquemas de medicación (catálogo, horarios y registro de tomas). | Implementado |
| **Proceso 2** | Citas médicas y bitácora de signos vitales (presión arterial, glucosa, temperatura). | Implementado |
| **Proceso 3** | Alertas automáticas, panel de indicadores y reportes clínicos en PDF. | Implementado |

### Funcionalidades por módulo

- **Autenticación y usuarios** — login con JWT, consulta del usuario actual, cambio
  de contraseña propia y gestión de usuarios (solo administrador). Borrado lógico.
- **Residentes** — alta, listado con búsqueda y paginación, detalle, edición y
  cambio de estado con motivo. Contactos de emergencia, vinculación de familiares y
  asignación de cuidadores.
- **Medicación** — catálogo de medicamentos, esquemas por residente con horarios y
  agenda del día con estado derivado (pendiente / atrasada / administrada /
  omitida) más historial de tomas.
- **Citas** — calendario de citas, agendamiento, edición y cambio de estado
  (incluye `INCUMPLIDA`). Catálogo de especialidades.
- **Bitácora** — registro y consulta de signos vitales por residente.
- **Alertas** — generación automática por un proceso programado (tomas atrasadas,
  citas próximas no atendidas y umbrales de signos vitales de referencia) y
  atención manual de la alerta.
- **Reportes** — generación de reportes clínicos en PDF e historial de reportes
  generados (solo metadatos).

### Roles

| Rol | Alcance |
| --- | --- |
| **ADMINISTRADOR** | Acceso completo: gestiona usuarios, residentes, medicamentos, esquemas y citas. Ve todos los residentes. |
| **CUIDADOR_ENFERMERO** | Registra tomas, signos vitales y atiende tareas del día. Solo ve los residentes con asignación vigente. |
| **FAMILIAR_AUTORIZADO** | **Solo lectura.** Solo ve los residentes con vinculación autorizada. Nunca debe ver acciones de escritura. |

El **alcance por rol** se resuelve en el backend: un residente fuera de alcance
responde `404` (no `403`) para no revelar su existencia, y el intento queda
auditado.

### Fuera de alcance

IoT, telemedicina, facturación e interoperabilidad con RIPS/EPS. El sistema
modela **un solo centro geriátrico**.

---

## 2. Arquitectura

**Cliente-servidor:** SPA en el navegador + API REST bajo `/api` con JWT.

```
Navegador
  └── SPA (Vite + React + TS)      Azure Static Web Apps
        │  HTTP/JSON + Authorization: Bearer <JWT>
        ▼
  API REST /api                     Azure App Service
        │
        ├── Filtro JWT → SecurityFilterChain → @PreAuthorize
        ├── Controller → Caso de uso → Puerto → Adaptador
        └── PostgreSQL 16 (esquema gobernado por Flyway)
```

### Backend: monolito modular + hexagonal por módulo

Paquete base `co.edu.uniquindio.meditrack`. Cada módulo de dominio repite el
mismo patrón:

```
<modulo>/
  domain/                      Modelo y reglas puras (sin Spring ni JPA)
  application/                 Casos de uso (POJOs) + puertos (interfaces)
  infrastructure/
    web/                       Controladores REST, DTOs y mappers
    persistence/               Entidades JPA, repositorios Spring Data, adaptadores
  config/                      Cableado de los casos de uso como beans
```

Módulos: `usuario`, `paciente`, `medicacion`, `cita`, `bitacora`, `alerta`,
`reporte`, `dashboard`. Paquetes transversales: `config` (seguridad, CORS,
Swagger, administrador inicial) y `shared` (excepciones de dominio, `ApiError`,
`PageResponse`, `AuditoriaPort`).

**Regla de dependencias:** el dominio no depende de Spring ni de JPA; los casos de
uso son POJOs que se cablean como beans en un `*UseCaseConfig`. La infraestructura
implementa los puertos.

> No es microservicios. `alerta` y `reporte` son los candidatos naturales a
> separarse en el futuro.

### Frontend: SPA por capas y features

Alias `@` → `src/`. Rutas con React Router v7 y guards por rol. Cada feature
agrupa componentes, `types.ts`, `services/` (llamadas a la API) y hooks. El cliente
HTTP es propio sobre `fetch` nativo (`src/lib/api-client.ts`), sin librerías de
datos: adjunta el token, parsea el `ApiError` del backend y notifica 401 al
contexto de autenticación.

Restricciones de interfaz cumplidas: mobile-first, botones táctiles de mínimo
44 px, fuente base 16 px, bottom-nav en móvil y sidebar en escritorio, formularios
de una columna en móvil y fuente **Atkinson Hyperlegible** (legibilidad para baja
visión).

---

## 3. Tecnologías

### Backend

| Elemento | Tecnología |
| --- | --- |
| Lenguaje / build | Java 21 (LTS) + Maven 3.9 |
| Framework | Spring Boot 3.4.1 |
| Persistencia | Spring Data JPA (Hibernate) — solo valida el esquema |
| Migraciones | Flyway |
| Base de datos | PostgreSQL 16.6 |
| Seguridad | Spring Security + JWT HS256 (`jjwt` 0.12.6) + BCrypt |
| Validación | Jakarta Validation |
| Documentación API | springdoc-openapi 2.8.4 (Swagger UI solo en dev) |
| Reportes PDF | OpenPDF |
| Observabilidad | Actuator (solo `/actuator/health`) |
| Pruebas | JUnit 5, Mockito, MockMvc, Testcontainers (PostgreSQL real) |

### Frontend

| Elemento | Tecnología |
| --- | --- |
| Build | Vite 6 |
| UI | React 19 + TypeScript 5.7 |
| Ruteo | react-router-dom 7 |
| Estilos | Tailwind CSS v4 (`@tailwindcss/vite`, sin archivos de configuración) |
| Iconos / gráficos | lucide-react / recharts |
| Tipografía | `@fontsource/atkinson-hyperlegible` |
| HTTP | `fetch` nativo (cliente propio) |
| Lint / pruebas | ESLint 9 (flat config) / Vitest 2 + Testing Library + jsdom |

### E2E e infraestructura

| Elemento | Tecnología |
| --- | --- |
| Pruebas E2E | Playwright 1.64 (Chromium, ejecución secuencial) |
| Entorno local | Docker Compose (PostgreSQL 16.6 alpine) |
| Contenedor backend | Multi-stage Maven + JRE 21, usuario no root |
| CI/CD | GitHub Actions |
| Calidad | SonarQube (análisis local) + JaCoCo / lcov |

---

## 4. Estructura del repositorio

Monorepo:

```
meditrack-senior/
├── backend/                   API REST (Java 21 + Spring Boot)
│   ├── pom.xml
│   ├── Dockerfile             Build multi-stage, usuario no root, puerto 8080
│   ├── README.md              Guía detallada del backend
│   └── src/
│       ├── main/java/co/edu/uniquindio/meditrack/
│       ├── main/resources/    application*.yml y db/migration (Flyway)
│       └── test/java/         Pruebas unitarias, de web y de persistencia
├── frontend/                  SPA (Vite + React + TS)
│   ├── README.md              Guía detallada del frontend
│   ├── public/                Recursos estáticos y staticwebapp.config.json
│   └── src/                   app/, layouts/, components/, features/, lib/
├── e2e/                       Pruebas end-to-end (Playwright)
│   ├── README.md              Guía detallada de las pruebas E2E
│   ├── paginas/               Page Objects
│   ├── soporte/               Infraestructura compartida (env, cliente API, datos)
│   └── pruebas/               Casos CP-01..CP-32 por módulo
├── docs/                      Documentación y contrato de API
│   ├── api/openapi.yaml       Contrato OpenAPI 3.0.3
│   ├── api-guia.md            Guía explicada de la API
│   ├── especificacion-requisitos.md
│   ├── modelo-datos.md        Modelo entidad-relación y decisiones
│   ├── seguridad.md           JWT, roles, CORS, auditoría
│   └── diagramas/             C4 (Mermaid) y Structurizr DSL
├── infra/                     Infraestructura como código en Azure (Bicep)
├── .github/workflows/         CI de backend, frontend y análisis Sonar
├── docker-compose.yml         PostgreSQL local con volumen y healthcheck
├── .env.example               Plantilla de variables de entorno (sin secretos)
└── sonar-project.properties   Configuración del análisis estático
```

---

## 5. Puesta en marcha (local)

### Requisitos previos

- **Java 21** (LTS) y **Maven 3.9**
- **Node.js 20+** y npm
- **Docker Desktop** (PostgreSQL local y Testcontainers)

### Paso a paso

```powershell
# 1) Configurar variables de entorno (raíz y frontend)
Copy-Item .env.example .env
Copy-Item frontend/.env.example frontend/.env
#    Edita .env: contrasenas de PostgreSQL, JWT_SECRET (>= 32 bytes) y credenciales
#    del administrador inicial.

# 2) Levantar PostgreSQL (desde la raíz del monorepo)
docker compose up -d

# 3) Backend: aplica las migraciones Flyway y arranca en :8080
cd backend
mvn spring-boot:run

# 4) Frontend: dev server en :5173 (en otra terminal)
cd frontend
npm ci
npm run dev
```

Con el backend arrancado la primera vez, el **administrador inicial** se crea de
forma idempotente si no existe ningún administrador y están definidas
`ADMIN_INITIAL_EMAIL` y `ADMIN_INITIAL_PASSWORD`. Con esas credenciales se entra a
`http://localhost:5173/login`.

### URLs útiles

| Servicio | URL |
| --- | --- |
| Frontend (dev) | `http://localhost:5173` |
| API REST | `http://localhost:8080/api` |
| Salud del servicio | `http://localhost:8080/actuator/health` |
| Swagger UI (solo perfil `dev`) | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON (solo perfil `dev`) | `http://localhost:8080/v3/api-docs` |

### Comandos frecuentes

```powershell
# Backend
cd backend
mvn clean verify        # compila y ejecuta todas las pruebas

# Frontend
cd frontend
npm run lint            # ESLint
npm run test            # Vitest
npm run test:coverage   # Vitest + cobertura
npm run build           # TypeScript + build de producción en dist/

# E2E (requiere backend y frontend arriba)
cd e2e
npm ci
npx playwright install chromium
npm test
```

### Detener el entorno

```powershell
docker compose down       # conserva los datos
docker compose down -v    # elimina el volumen y reinicia la base
```

---

## 6. Variables de entorno

Los valores reales viven en `.env` (raíz) y `frontend/.env`, **nunca
versionados**. Las plantillas `.env.example` sí se versionan.

### Raíz (`.env`)

| Variable | Uso |
| --- | --- |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Credenciales del contenedor PostgreSQL |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión del backend a PostgreSQL |
| `JWT_SECRET` | Secreto HS256 (mínimo 32 bytes; el arranque falla si es más corto) |
| `JWT_EXPIRATION_MINUTES` | Vigencia del token en minutos |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por comas (nunca `*`) |
| `ADMIN_INITIAL_EMAIL`, `ADMIN_INITIAL_PASSWORD` | Administrador inicial idempotente |

El backend carga el `.env` de la raíz con
`spring.config.import: optional:file:../.env[.properties]`.

### Frontend (`frontend/.env`)

| Variable | Uso |
| --- | --- |
| `VITE_API_URL` | URL base de la API; debe terminar en `/api` (por defecto `http://localhost:8080/api`) |

### E2E (`e2e/.env`)

| Variable | Uso |
| --- | --- |
| `E2E_ADMIN_EMAIL`, `E2E_ADMIN_PASSWORD` | Credenciales del administrador (obligatorias) |
| `E2E_BASE_URL`, `E2E_API_URL` | URLs del frontend y de la API |
| `E2E_SLOWMO` | Pausa entre acciones (útil para demostraciones) |

En Azure estas variables se configuran como *Application Settings*.

---

## 7. API REST

API REST stateless con JSON, prefijo común `/api` y autenticación
`Authorization: Bearer <JWT>`. El contrato completo está en
[`docs/api/openapi.yaml`](docs/api/openapi.yaml) y se explica en prosa en
[`docs/api-guia.md`](docs/api-guia.md).

### Grupos de endpoints

| Grupo | Rutas |
| --- | --- |
| Autenticación | `POST /api/auth/login`, `GET /api/auth/me`, `PUT /api/auth/contrasena` |
| Usuarios | `/api/usuarios` (+ `{id}`, `{id}/estado`) — solo ADMINISTRADOR |
| Pacientes | `/api/pacientes` (+ `{id}`, `{id}/estado`) |
| Contactos | `/api/pacientes/{id}/contactos` (+ `{contactoId}`) |
| Familiares | `/api/pacientes/{id}/familiares` (+ `{usuarioId}`) |
| Cuidadores | `/api/pacientes/{id}/cuidadores` (+ `{usuarioId}`) |
| Medicamentos | `/api/medicamentos` (+ `{id}`, `{id}/estado`) |
| Esquemas | `/api/pacientes/{id}/esquemas` (+ `{esquemaId}`, `{esquemaId}/estado`) |
| Tomas | `/api/pacientes/{id}/tomas/agenda`, `/tomas/historial`, `POST /esquemas/{esquemaId}/tomas` |
| Citas | `GET /api/citas`, `/api/pacientes/{id}/citas` (+ `{citaId}`, `{citaId}/estado`) |
| Especialidades | `GET /api/especialidades` |
| Bitácora | `POST/GET /api/pacientes/{id}/signos-vitales`, `.../signos-vitales/ultimo` |
| Alertas | `GET /api/alertas`, `PATCH /api/alertas/{id}/atender` |
| Reportes | `GET /api/pacientes/{id}/reportes`, `GET /api/pacientes/{id}/reportes/{tipo}.pdf` |
| Salud | `GET /actuator/health` |

### Formatos comunes

- **Errores** — `ApiError`: `{ timestamp, status, codigo, mensaje, detalles? }`.
  Códigos HTTP 400/401/403/404/409/500 con códigos de negocio como
  `VALIDACION`, `NO_AUTENTICADO`, `ACCESO_DENEGADO`, `CREDENCIALES_INVALIDAS`,
  `CUERPO_INVALIDO`. Nunca se exponen trazas ni detalles técnicos.
- **Paginación** — `PageResponse`:
  `{ contenido, pagina, tamano, total, totalPaginas }`.

### Ejemplo de login

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"correo":"admin@ejemplo.com","contrasena":"..."}'
```

Respuesta `200`: `{ token, tipo: "Bearer", expiraEnSegundos, usuario: { id, nombreCompleto, correo, rol } }`.

---

## 8. Modelo de datos

Esquema gobernado por **Flyway**; JPA solo valida (`ddl-auto: validate`). El
detalle (columnas, índices, CHECK, relaciones) está en
[`docs/modelo-datos.md`](docs/modelo-datos.md).

Migraciones:

| Archivo | Contenido |
| --- | --- |
| `V1__baseline.sql` | Baseline sin tablas |
| `V2__modelo_inicial.sql` | 16 tablas del dominio, índices y restricciones |
| `V3__datos_semilla.sql` | Solo roles (3) y especialidades (8); no siembra usuarios |

Entidades principales: `rol`, `usuario`, `paciente`, `contacto_emergencia`,
`vinculacion_familiar`, `asignacion_cuidador`, `catalogo_medicamento`,
`esquema_medicacion`, `horario_medicacion`, `registro_toma`,
`catalogo_especialidad`, `cita`, `registro_signos_vitales`, `alerta`,
`reporte_generado`, `auditoria_acceso`.

Decisiones destacadas:

- PK `BIGINT IDENTITY` y fechas `TIMESTAMPTZ`.
- Enumeraciones como `VARCHAR` + `CHECK`.
- `registro_toma.estado` solo persiste `ADMINISTRADA`/`OMITIDA`; los estados
  pendiente y atrasado se derivan de `horario_medicacion`.
- Borrado lógico en usuarios; revocación sin borrado físico en vinculaciones y
  asignaciones.
- `auditoria_acceso` es append-only; `reporte_generado` guarda solo metadatos.
- Un solo centro geriátrico (limitación documentada).

---

## 9. Seguridad

Detalle completo en [`docs/seguridad.md`](docs/seguridad.md).

- **Stateless:** sin sesiones HTTP ni cookies; autenticación con JWT HS256.
- **Contraseñas** con BCrypt.
- **Autorización por rol** con `@PreAuthorize` (method security) y alcance por rol
  resuelto en los casos de uso.
- **CORS** desde `CORS_ALLOWED_ORIGINS`, sin comodín.
- **Auditoría** en `auditoria_acceso`: login exitoso/denegado, lectura fuera de
  alcance, altas, cambios de rol y de estado. Jamás se registran contraseñas ni
  tokens.
- **Minimización:** los listados no exponen la situación clínica.
- **Frontend:** la sesión se guarda en `sessionStorage` (se pierde al cerrar la
  pestaña); no hay refresh tokens y al vencer el JWT se cierra sesión. La interfaz
  solo decide qué controles mostrar; la autorización real la aplica el backend.
- **Swagger** deshabilitado fuera del perfil `dev`.

---

## 10. Pruebas

| Nivel | Herramientas | Cómo ejecutarlas |
| --- | --- | --- |
| Backend unitario y de web | JUnit 5, Mockito, MockMvc, Spring Security Test | `cd backend && mvn test` |
| Backend de persistencia | Testcontainers (PostgreSQL 16 real + migraciones Flyway) | incluido en `mvn clean verify` |
| Frontend | Vitest + Testing Library + jsdom | `cd frontend && npm run test` |
| End-to-end | Playwright (Chromium) | `cd e2e && npm test` |

- En el backend, las pruebas de persistencia extienden `BasePruebaPostgres`: un
  contenedor único por JVM, cada prueba se revierte con `@Transactional`. Son las
  que detectan errores de tipado SQL que los mocks no ven.
- La prueba de contexto excluye las autoconfiguraciones de base de datos, por lo
  que no necesita PostgreSQL.
- El frontend no necesita backend: `fetch` se simula con `vi.stubGlobal`.
- La suite E2E es **secuencial y con estado compartido** (los casos se encadenan);
  se ejecuta por archivo completo o la suite entera, no un caso aislado. Deja
  video, capturas y trace en `test-results/` y `playwright-report/`.

---

## 11. CI/CD y calidad

### GitHub Actions

| Workflow | Qué hace |
| --- | --- |
| `backend-ci.yml` | `mvn verify` con JDK 21 + Testcontainers y subida de cobertura JaCoCo (filtrado por `backend/**`) |
| `frontend-ci.yml` | `npm ci`, lint, pruebas con cobertura y build con Node 22 (filtrado por `frontend/**`) |
| `sonar-analysis.yml` | Análisis estático (backend y frontend en un solo proyecto) |

Ambos workflows se disparan en `push` y `pull_request` hacia `develop` y `main`.

### SonarQube local

```powershell
cd backend  && mvn -B verify
cd frontend && npm run test:coverage
# Con SONAR_TOKEN definido en la sesion y SonarQube en http://localhost:9000:
sonar-scanner
```

Las exclusiones y rutas de cobertura viven en `sonar-project.properties`.

---

## 12. Estado del proyecto

### Implementado y verificado

- **Backend:** módulos `usuario` (auth, usuarios, contraseña), `paciente`,
  `medicacion`, `cita`, `bitacora`, `alerta` y `reporte`. Seguridad JWT + roles,
  alcance por rol, auditoría, migraciones Flyway y manejo de errores uniforme.
- **Frontend:** login real, panel de administrador, gestión de usuarios, residentes,
  medicación, citas, bitácora, alertas, reportes y cambio de contraseña; navegación
  por rol y diseño responsive verificado a 375, 768 y 1280 px.
- **Pruebas:** ~200 casos en el backend (unitarios, web y persistencia con
  PostgreSQL real), pruebas de frontend con Vitest y 32 casos end-to-end con
  Playwright.
- **CI:** workflows de backend y frontend con cobertura.
- **Documentación:** OpenAPI, guía de API, modelo de datos, seguridad, requisitos y
  diagramas C4.

### Pendiente

- Módulo `dashboard`: el backend conserva solo la estructura del paquete; el panel
  del administrador y la vista del cuidador se construyen con los endpoints de
  residentes, tomas, citas y alertas.
- `infra/main.bicep` y `infra/parameters.prod.json` (despliegue en Azure) están como
  estructura, sin contenido.
- `sonar-analysis.yml` está creado pero sin pasos definidos.
- El PDF `docs/definicion-proyecto.pdf` está en el repositorio.

---

## 13. Documentación

| Documento | Contenido |
| --- | --- |
| [`backend/README.md`](backend/README.md) | Cómo correr y probar el backend, estructura de paquetes y rutas |
| [`frontend/README.md`](frontend/README.md) | Cómo correr y probar el frontend, módulos y reglas reflejadas |
| [`e2e/README.md`](e2e/README.md) | Cómo ejecutar las pruebas E2E y qué cubre cada caso |
| [`docs/api/openapi.yaml`](docs/api/openapi.yaml) | Contrato OpenAPI 3.0.3 |
| [`docs/api-guia.md`](docs/api-guia.md) | Guía de la API: autenticación, errores, paginación y endpoints |
| [`docs/especificacion-requisitos.md`](docs/especificacion-requisitos.md) | Requisitos funcionales y no funcionales |
| [`docs/modelo-datos.md`](docs/modelo-datos.md) | Modelo entidad-relación y decisiones de diseño |
| [`docs/seguridad.md`](docs/seguridad.md) | JWT, roles, CORS, auditoría y alcance |
| [`docs/diagramas/`](docs/diagramas) | Diagramas C4 (contexto, contenedores, componentes) |

---

## 14. Equipo

Proyecto desarrollado para el curso **Ingeniería de Software III** de la
Universidad del Quindío, por los estudiantes Miguel Angel Montenegro y Maria Juanita Camargo**
(Armenia, Quindío).

Interfaz y textos del producto en **español**; zona horaria `America/Bogota`.
