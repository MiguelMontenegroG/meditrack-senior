# Guia tecnica de la API - MediTrack Senior

Documentacion detallada de la API REST del backend de **MediTrack Senior**.
Explica que es la API, como esta construida, como autenticarse, como se
estructuran las peticiones y respuestas, que significa cada error y que hace
cada endpoint, con ejemplos reales contrastados contra el codigo fuente
(paquete base `co.edu.uniquindio.meditrack`).

> Documento generado a partir de la lectura directa de los controladores, DTOs,
> casos de uso y configuracion de seguridad del backend. No sustituye a
> `docs/api/openapi.yaml` (contrato maquina-legible); lo explica en prosa.

---

## Indice

1. [Que es esta API](#1-que-es-esta-api)
2. [Arquitectura del backend](#2-arquitectura-del-backend)
3. [Convenciones generales](#3-convenciones-generales)
4. [Autenticacion y autorizacion (JWT)](#4-autenticacion-y-autorizacion-jwt)
5. [Formato de errores](#5-formato-de-errores)
6. [Paginacion, filtros y alcance por rol](#6-paginacion-filtros-y-alcance-por-rol)
7. [Categorias de endpoints](#7-categorias-de-endpoints)
8. [Referencia de endpoints](#8-referencia-de-endpoints)
    - [8.1 Autenticacion](#81-autenticacion)
    - [8.2 Usuarios](#82-usuarios)
    - [8.3 Pacientes](#83-pacientes)
    - [8.4 Contactos de emergencia](#84-contactos-de-emergencia)
    - [8.5 Familiares](#85-familiares)
    - [8.6 Cuidadores](#86-cuidadores)
    - [8.7 Medicamentos](#87-medicamentos)
    - [8.8 Esquemas de medicacion](#88-esquemas-de-medicacion)
    - [8.9 Tomas](#89-tomas)
    - [8.10 Citas](#810-citas)
    - [8.11 Bitacora de signos vitales](#811-bitacora-de-signos-vitales)
    - [8.12 Especialidades](#812-especialidades)
    - [8.13 Salud del servicio](#813-salud-del-servicio)
9. [Enumeraciones y modelos de datos](#9-enumeraciones-y-modelos-de-datos)
10. [Reglas de negocio transversales](#10-reglas-de-negocio-transversales)
11. [Como probar la API](#11-como-probar-la-api)
12. [Estado de implementacion por modulo](#12-estado-de-implementacion-por-modulo)

---

## 1. Que es esta API

Una **API (Interfaz de Programacion de Aplicaciones)** es el contrato que
permite que dos programas se comuniquen. En MediTrack Senior, la API es el
unico punto por el que el frontend (SPA en React) obtiene y modifica datos: no
hay acceso directo a base de datos desde el navegador.

Caracteristicas de la API de MediTrack Senior:

- **Estilo REST**: recursos identificados por URL y operaciones expresadas con
  verbos HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- **Formato JSON** en peticiones y respuestas (`Content-Type: application/json`).
- **Prefijo comun `/api`** para todas las rutas.
- **Transporte HTTP/HTTPS**; en local escucha en `http://localhost:8080`.
- **Stateless**: cada peticion debe llevar su credencial (token JWT); el servidor
  no guarda sesion entre peticiones.
- **Datos sensibles de salud**: toda operacion esta sujeta a control de acceso
  por rol y a auditoria (Ley 1581 de 2012).
- **Documentacion automatica** con `springdoc-openapi` (Swagger UI disponible
  solo en el perfil `dev`).

---

## 2. Arquitectura del backend

La API es la capa exterior de un **monolito modular con arquitectura hexagonal
por feature**. Paquete base: `co.edu.uniquindio.meditrack`.

```
back de un modulo (ej. paciente)
  domain/                 modelo y reglas puras (sin Spring ni JPA)
  application/            casos de uso (POJO) + puertos (interfaces)
  infrastructure/
    web/                  controladores REST + DTOs (Request/Response) + mappers
    persistence/          entidades JPA + repositorios Spring Data + adaptadores
  config/                 cableado de casos de uso como beans
```

Paquetes transversales:

- `config`: seguridad (`SecurityConfig`, `JwtService`,
  `JwtAuthenticationFilter`, `CorsConfig`, `HandlersSeguridad`), Swagger
  (`OpenApiConfig`), BCrypt (`PasswordEncoderConfig`) y admin inicial
  (`AdministradorInicialRunner`).
- `shared`: excepciones de dominio, `ApiError`, `PageResponse`, `AuditoriaPort`.

**Recorrido de una peticion** (de arriba a abajo):

```
Cliente (SPA)
  -> HTTP + Authorization: Bearer <JWT>
  -> JwtAuthenticationFilter        (valida token, carga usuario)
  -> SecurityFilterChain            (permitAll / authenticated)
  -> @PreAuthorize del controlador  (autoriza por rol)
  -> Controller (infrastructure/web) (DTO de entrada -> Comando)
  -> UseCase (application)          (reglas de negocio, invoca puertos)
  -> RepositoryPort -> Adaptador (infrastructure/persistence)
  -> JpaRepository -> PostgreSQL (esquema gobernado por Flyway)
  -> dominio -> mapper -> DTO de salida -> JSON (o ApiError)
```

**Stack tecnico** relevante para la API:

| Elemento | Tecnologia |
| --- | --- |
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Web | Spring MVC (`spring-boot-starter-web`) |
| Validacion | Jakarta Validation (`@Valid`, `@NotBlank`, `@Size`, ...) |
| Seguridad | Spring Security + JWT HS256 (`jjwt` 0.12.6) |
| Persistencia | Spring Data JPA + Hibernate + PostgreSQL 16 |
| Migraciones | Flyway |
| Documentacion | springdoc-openapi 2.8.4 |

---

## 3. Convenciones generales

### 3.1 URL base

| Entorno | URL base |
| --- | --- |
| Local | `http://localhost:8080/api` |
| Produccion | Azure App Service (host propio); el frontend la recibe en `VITE_API_URL` |

Todas las rutas de este documento ya incluyen el prefijo `/api`.

### 3.2 Verbos HTTP y su significado en esta API

| Verbo | Uso | Ejemplo |
| --- | --- | --- |
| `GET` | Leer uno o varios recursos | `GET /api/pacientes` |
| `POST` | Crear un recurso | `POST /api/pacientes` |
| `PUT` | Reemplazar/actualizar datos de un recurso | `PUT /api/usuarios/{id}` |
| `PATCH` | Cambio parcial (estado activo/inactivo, cambio de estado) | `PATCH /api/pacientes/{id}/estado` |
| `DELETE` | Eliminar o revocar (cuando aplica) | `DELETE /api/pacientes/{id}/contactos/{contactoId}` |

### 3.3 Codigos de estado HTTP usados

| Codigo | Significado en esta API |
| --- | --- |
| `200 OK` | Operacion exitosa con cuerpo de respuesta |
| `201 Created` | Recurso creado correctamente |
| `204 No Content` | Operacion exitosa sin cuerpo (cambiar contrasena, eliminar, revocar) |
| `400 Bad Request` | Cuerpo invalido (`CUERPO_INVALIDO`), fallo de validacion (`VALIDACION`) o dato incoherente de negocio |
| `401 Unauthorized` | Falta token, token invalido/expirado o credenciales incorrectas |
| `403 Forbidden` | Autenticado pero sin permisos para la operacion |
| `404 Not Found` | Recurso inexistente o **fuera del alcance** del usuario |
| `409 Conflict` | Conflicto de negocio (duplicados, transicion de estado no permitida) |
| `500 Internal Server Error` | Error no controlado (el detalle queda solo en el log) |

### 3.4 Tipos de datos JSON

- **Fechas**: `LocalDate` en formato `YYYY-MM-DD`.
- **Fechas con hora**: `Instant` en formato ISO-8601 UTC, p. ej.
  `2025-03-14T08:30:00Z`.
- **Hora del dia**: `LocalTime` en formato `HH:mm` o `HH:mm:ss`.
- **Decimales**: `BigDecimal` (por ejemplo, temperatura `37.5`).
- **Booleanos**: `true` / `false`.
- **Identificadores**: enteros largos (`Long`).
- **Enumeraciones**: cadenas en MAYUSCULAS (ver seccion 9).

### 3.5 Cabeceras relevantes

| Cabecera | Uso |
| --- | --- |
| `Authorization: Bearer <token>` | Obligatoria salvo en rutas publicas |
| `Content-Type: application/json` | En peticiones con cuerpo |
| `Accept: application/json` | Formato esperado de respuesta |

---

## 4. Autenticacion y autorizacion (JWT)

### 4.1 Modelo

- Autenticacion **stateless** con **JWT firmado en HS256**.
- El token se obtiene en `POST /api/auth/login` y se envia en cada peticion.
- Base64 transparente; el cliente no necesita decodificarlo.

### 4.2 Contenido del token (claims)

| Claim | Significado |
| --- | --- |
| `sub` | Id del usuario (se usa como nombre de autenticacion) |
| `rol` | Nombre del rol (`ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`) |
| `iat` | Momento de emision |
| `exp` | Momento de expiracion |

El token **no incluye** correo ni ningun otro dato sensible. La vigencia se
controla con `JWT_EXPIRATION_MINUTES`.

### 4.3 Proceso de validacion en cada peticion

`JwtAuthenticationFilter` (`OncePerRequestFilter`):

1. Lee `Authorization: Bearer <token>`.
2. Valida firma y expiracion.
3. Carga el usuario por `sub`; si no existe o tiene `activo = false`, limpia el
   contexto y la peticion queda como no autenticada.
4. Coloca la autenticacion con authority `ROLE_<rol>` en el `SecurityContext`.

Si el token falta o es invalido, la respuesta es `401 NO_AUTENTICADO`. Si el
usuario esta autenticado pero su rol no permite la operacion, la respuesta es
`403 ACCESO_DENEGADO`.

### 4.4 Rutas publicas

- `POST /api/auth/login`
- `GET /actuator/health`
- Swagger UI y `/v3/api-docs/**` (solo cuando `meditrack.swagger-habilitado=true`,
  es decir, perfil `dev`)

Todas las demas rutas exigen JWT valido.

### 4.5 Roles y autorizacion

La autorizacion se aplica con `@PreAuthorize` (method security). Los roles son:

| Rol | Descripcion |
| --- | --- |
| `ADMINISTRADOR` | Acceso completo; gestiona usuarios y operacion del centro |
| `CUIDADOR_ENFERMERO` | Registra bitacoras y tomas, atiende tareas del dia |
| `FAMILIAR_AUTORIZADO` | **Solo lectura** de los residentes que tiene vinculados |

Las authorities de Spring Security llevan el prefijo `ROLE_` (por ejemplo,
`ROLE_ADMINISTRADOR`), pero en la API los roles viajan **sin** el prefijo.

### 4.6 CORS

- Origenes permitidos: variable `CORS_ALLOWED_ORIGINS` (lista separada por
  comas). Nunca se usa `*`.
- Metodos: `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
- Cabeceras permitidas: `Authorization, Content-Type, Accept`.
- Credenciales permitidas.

---

## 5. Formato de errores

Todos los errores de la API usan **un unico formato** (`ApiError`), sin stack
traces ni datos internos:

```json
{
  "timestamp": "2025-03-14T08:30:00Z",
  "status": 400,
  "codigo": "VALIDACION",
  "mensaje": "La peticion no es valida",
  "detalles": ["correo: El correo es obligatorio"]
}
```

| Campo | Tipo | Descripcion |
| --- | --- | --- |
| `timestamp` | `Instant` | Momento del error |
| `status` | `int` | Codigo HTTP |
| `codigo` | `string` | Codigo estable para el cliente (ver tabla) |
| `mensaje` | `string` | Mensaje legible en espanol |
| `detalles` | `string[]` (opcional) | Solo presente en errores de validacion |

### 5.1 Tabla de codigos de error

| Codigo | HTTP tipico | Causa |
| --- | --- | --- |
| `VALIDACION` | 400 | Fallo de validacion de DTO o dato incoherente de negocio |
| `CUERPO_INVALIDO` | 400 | JSON malformado o tipo incorrecto |
| `CREDENCIALES_INVALIDAS` | 401 | Login con correo/contrasena incorrectos (mensaje generico) |
| `NO_AUTENTICADO` | 401 | Falta token, token invalido o expirado |
| `ACCESO_DENEGADO` | 403 | Rol sin permisos para la operacion |
| `NO_ENCONTRADO` | 404 | Recurso inexistente o fuera de alcance |
| `CONFLICTO` | 409 | Duplicado o transicion de estado no permitida |
| `ERROR_INTERNO` | 500 | Error no controlado |

### 5.2 Nota sobre privacidad

- El login devuelve siempre `CREDENCIALES_INVALIDAS` para no revelar si el
  correo existe.
- Un residente fuera del alcance del usuario devuelve **404** (nunca 403) para
  no revelar su existencia.
- El detalle del error interno (`500`) se escribe solo en el log del servidor.

---

## 6. Paginacion, filtros y alcance por rol

### 6.1 Paginacion (`PageResponse`)

Los listados paginados comparten esta envoltura:

```json
{
  "contenido": [],
  "pagina": 0,
  "tamano": 20,
  "total": 57,
  "totalPaginas": 3
}
```

| Parametro de query | Por defecto | Limite |
| --- | --- | --- |
| `pagina` | `0` (base 0) | - |
| `tamano` | `20` | 100 (se acota automaticamente) |

> Excepcion: `GET /api/medicamentos` usa su propia envoltura
> `PaginaMedicamentosResponse` y `GET /api/pacientes/{id}/tomas/historial` usa
> `PaginaTomasResponse`; en ambos casos los campos son identicos a
> `PageResponse`.

### 6.2 Alcance por rol (filtrado en el backend)

El backend filtra los datos segun el rol del usuario; el cliente no decide el
alcance. Regla aplicada en `paciente`, `medicacion`, `cita` y `bitacora`:

| Rol | Que ve |
| --- | --- |
| `ADMINISTRADOR` | Todos los residentes (incluidos inactivos en la gestion) |
| `CUIDADOR_ENFERMERO` | Solo residentes con **asignacion vigente** (`hasta IS NULL`) |
| `FAMILIAR_AUTORIZADO` | Solo residentes con **vinculacion autorizada** (`autorizado = true`) |

Cuando el residente esta fuera del alcance, la respuesta es `404` y se registra
una auditoria `READ / DENEGADO`. En los listados, un residente fuera de alcance
simplemente no aparece (no se revela su existencia).

### 6.3 Filtros comunes

- `texto`: busqueda por nombre u otro texto (segun el recurso).
- `activo`: filtra activos/inactivos (`Boolean`).
- `desde` / `hasta`: rangos de fecha (ISO-8601) en citas, tomas y bitacora.

---

## 7. Categorias de endpoints

| Categoria | Prefijo de ruta | Modulo |
| --- | --- | --- |
| Autenticacion | `/api/auth` | `usuario` |
| Usuarios | `/api/usuarios` | `usuario` |
| Pacientes | `/api/pacientes` | `paciente` |
| Contactos de emergencia | `/api/pacientes/{pacienteId}/contactos` | `paciente` |
| Familiares | `/api/pacientes/{pacienteId}/familiares` | `paciente` |
| Cuidadores | `/api/pacientes/{pacienteId}/cuidadores` | `paciente` |
| Medicamentos | `/api/medicamentos` | `medicacion` |
| Esquemas | `/api/pacientes/{pacienteId}/esquemas` | `medicacion` |
| Tomas | `/api/pacientes/{pacienteId}` | `medicacion` |
| Citas | `/api/citas`, `/api/pacientes/{pacienteId}/citas` | `cita` |
| Signos vitales | `/api/pacientes/{pacienteId}/signos-vitales` | `bitacora` |
| Especialidades | `/api/especialidades` | `cita` |
| Salud | `/actuator/health` | transversal |

---

## 8. Referencia de endpoints

> Convencion: se indica **Metodo + Ruta**, el rol requerido, el cuerpo de entrada
> y la respuesta. Los roles se refieren a `@PreAuthorize`.

---

### 8.1 Autenticacion

#### POST /api/auth/login

- **Roles:** publico (sin JWT).
- **Cuerpo de entrada** (`LoginRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `correo` | string | Obligatorio, formato email valido |
| `contrasena` | string | Obligatorio |

```json
{ "correo": "admin@meditrack.co", "contrasena": "ClaveSegura123" }
```

- **Respuesta `200 OK`** (`LoginResponse`):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 3600,
  "usuario": {
    "id": 1,
    "nombreCompleto": "Ana Perez",
    "correo": "admin@meditrack.co",
    "rol": "ADMINISTRADOR"
  }
}
```

- **Errores:** `400 VALIDACION` / `CUERPO_INVALIDO`,
  `401 CREDENCIALES_INVALIDAS` (correo/contrasena incorrectos o usuario
  inactivo).
- **Efecto colateral:** registra un acceso en `auditoria_acceso`.

#### GET /api/auth/me

- **Roles:** cualquier rol autenticado.
- **Sin parametros.** Devuelve el usuario dueno del token.
- **Respuesta `200 OK`** (`UsuarioAutenticadoResponse`): `id`, `nombreCompleto`,
  `correo`, `rol`.
- **Errores:** `401 NO_AUTENTICADO`.

#### PUT /api/auth/contrasena

- **Roles:** cualquier rol autenticado (cambia **su propia** contrasena).
- **Cuerpo de entrada** (`CambiarContrasenaRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `contrasenaActual` | string | Obligatorio |
| `contrasenaNueva` | string | Obligatorio, minimo 8 caracteres |

- **Respuesta `204 No Content`.**
- **Errores:** `400 VALIDACION`, `401 NO_AUTENTICADO`; contrasena actual
  incorrecta se traduce a `400 VALIDACION` o `401 CREDENCIALES_INVALIDAS` segun
  el caso de uso.

---

### 8.2 Usuarios

> Todo el controlador exige `@PreAuthorize("hasRole('ADMINISTRADOR')")`. No
> existe borrado fisico; el estado se gestiona con activar/desactivar.

#### POST /api/usuarios

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CrearUsuarioRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `correo` | string | Obligatorio, email valido, unico (case-insensitive) |
| `nombreCompleto` | string | Obligatorio, max 150 |
| `telefono` | string | Opcional, max 30 |
| `contrasena` | string | Obligatorio, minimo 8 |
| `rol` | enum `RolNombre` | Obligatorio |

- **Respuesta `201 Created`** (`UsuarioResponse`):

```json
{
  "id": 12,
  "nombreCompleto": "Luis Gomez",
  "correo": "luis@meditrack.co",
  "telefono": "3001234567",
  "rol": "CUIDADOR_ENFERMERO",
  "activo": true,
  "ultimoAccesoEn": null,
  "creadoEn": "2025-03-14T08:30:00Z",
  "actualizadoEn": "2025-03-14T08:30:00Z"
}
```

- **Errores:** `400 VALIDACION`, `409 CONFLICTO` (correo duplicado).

#### GET /api/usuarios

- **Rol:** `ADMINISTRADOR`.
- **Query:** `rol` (opcional), `activo` (opcional), `pagina` (def. 0),
  `tamano` (def. 20, max 100).
- **Respuesta `200 OK`:** `PageResponse<UsuarioResponse>`.

#### GET /api/usuarios/{id}

- **Rol:** `ADMINISTRADOR`.
- **Respuesta `200 OK`:** `UsuarioResponse`.
- **Errores:** `404 NO_ENCONTRADO`.

#### PUT /api/usuarios/{id}

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`ActualizarUsuarioRequest`): `nombreCompleto` (obligatorio, max
  150), `telefono` (opcional, max 30), `rol` (obligatorio).
- **Respuesta `200 OK`:** `UsuarioResponse`.
- **Regla:** un administrador no puede quitarse su propio rol (`409`).

#### PATCH /api/usuarios/{id}/estado

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CambiarEstadoRequest`): `activo` (boolean obligatorio).
- **Respuesta `200 OK`:** `UsuarioResponse`.
- **Regla:** un administrador no puede desactivarse a si mismo (`409`).

---

### 8.3 Pacientes

> Escritura solo `ADMINISTRADOR`. La lectura aplica alcance por rol (seccion 6.2).

#### POST /api/pacientes

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CrearPacienteRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `documento` | string | Obligatorio, max 30, unico |
| `nombres` | string | Obligatorio, max 100 |
| `apellidos` | string | Obligatorio, max 100 |
| `fechaNacimiento` | date | Obligatorio, no futura |
| `sexo` | enum `Sexo` | Obligatorio |
| `habitacion` | string | Opcional, max 20 |
| `situacionClinica` | string | Opcional |
| `fechaIngreso` | date | Opcional |

```json
{
  "documento": "1094888123",
  "nombres": "Rosa",
  "apellidos": "Martinez",
  "fechaNacimiento": "1940-05-12",
  "sexo": "FEMENINO",
  "habitacion": "A-101",
  "situacionClinica": "Hipertension controlada",
  "fechaIngreso": "2024-01-10"
}
```

- **Respuesta `201 Created`** (`PacienteDetalleResponse`): incluye `edad`
  calculada y `contactos`.
- **Errores:** `400 VALIDACION`, `409 CONFLICTO` (documento duplicado).

#### GET /api/pacientes

- **Roles:** cualquier rol autenticado (resultado filtrado por alcance).
- **Query:** `texto`, `activo`, `pagina`, `tamano`.
- **Respuesta `200 OK`:** `PageResponse<PacienteResumenResponse>`. El resumen no
  incluye la situacion clinica:

```json
{ "id": 3, "nombreCompleto": "Rosa Martinez", "edad": 84, "habitacion": "A-101", "activo": true }
```

#### GET /api/pacientes/{id}

- **Roles:** cualquier rol autenticado (alcance).
- **Respuesta `200 OK`:** `PacienteDetalleResponse`.
- **Errores:** `404 NO_ENCONTRADO` (inexistente o fuera de alcance).

#### PUT /api/pacientes/{id}

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo:** `CrearPacienteRequest` (mismo esquema que la creacion).
- **Respuesta `200 OK`:** `PacienteDetalleResponse`.

#### PATCH /api/pacientes/{id}/estado

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CambiarEstadoPacienteRequest`): `activo` (obligatorio), `motivo`
  (max 200; obligatorio al desactivar por auditoria, ignorado al activar).
- **Respuesta `200 OK`:** `PacienteDetalleResponse`.

---

### 8.4 Contactos de emergencia

> Base: `/api/pacientes/{pacienteId}/contactos`. Lectura segun alcance;
> escritura solo `ADMINISTRADOR`.

| Metodo | Ruta | Rol | Descripcion |
| --- | --- | --- | --- |
| `GET` | `` | segun alcance | Listar contactos |
| `POST` | `` | `ADMINISTRADOR` | Crear contacto |
| `PUT` | `/{contactoId}` | `ADMINISTRADOR` | Actualizar contacto |
| `DELETE` | `/{contactoId}` | `ADMINISTRADOR` | Eliminar contacto |

**Cuerpo** (`ContactoRequest`): `nombre` (obligatorio, max 150), `parentesco`
(opcional, max 60), `telefono` (obligatorio, max 30), `principal` (boolean).

**Respuesta** (`ContactoEmergenciaResponse`): `id`, `nombre`, `parentesco`,
`telefono`, `principal`.

**Regla:** solo puede haber **un contacto principal**; al marcar uno nuevo se
degrada el anterior. `POST` devuelve `201`, `DELETE` devuelve `204`.

---

### 8.5 Familiares

> Base: `/api/pacientes/{pacienteId}/familiares`. Todo el controlador exige
> `ADMINISTRADOR`.

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `GET` | `` | Listar familiares vinculados |
| `POST` | `` | Vincular familiar |
| `DELETE` | `/{usuarioId}` | Revocar vinculacion (`autorizado = false`, sin borrado fisico) |

**Cuerpo (vincular)** (`FamiliarRequest`): `usuarioId` (obligatorio),
`parentesco` (opcional, max 60).

**Respuesta** (`VinculacionFamiliarResponse`): `id`, `usuarioId`, `pacienteId`,
`parentesco`, `autorizado`.

**Regla:** el usuario a vincular debe tener rol `FAMILIAR_AUTORIZADO` y estar
activo. `POST` devuelve `201`; `DELETE` devuelve `204`.

---

### 8.6 Cuidadores

> Base: `/api/pacientes/{pacienteId}/cuidadores`. Todo el controlador exige
> `ADMINISTRADOR`.

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| `GET` | `` | Listar asignaciones vigentes |
| `POST` | `` | Asignar cuidador |
| `DELETE` | `/{usuarioId}` | Desasignar (cierra la asignacion: `hasta = hoy`) |

**Cuerpo (asignar)** (`CuidadorRequest`): `usuarioId` (obligatorio).

**Respuesta** (`AsignacionCuidadorResponse`): `id`, `usuarioId`, `pacienteId`,
`desde`, `hasta` (null mientras este vigente).

**Regla:** el usuario debe tener rol `CUIDADOR_ENFERMERO` y estar activo. Un
cuidador sin asignacion vigente no ve al paciente. `POST` devuelve `201`;
`DELETE` devuelve `204`.

---

### 8.7 Medicamentos

> Catalogo de medicamentos. **Lectura:** `ADMINISTRADOR` y `CUIDADOR_ENFERMERO`.
> **Escritura:** solo `ADMINISTRADOR`.

#### GET /api/medicamentos

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`.
- **Query:** `texto`, `activo`, `pagina` (def. 0), `tamano` (def. 20, max 100).
- **Respuesta `200 OK`** (`PaginaMedicamentosResponse`):

```json
{
  "contenido": [ { "id": 5, "nombre": "Losartan", "presentacion": "50 mg tableta", "activo": true } ],
  "pagina": 0, "tamano": 20, "total": 1, "totalPaginas": 1
}
```

#### POST /api/medicamentos

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`MedicamentoRequest`): `nombre` (obligatorio, max 150),
  `presentacion` (opcional, max 100).
- **Respuesta `201 Created`:** `MedicamentoResponse`.

#### PUT /api/medicamentos/{id}

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo:** `MedicamentoRequest`.
- **Respuesta `200 OK`:** `MedicamentoResponse`.

#### PATCH /api/medicamentos/{id}/estado

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CambiarEstadoMedicacionRequest`): `activo` (boolean obligatorio).
- **Respuesta `200 OK`:** `MedicamentoResponse`.

---

### 8.8 Esquemas de medicacion

> Base: `/api/pacientes/{pacienteId}/esquemas`. **Lectura:** los tres roles
> (segun alcance). **Escritura:** solo `ADMINISTRADOR`.

#### GET /api/pacientes/{pacienteId}/esquemas

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`.
- **Query:** `activo` (opcional), `vigente` (opcional).
- **Respuesta `200 OK`:** `List<EsquemaResponse>` (no paginado).

```json
[
  {
    "id": 8,
    "pacienteId": 3,
    "medicamentoId": 5,
    "medicamentoNombre": "Losartan",
    "dosis": "1 tableta",
    "frecuenciaHoras": 24,
    "via": "ORAL",
    "indicaciones": "Despues del desayuno",
    "fechaInicio": "2025-01-01",
    "fechaFin": null,
    "activo": true,
    "vigente": true,
    "horarios": ["08:00"]
  }
]
```

#### GET /api/pacientes/{pacienteId}/esquemas/{esquemaId}

- **Roles:** los tres (segun alcance).
- **Respuesta `200 OK`:** `EsquemaResponse`.

#### POST /api/pacientes/{pacienteId}/esquemas

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`EsquemaRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `medicamentoId` | long | Obligatorio |
| `dosis` | string | Obligatorio, max 50 |
| `frecuenciaHoras` | int | Entre 1 y 24 |
| `via` | enum `ViaAdministracion` | Obligatorio |
| `indicaciones` | string | Opcional |
| `fechaInicio` | date | Obligatorio |
| `fechaFin` | date | Opcional |
| `horarios` | `LocalTime[]` | Obligatorio, al menos uno (`HH:mm`) |

- **Respuesta `201 Created`:** `EsquemaResponse`.


#### PUT /api/pacientes/{pacienteId}/esquemas/{esquemaId}

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo:** `EsquemaRequest` (reemplaza tambien los horarios).
- **Respuesta `200 OK`:** `EsquemaResponse`.

#### PATCH /api/pacientes/{pacienteId}/esquemas/{esquemaId}/estado

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CambiarEstadoMedicacionRequest`): `activo` (boolean obligatorio).
- **Respuesta `200 OK`:** `EsquemaResponse`.

---

### 8.9 Tomas

> Base: `/api/pacientes/{pacienteId}`. La agenda y el historial cuelgan de
> `/tomas` (agregados del residente); el registro de una toma es un subrecurso
> del esquema. Los estados `PENDIENTE` y `ATRASADA` **no se persisten**: se
> calculan a partir de los horarios y de las tomas registradas.

#### GET /api/pacientes/{pacienteId}/tomas/agenda

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`.
- **Query:** `fecha` (opcional, `YYYY-MM-DD`; por defecto hoy).
- **Respuesta `200 OK`:** `List<AgendaEsquemaResponse>`.

```json
[
  {
    "esquemaId": 8,
    "medicamentoId": 5,
    "medicamentoNombre": "Losartan",
    "dosis": "1 tableta",
    "slots": [
      { "fechaHoraProgramada": "2025-03-14T13:00:00Z", "estado": "ADMINISTRADA" },
      { "fechaHoraProgramada": "2025-03-14T21:00:00Z", "estado": "PENDIENTE" }
    ]
  }
]
```

Estados de slot posibles: `PENDIENTE`, `ATRASADA`, `ADMINISTRADA`, `OMITIDA`.

#### GET /api/pacientes/{pacienteId}/tomas/historial

- **Roles:** los tres (segun alcance).
- **Query:** `esquemaId` (opcional), `desde` (opcional, `Instant`), `hasta`
  (opcional, `Instant`), `pagina` (def. 0), `tamano` (def. 20, max 100).
- **Respuesta `200 OK`** (`PaginaTomasResponse` con `RegistroTomaResponse`):

```json
{
  "contenido": [
    {
      "id": 40,
      "esquemaId": 8,
      "registradoPor": 12,
      "fechaHoraProgramada": "2025-03-14T13:00:00Z",
      "fechaHoraRegistro": "2025-03-14T13:02:00Z",
      "estado": "ADMINISTRADA",
      "observaciones": "Sin novedad"
    }
  ],
  "pagina": 0, "tamano": 20, "total": 1, "totalPaginas": 1
}
```

#### POST /api/pacientes/{pacienteId}/esquemas/{esquemaId}/tomas

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`.
- **Cuerpo** (`RegistrarTomaRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `fechaHoraProgramada` | `Instant` | Obligatorio; debe coincidir con un horario del esquema y no ser futura |
| `estado` | enum `EstadoToma` | Obligatorio (`ADMINISTRADA` / `OMITIDA`) |
| `observaciones` | string | Opcional |

- **Respuesta `201 Created`:** `RegistroTomaResponse`. El autor
  (`registradoPor`) sale del usuario autenticado, **no** del cuerpo.
- **Errores:** `400 VALIDACION` (hora no programada o futura),
  `404 NO_ENCONTRADO` (esquema inexistente o fuera de alcance),
  `409 CONFLICTO` (esquema no utilizable o toma ya registrada para esa hora).

---

### 8.10 Citas

> Calendario de citas medicas. Las reglas de estado se detallan en la seccion 10.

#### GET /api/citas

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`.
- **Query:** `desde` (obligatorio, `Instant`), `hasta` (obligatorio, `Instant`),
  `estado` (opcional), `pacienteId` (opcional), `pagina`, `tamano`.
- **Respuesta `200 OK`:** `PageResponse<CitaResponse>`.

```json
{
  "contenido": [
    {
      "id": 21,
      "pacienteId": 3,
      "especialidadId": 2,
      "especialidadNombre": "Cardiologia",
      "registradaPor": 1,
      "profesional": "Dr. Ruiz",
      "fechaHora": "2025-03-20T15:00:00Z",
      "lugar": "Consultorio 4",
      "motivo": "Control",
      "estado": "PROGRAMADA"
    }
  ],
  "pagina": 0, "tamano": 20, "total": 1, "totalPaginas": 1
}
```

#### GET /api/pacientes/{pacienteId}/citas/{citaId}

- **Roles:** los tres (segun alcance).
- **Respuesta `200 OK`:** `CitaResponse`.
- **Errores:** `404 NO_ENCONTRADO`.

#### POST /api/pacientes/{pacienteId}/citas

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo** (`CitaRequest`):

| Campo | Tipo | Reglas |
| --- | --- | --- |
| `especialidadId` | long | Opcional |
| `profesional` | string | Opcional, max 150 |
| `fechaHora` | `Instant` | Obligatorio en el dominio; no puede estar en el pasado |
| `lugar` | string | Opcional, max 150 |
| `motivo` | string | Opcional |

- **Respuesta `201 Created`:** `CitaResponse` (estado inicial `PROGRAMADA`).
- **Errores:** `400 VALIDACION` (fecha en el pasado), `404 NO_ENCONTRADO`.

#### PUT /api/pacientes/{pacienteId}/citas/{citaId}

- **Rol:** `ADMINISTRADOR`.
- **Cuerpo:** `CitaRequest`.
- **Respuesta `200 OK`:** `CitaResponse`.
- **Regla:** solo se edita en estado `PROGRAMADA` (en otro caso `409 CONFLICTO`).

#### PATCH /api/pacientes/{pacienteId}/citas/{citaId}/estado

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`.
- **Cuerpo** (`CambiarEstadoCitaRequest`): `estado` (enum `EstadoCita`,
  obligatorio).
- **Respuesta `200 OK`:** `CitaResponse`.
- **Reglas:**
  - `CUMPLIDA` / `INCUMPLIDA`: administrador o cuidador; solo desde
    `PROGRAMADA` y solo si la fecha/hora ya llego.
  - `CANCELADA`: **solo** administrador (`403 ACCESO_DENEGADO` si es cuidador);
    desde `PROGRAMADA`.
  - `PROGRAMADA` / `REPROGRAMADA` como destino: `400 VALIDACION`.
  - Transicion desde un estado final: `409 CONFLICTO`.

---

### 8.11 Bitacora de signos vitales

> Base: `/api/pacientes/{pacienteId}/signos-vitales`. Registros **inmutables**
> (sin edicion ni borrado). Al menos un signo debe informarse por registro.

#### POST /api/pacientes/{pacienteId}/signos-vitales

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`.
- **Cuerpo** (`SignosVitalesRequest`):

| Campo | Tipo | Rango / Reglas |
| --- | --- | --- |
| `fechaHora` | `Instant` | Opcional; si falta usa el reloj del servidor; no futura |
| `presionSistolica` | int | 50-300 mmHg; debe ser mayor que la diastolica |
| `presionDiastolica` | int | 30-200 mmHg; estrictamente menor que la sistolica |
| `glucosaMgDl` | int | 20-800 mg/dL |
| `temperaturaC` | decimal | 30.0-45.0 grados C |
| `observaciones` | string | Opcional, max 500 |

```json
{
  "fechaHora": "2025-03-14T08:00:00Z",
  "presionSistolica": 130,
  "presionDiastolica": 85,
  "glucosaMgDl": 110,
  "temperaturaC": 36.7,
  "observaciones": "Paciente estable"
}
```

- **Respuesta `201 Created`** (`SignosVitalesResponse`): `id`, `pacienteId`,
  `registradoPor`, `fechaHora`, `presionSistolica`, `presionDiastolica`,
  `glucosaMgDl`, `temperaturaC`, `observaciones`.
- **Errores:** `400 VALIDACION` (sin signos, fuera de rango, presion incoherente,
  fecha futura), `404 NO_ENCONTRADO`, `409 CONFLICTO` (residente inactivo).

#### GET /api/pacientes/{pacienteId}/signos-vitales

- **Roles:** los tres (segun alcance).
- **Query:** `desde` (opcional), `hasta` (opcional), `pagina`, `tamano`. Orden
  por fecha descendente.
- **Respuesta `200 OK`:** `PageResponse<SignosVitalesResponse>`.

#### GET /api/pacientes/{pacienteId}/signos-vitales/ultimo

- **Roles:** los tres (segun alcance).
- **Respuesta `200 OK`:** `SignosVitalesResponse` (el registro mas reciente).
- **Errores:** `404 NO_ENCONTRADO` si el residente no tiene registros o esta
  fuera de alcance.

---

### 8.12 Especialidades

#### GET /api/especialidades

- **Roles:** `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`.
- **Respuesta `200 OK`:** `List<EspecialidadResponse>`.

```json
[ { "id": 1, "nombre": "Medicina General" }, { "id": 2, "nombre": "Cardiologia" } ]
```

---

### 8.13 Salud del servicio

#### GET /actuator/health

- **Roles:** publico (sin JWT).
- **Respuesta `200 OK`:** estado de salud (sin detalles internos).

```json
{ "status": "UP" }
```

---

## 9. Enumeraciones y modelos de datos

### 9.1 Enumeraciones

| Enum | Valores | Uso |
| --- | --- | --- |
| `RolNombre` | `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO` | Rol de usuario |
| `Sexo` | `MASCULINO`, `FEMENINO`, `OTRO`, `NO_ESPECIFICA` | Sexo del paciente |
| `EstadoCita` | `PROGRAMADA`, `CUMPLIDA`, `INCUMPLIDA`, `CANCELADA`, `REPROGRAMADA` | Estado de cita |
| `EstadoToma` | `ADMINISTRADA`, `OMITIDA` | Estado de una toma registrada |
| `ViaAdministracion` | `ORAL`, `SUBCUTANEA`, `INTRAMUSCULAR`, `TOPICA`, `OTRA` | Via de un esquema |

Estados calculados (no persistidos) de un slot de agenda: `PENDIENTE`,
`ATRASADA`, `ADMINISTRADA`, `OMITIDA`.

### 9.2 Modelos (DTOs) principales

| DTO | Donde se usa | Campos clave |
| --- | --- | --- |
| `UsuarioAutenticadoResponse` | login, `GET /auth/me` | `id`, `nombreCompleto`, `correo`, `rol` |
| `UsuarioResponse` | CRUD de usuarios | agrega `telefono`, `activo`, marcas de tiempo |
| `PacienteResumenResponse` | listado de pacientes | `id`, `nombreCompleto`, `edad`, `habitacion`, `activo` |
| `PacienteDetalleResponse` | detalle/creacion de pacientes | mas `documento`, `situacionClinica`, `contactos` |
| `ContactoEmergenciaResponse` | contactos | `nombre`, `parentesco`, `telefono`, `principal` |
| `VinculacionFamiliarResponse` | familiares | `usuarioId`, `pacienteId`, `parentesco`, `autorizado` |
| `AsignacionCuidadorResponse` | cuidadores | `usuarioId`, `pacienteId`, `desde`, `hasta` |
| `MedicamentoResponse` | catalogo | `nombre`, `presentacion`, `activo` |
| `EsquemaResponse` | esquemas | mas `dosis`, `frecuenciaHoras`, `via`, `horarios` |
| `AgendaEsquemaResponse` | agenda | `slots[]` con `fechaHoraProgramada`, `estado` |
| `RegistroTomaResponse` | historial/registro | `esquemaId`, `registradoPor`, `estado` |
| `CitaResponse` | citas | `especialidadNombre`, `profesional`, `fechaHora`, `estado` |
| `SignosVitalesResponse` | bitacora | `registradoPor`, signos, `observaciones` |
| `PageResponse<T>` | listados paginados | `contenido`, `pagina`, `tamano`, `total`, `totalPaginas` |
| `ApiError` | errores | `timestamp`, `status`, `codigo`, `mensaje`, `detalles` |

---

## 10. Reglas de negocio transversales

- **Borrado logico**: usuarios, pacientes y medicamentos no se borran; se
  activan/desactivan (`PATCH .../estado`). Los contactos si admiten `DELETE`
  fisico; los familiares se revocan (`autorizado = false`) y los cuidadores se
  desasignan (se cierra la asignacion con `hasta = hoy`).
- **Autor de la accion**: campos como `registradoPor` o `registradaPor` salen
  siempre del usuario autenticado, nunca del cuerpo.
- **Inmutabilidad de la bitacora**: los registros de signos vitales no se editan
  ni se borran; una toma tampoco se modifica una vez registrada.
- **Calculo de estados**: `PENDIENTE`/`ATRASADA` de tomas y la vigencia de los
  esquemas se derivan en el backend con el reloj inyectado (`Clock`).
- **Alcance por rol**: ver seccion 6.2; fuera de alcance responde `404`, nunca
  `403`, y en listados el residente simplemente no aparece.
- **Aislamiento por residente**: los subrecursos (`contactos`, `cuidadores`,
  `familiares`, `esquemas`, `tomas`, `citas`, `signos-vitales`) se validan
  contra el `pacienteId` de la ruta; un recurso de otro residente da `404`.
- **Auditoria**: login, cambios de estado, registros y accesos denegados dejan
  traza en la tabla de auditoria (sin datos clinicos ni contrasenas).
- **Validacion en dos capas**: DTOs (`@Valid`) para forma y casos de uso/dominio
  para coherencia de negocio; ambos responden `400 VALIDACION`.

---

## 11. Como probar la API

### 11.1 Swagger UI (solo desarrollo)

Con el perfil `dev` (`meditrack.swagger-habilitado=true`):

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- El contrato versionado en el repositorio esta en `docs/api/openapi.yaml`.

En produccion Swagger queda deshabilitado.

### 11.2 Flujo minimo con curl

1. Iniciar sesion:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"admin@meditrack.co","contrasena":"ClaveSegura123"}'
```

2. Copiar el `token` de la respuesta y usarlo:

```bash
curl http://localhost:8080/api/pacientes \
  -H "Authorization: Bearer <TOKEN>"
```

3. Registrar signos vitales (administrador o cuidador):

```bash
curl -X POST http://localhost:8080/api/pacientes/3/signos-vitales \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"presionSistolica":130,"presionDiastolica":85,"temperaturaC":36.7}'
```

### 11.3 Requisitos previos

- Base de datos levantada (`docker compose up -d`) y backend en marcha con
  `mvn spring-boot:run` (perfil `dev`).
- Usuario administrador inicial creado por `AdministradorInicialRunner` a
  partir de las variables de entorno.

---

## 12. Estado de implementacion por modulo

| Modulo | Endpoints | Roles | Alcance por rol | Auditoria |
| --- | --- | --- | --- | --- |
| Autenticacion/Usuarios | Completos | Implementados | - | Login y cambios |
| Pacientes | Completos | Implementados | Implementado | Cambios de estado |
| Medicacion | Completos | Implementados | Implementado | Tomas y esquemas |
| Citas | Completos | Implementados | Implementado | Citas |
| Bitacora | Completos | Implementados | Implementado | Registros |

> El detalle de la capa de seguridad (JWT, BCrypt, filtros, CORS, auditoria)
> esta en `docs/seguridad.md`. El modelo de datos (tablas, CHECK y relaciones)
> esta en `docs/modelo-datos.md`.
