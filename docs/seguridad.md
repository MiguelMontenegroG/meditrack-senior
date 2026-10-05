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
| `ADMINISTRADOR` | Acceso completo | Gestion de usuarios y operacion del centro |
| `CUIDADOR_ENFERmero` | Personal de cuidado | Registra bitacoras, medicacion y tareas del dia |
| `FAMILIAR_AUTORIZADO` | Familiar | Solo lectura de informacion de sus familiares |

> Nota: el nombre correcto del rol es `CUIDADOR_ENFERMERO` (la tabla anterior
> muestra el valor exacto del enum).

En esta entrega la administracion de usuarios (`/api/usuarios/**`) esta restringida
a `ADMINISTRADOR` mediante `@PreAuthorize("hasRole('ADMINISTRADOR')")` a nivel de
controlador.

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
  cambio de contrasena).

El campo `detalle` nunca contiene contrasenas ni tokens.

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
