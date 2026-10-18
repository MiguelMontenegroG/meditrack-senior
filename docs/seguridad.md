# Seguridad - MediTrack Senior

Documentacion de la capa de seguridad del backend: autenticacion con JWT,
autorizacion por roles, CORS, auditoria, administrador inicial y exposicion de
Swagger. Cumple los lineamientos de la Ley 1581 (proteccion de datos personales)
al no registrar contrasenas ni tokens y al trazar los accesos a datos sensibles.

## 1. Resumen del modelo

- API **stateless**: no se usan sesiones HTTP ni cookies de sesion.
- Autenticacion mediante **JWT firmado con HS256**.
- Autorizacion por **roles** (RBAC) con `@PreAuthorize`.
- Autorizacion **fina por rol** en los controladores (method security).
- Errores de seguridad en **JSON** con el mismo formato del resto de la API.
- Contrasenas cifradas con **BCrypt**.

## 2. Flujo de autenticacion

1. El cliente envia `POST /api/auth/login` con `correo` y `contrasena`.
2. El caso de uso `AutenticarUsuarioUseCase`:
   - busca el usuario por correo (comparando en minusculas),
   - verifica el hash con BCrypt,
   - rechaza usuarios inexistentes o con `activo = false`,
   - genera un JWT firmado y registra el acceso en `auditoria_acceso`.
3. El cliente envia el token en las siguientes peticiones:
   `Authorization: Bearer <token>`.
4. En cada peticion, `JwtAuthenticationFilter`:
   - valida la firma y la expiracion del token,
   - carga el usuario (rechaza si no existe o esta desactivado),
   - coloca la autenticacion en el `SecurityContext`.

Respuesta de login:

```json
{
  "token": "eyJ...",
  "tipo": "Bearer",
  "expiraEnSegundos": 28800,
  "usuario": {
    "id": 1,
    "nombreCompleto": "Administrador inicial",
    "correo": "admin@meditrack.co",
    "rol": "ADMINISTRADOR"
  }
}
```

## 3. Estructura del token JWT

El token incluye unicamente los claims minimos:

| Claim | Descripcion |
| --- | --- |
| `sub` | Identificador del usuario |
| `rol` | Nombre del rol (`ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`) |
| `iat` | Fecha de emision |
| `exp` | Fecha de expiracion |

No se incluyen datos personales (correo, nombre) ni informacion clinica.

- Algoritmo: **HS256**.
- Secreto: variable de entorno `JWT_SECRET` (minimo 32 bytes; el arranque falla si
  es mas corto).
- Vigencia: variable de entorno `JWT_EXPIRATION_MINUTES`.

## 4. Roles y permisos

| Rol | Descripcion | Acceso |
| --- | --- | --- |
| `ADMINISTRADOR` | Acceso completo | Gestion de usuarios y pacientes (lectura y escritura) |
| `CUIDADOR_ENFERMERO` | Personal de cuidado | Lectura de los pacientes con asignacion vigente |
| `FAMILIAR_AUTORIZADO` | Familiar | Lectura de los pacientes con vinculacion autorizada |

> Nota: el nombre exacto del enum es `CUIDADOR_ENFERMERO`.

En esta entrega:

- La administracion de usuarios (`/api/usuarios/**`) esta restringida a
  `ADMINISTRADOR` mediante `@PreAuthorize("hasRole('ADMINISTRADOR')")` a nivel de
  controlador.
- El modulo de pacientes (`/api/pacientes/**`, contactos, familiares y cuidadores)
  aplica el alcance del usuario (ver seccion 13): la lectura se filtra en el
  backend y la escritura exige `ADMINISTRADOR`.

## 5. Rutas publicas y protegidas

Publicas (sin token):

- `POST /api/auth/login`
- `GET /actuator/health`
- Swagger UI y `/v3/api-docs/**` (solo cuando `meditrack.swagger-habilitado = true`,
  es decir, en el perfil `dev`).

Protegidas (requieren JWT valido):

- Todo lo demas, incluido `/api/auth/me` y `/api/auth/contrasena`.

## 6. Errores de seguridad

| Codigo HTTP | Codigo de negocio | Cuando |
| --- | --- | --- |
| 400 | `VALIDACION` | Cuerpo de la peticion invalido |
| 401 | `CREDENCIALES_INVALIDAS` | Login con correo o contrasena incorrectos, o cuenta desactivada |
| 401 | `NO_AUTENTICADO` | Falta un token valido en una ruta protegida |
| 403 | `ACCESO_DENEGADO` | El usuario autenticado no tiene el rol requerido |
| 404 | `NO_ENCONTRADO` | El recurso no existe |
| 409 | `CONFLICTO` | Regla de negocio violada (p. ej. correo duplicado) |
| 500 | `ERROR_INTERNO` | Error no controlado (sin detalles internos) |

Formato unico:

```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 401,
  "codigo": "NO_AUTENTICADO",
  "mensaje": "Se requiere autenticacion para acceder a este recurso"
}
```

## 7. CORS

- Los origenes permitidos se definen en la variable `CORS_ALLOWED_ORIGINS`
  (lista separada por comas). Nunca se usa `*`.
- Metodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Cabeceras permitidas: `Authorization`, `Content-Type`, `Accept`.

## 8. Reglas de negocio de seguridad

- No se permite desactivar la propia cuenta (`PATCH /api/usuarios/{id}/estado`).
- No se puede dejar el sistema sin administradores activos: si se intenta
  desactivar o cambiar el rol del ultimo administrador activo, se responde `409`.
- No se permite quitarse el propio rol de administrador.
- No hay borrado fisico de usuarios: el estado se controla con `activo`.

## 9. Administrador inicial

`AdministradorInicialRunner` (idempotente) crea el administrador inicial solo si:

- no existe ningun usuario con rol `ADMINISTRADOR`, y
- estan definidas `ADMIN_INITIAL_EMAIL` y `ADMIN_INITIAL_PASSWORD`.

Si faltan las variables, registra un aviso sin exponer valores y continua el
arranque. La contrasena y el hash nunca se registran en el log.

## 10. Auditoria

Toda operacion sensible se registra en `auditoria_acceso` (tabla append-only)
dentro de una transaccion nueva (`REQUIRES_NEW`), de modo que los accesos
denegados no se pierden si la operacion principal falla.

Eventos auditados actualmente:

- `LOGIN` (exitoso y denegado),
- `CREATE`/`UPDATE` de usuarios (alta, actualizacion, activacion/desactivacion y
  cambio de contrasena),
- `CREATE`/`UPDATE` de pacientes (alta, actualizacion y activacion/desactivacion),
- `CREATE`/`UPDATE`/`DELETE` de contactos de emergencia, vinculaciones de
  familiares y asignaciones de cuidadores,
- `READ` de pacientes y contactos (exitoso y denegado),
- `CREATE`/`UPDATE` de medicamentos y esquemas,
- `CREATE` de tomas y `READ` de esquemas, agenda e historial (exitoso y denegado),
- `CREATE`/`UPDATE` de citas y `READ` de citas (detalle y calendario; exitoso y denegado).

El campo `detalle` nunca contiene contrasenas, tokens ni datos personales: solo
describe la operacion realizada.

## 11. Swagger

- Deshabilitado por defecto.
- Habilitado solo en el perfil `dev` (`meditrack.swagger-habilitado = true`).
- Incluye el esquema de seguridad Bearer para probar la API con el boton
  "Authorize".

## 12. Variables de entorno

Ver `.env.example`. Las relevantes para seguridad:

- `JWT_SECRET` (minimo 32 bytes),
- `JWT_EXPIRATION_MINUTES`,
- `CORS_ALLOWED_ORIGINS`,
- `ADMIN_INITIAL_EMAIL`,
- `ADMIN_INITIAL_PASSWORD`.

## 13. Alcance sobre los pacientes (modulo paciente)

El acceso a un paciente se decide en el backend (no en el cliente) con la regla
de alcance `AlcancePaciente`:

- `ADMINISTRADOR`: ve todos los pacientes, activos e inactivos, y es el unico que
  crea, edita, activa/desactiva y gestiona contactos, familiares y cuidadores.
- `CUIDADOR_ENFERMERO`: solo lectura de los pacientes con una **asignacion
  vigente** (`asignacion_cuidador.hasta IS NULL`). Nunca ve inactivos.
- `FAMILIAR_AUTORIZADO`: solo lectura de los pacientes con una **vinculacion
  autorizada** (`vinculacion_familiar.autorizado = true`). Nunca ve inactivos.

Reglas aplicadas:

- El filtrado se hace en la consulta (paginacion y conteo coherentes), no
  ocultando campos despues de traerlos.
- Un paciente inexistente o fuera del alcance responde **404** (nunca 403) para no
  revelar su existencia; el intento se audita como `READ` **DENEGADO**.
- La lista de pacientes no expone la situacion clinica; el detalle si, y solo
  dentro del alcance.
- Un usuario autenticado sin un rol conocido no obtiene alcance (no se otorga
  acceso por defecto).
- La escritura exige `ADMINISTRADOR` con `@PreAuthorize("hasRole('ADMINISTRADOR')")`.

## 14. Alcance sobre la medicacion (modulo medicacion)

El modulo de medicacion reutiliza el alcance del modulo paciente a traves del
adaptador `PacienteAlcanceAdapter` (no duplica la logica de visibilidad ni accede
a las tablas del modulo paciente de forma directa). Antes de operar sobre un
residente se resuelve su visibilidad con `VerificadorAlcanceResidente`:

- `ADMINISTRADOR`: catalogo de medicamentos, esquemas y horarios; lectura y
  escritura; registra tomas de cualquier residente existente.
- `CUIDADOR_ENFERMERO`: lectura de esquemas y tomas, y registro de tomas, solo de
  residentes con **asignacion vigente**. No crea ni edita esquemas ni catalogo.
- `FAMILIAR_AUTORIZADO`: solo lectura de esquemas, agenda e historial de
  residentes con **vinculacion autorizada**. Nunca escribe.

Reglas aplicadas:

- Un residente inexistente o fuera de alcance responde **404** (nunca 403) y se
  audita como `READ` **DENEGADO**; un residente inactivo responde **409** en las
  operaciones de escritura (no admite nuevos esquemas ni tomas).
- La agenda del dia y los estados **PENDIENTE** y **ATRASADA** se calculan en el
  dominio (`CalendarioTomas`) y **nunca se persisten**: la tabla `registro_toma`
  solo admite `ADMINISTRADA` y `OMITIDA`.
- Los registros de toma son **append-only** (Ley 1581): nunca se editan ni se
  borran; un error se corrige con una nueva observacion.
- El autor de una toma (`registradoPor`) sale siempre del usuario autenticado,
  nunca del cuerpo de la peticion.
- El alcance se resuelve con metodos separados por rol (igual que en el modulo
  paciente) para evitar el problema de tipado de Hibernate 6.6.

Eventos auditados del modulo: `CREATE`/`UPDATE` de medicamentos y esquemas,
`CREATE` de tomas y `READ` de esquemas, agenda e historial (EXITOSO y DENEGADO).
## 15. Alcance sobre las citas (modulo cita)

El modulo de citas reutiliza el alcance del modulo paciente a traves del mismo
`VerificadorAlcanceResidente` y `PacienteAlcancePort` del modulo medicacion (no
duplica la regla de visibilidad ni accede a las tablas del modulo paciente de
forma directa). Antes de operar sobre un residente se resuelve su visibilidad con
`VerificadorAlcanceResidente`:

- `ADMINISTRADOR`: agenda, edita, cancela y marca estado de citas de cualquier
  residente existente; ve todo el calendario.
- `CUIDADOR_ENFERMERO`: lectura de citas y marcado de `CUMPLIDA`/`INCUMPLIDA`
  solo de residentes con **asignacion vigente**. No agenda, no edita ni cancela.
- `FAMILIAR_AUTORIZADO`: solo lectura de citas de residentes con **vinculacion
  autorizada**. Nunca escribe ni marca estado.

Endpoints (todos requieren JWT valido):

- `GET /api/citas?desde&hasta[&estado][&pacienteId][&pagina][&tamano]`: calendario
  por rango de fechas, paginado (tamano acotado a 100) y filtrable opcionalmente por
  residente; los tres roles (lectura segun alcance). Un residente fuera de alcance o
  inexistente en el filtro `pacienteId` devuelve una pagina vacia (total 0), sin
  revelar su existencia.
- `GET /api/pacientes/{pacienteId}/citas/{citaId}`: detalle (los tres roles).
- `POST /api/pacientes/{pacienteId}/citas`: agendar (solo `ADMINISTRADOR`).
- `PUT /api/pacientes/{pacienteId}/citas/{citaId}`: editar (solo `ADMINISTRADOR`).
- `PATCH /api/pacientes/{pacienteId}/citas/{citaId}/estado`: cambiar estado
  (`ADMINISTRADOR` o `CUIDADOR_ENFERMERO`; `CANCELADA` solo `ADMINISTRADOR`).
- `GET /api/especialidades`: catalogo (solo lectura, los tres roles).

Reglas aplicadas:

- El residente inexistente o fuera de alcance responde **404** (nunca 403) y se
  audita como `READ` **DENEGADO**; un residente inactivo responde **409** en las
  operaciones de escritura (agendar, editar, cambiar estado).
- La fecha/hora de la cita no puede estar en el pasado al agendar o editar
  (**400**).
- Solo se edita, cancela o cambia de estado una cita en estado inicial
  (`PROGRAMADA`); el resto responde **409**.
- `CUMPLIDA`/`INCUMPLIDA` solo se marcan sobre citas cuya fecha/hora ya llego
  (**400** si es futura). `CANCELADA` es exclusiva del administrador (**403** para
  el resto). Registrar `PROGRAMADA` o `REPROGRAMADA` como destino responde
  **400**: la reprogramacion no es una operacion del producto.
- Si se indica una especialidad, debe existir en el catalogo (**404** en caso
  contrario). El catalogo (`catalogo_especialidad`) no tiene columna de estado:
  no existe el concepto de "especialidad activa".
- El autor de la cita (`registradaPor`) sale siempre del usuario autenticado,
  nunca del cuerpo de la peticion.
- El calendario exige `desde <= hasta` y filtra por el conjunto de residentes
  visibles resuelto con las consultas de alcance del modulo paciente; nunca se
  expone una cita de un residente fuera de alcance.
- Los filtros opcionales (estado y rango de fechas) se resuelven con metodos de
  repositorio separados por combinacion, para no enviar parametros nulos a
  PostgreSQL (misma tecnica que en paciente y medicacion).

Eventos auditados del modulo: `CREATE` de citas, `UPDATE` de citas (edicion y
cambio de estado) y `READ` de citas (detalle y calendario); el intento sobre un
residente fuera de alcance se audita como `READ` **DENEGADO** (via
`VerificadorAlcanceResidente`).
