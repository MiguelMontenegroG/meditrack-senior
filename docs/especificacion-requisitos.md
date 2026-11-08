# Especificacion de Requisitos - MediTrack Senior

Documento de Especificacion de Requisitos Funcionales (RF) y No Funcionales
(RNF) de **MediTrack Senior**, sistema de gestion clinica para centros de
atencion geriatrica.

> **Convencion de estado.** Cada requisito lleva una etiqueta:
> **[Implementado]** verificado en el codigo del repositorio;
> **[En construccion]** pantalla o modulo con estructura creada pero sin
> funcionalidad completa; **[Pendiente]** planificado y aun no construido.
> El estado refleja el repositorio al momento de redactar.

---

## Indice

1. [Proposito y alcance](#1-proposito-y-alcance)
2. [Stakeholders y usuarios](#2-stakeholders-y-usuarios)
3. [Glosario](#3-glosario)
4. [Requisitos funcionales](#4-requisitos-funcionales)
5. [Requisitos no funcionales](#5-requisitos-no-funcionales)
6. [Reglas de negocio](#6-reglas-de-negocio)
7. [Indicadores (KPIs) y modelo de datos](#7-indicadores-kpis-y-modelo-de-datos)
8. [Matriz de trazabilidad](#8-matriz-de-trazabilidad)
9. [Estado de implementacion](#9-estado-de-implementacion)
10. [Supuestos y limitaciones](#10-supuestos-y-limitaciones)
11. [Documentos relacionados](#11-documentos-relacionados)

---

## 1. Proposito y alcance

### 1.1 Proposito

MediTrack Senior es una aplicacion web **responsive** (usable tambien en
telefono movil) que permite al personal de un centro de atencion geriatrica
llevar el seguimiento clinico de los adultos mayores residentes y mantener
informadas a sus familias de forma clara y segura.

Este documento define **que debe hacer** el sistema (requisitos funcionales) y
**con que calidad y restricciones** (requisitos no funcionales), sirviendo de
base para el diseno, la implementacion, las pruebas y la sustentacion del
proyecto.

- **Contexto academico:** curso Ingenieria de Software III, Universidad del
  Quindio.
- **Datos sensibles:** el sistema trata datos personales de salud y queda sujeto
  a la **Ley 1581 de 2012** (proteccion de datos personales en Colombia). Esto
  obliga a control de acceso por rol, minimizacion de datos y trazabilidad
  (auditoria) de los accesos.

### 1.2 Alcance funcional

El sistema cubre los siguientes procesos de negocio:

| Proceso | Contenido | Estado general |
| --- | --- | --- |
| **Proceso 1** | Perfiles de residentes y esquemas de medicacion (catalogo, horarios y registro de tomas). | Implementado |
| **Proceso 2** | Citas medicas y bitacora de signos vitales (presion arterial, glucosa y temperatura). | Implementado |
| **Proceso 3** | Alertas, dashboard de indicadores y reportes clinicos en PDF. | Pendiente |

Tambien forma parte del alcance la **gestion de usuarios y roles** (transversal
a los tres procesos), implementada en el modulo `usuario`.

### 1.3 Fuera de alcance

Los siguientes elementos **no** forman parte de esta version:

- Integracion con dispositivos IoT.
- Telemedicina.
- Facturacion.
- Interoperabilidad con RIPS/EPS.
- Gestion de multiples centros geriatricos (el modelo asume **un solo centro**;
  ver seccion 10).
---

## 2. Stakeholders y usuarios

### 2.1 Roles del sistema

El control de acceso es por **roles (RBAC)**. El nombre tecnico de cada rol es
el valor del enum almacenado en la base de datos.

| Rol | Descripcion | Permisos generales |
| --- | --- | --- |
| **ADMINISTRADOR** | Autoridad clinica y administrativa del centro. | Acceso completo: gestiona usuarios, residentes, catalogo de medicamentos, esquemas y citas; consulta la bitacora. Unico rol que crea y edita datos maestros. |
| **CUIDADOR_ENFERMERO** | Personal de cuidado y enfermeria. | Ejecuta el tratamiento: registra tomas, registra signos vitales y marca el estado de citas cumplidas o incumplidas. Lectura de los residentes que tiene asignados. |
| **FAMILIAR_AUTORIZADO** | Familiar de un residente, con autorizacion expresa. | **Solo lectura** de la informacion del residente con el que tiene vinculacion autorizada. Nunca ve acciones de crear, editar ni eliminar. |

### 2.2 Otros interesados

- **Direccion del centro:** consulta indicadores operativos y clinicos.
- **Auditoria y cumplimiento:** revisa la trazabilidad de accesos (Ley 1581).

---

## 3. Glosario

| Termino | Significado |
| --- | --- |
| **Residente / Paciente** | Adulto mayor atendido por el centro. En el codigo el modulo se llama `paciente`. |
| **Cuidador** | Usuario con rol `CUIDADOR_ENFERMERO` asignado a uno o varios residentes. |
| **Familiar autorizado** | Usuario con rol `FAMILIAR_AUTORIZADO` vinculado a un residente. |
| **Vinculacion** | Relacion entre un familiar y un residente que habilita su lectura. |
| **Asignacion** | Relacion vigente entre un cuidador y un residente. |
| **Catalogo de medicamentos** | Lista maestra de medicamentos reutilizables. |
| **Esquema de medicacion** | Receta de un residente: medicamento, dosis, frecuencia, via y vigencia. |
| **Horario de medicacion** | Horas exactas del dia en que corresponde una toma de un esquema. |
| **Toma** | Evento de administracion u omision de una dosis a una hora programada. |
| **Slot de toma** | Celda de la agenda diaria cuyo estado se deriva (`PENDIENTE`, `ATRASADA`, `ADMINISTRADA`, `OMITIDA`). |
| **Bitacora** | Registro historico de signos vitales de un residente. |
| **Cita** | Control medico agendado con especialidad, fecha/hora, profesional y lugar. |
| **Alcance** | Conjunto de residentes que un usuario puede ver; se resuelve en el backend segun su rol. |
| **Auditoria** | Registro inmutable (append-only) de accesos y operaciones sensibles. |
| **JWT** | Token firmado que identifica al usuario en cada peticion. |
| **KPI** | Indicador clave de desempeno mostrado en el dashboard. |

---

## 4. Requisitos funcionales

Se agrupan por modulo. Cada requisito tiene un identificador
`RF-<modulo>-<n>`, una descripcion, los actores y su estado.

### 4.1 Autenticacion y sesion

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-AUTH-01** | El sistema debe permitir iniciar sesion con correo y contrasena y devolver un token JWT firmado con los datos basicos del usuario y su rol. | Todos | Implementado |
| **RF-AUTH-02** | El sistema debe rechazar el inicio de sesion con un mensaje generico cuando las credenciales son invalidas, sin revelar si el correo existe. | Todos | Implementado |
| **RF-AUTH-03** | El sistema no debe permitir iniciar sesion a usuarios con la cuenta desactivada. | Todos | Implementado |
| **RF-AUTH-04** | El sistema debe permitir a un usuario autenticado consultar su propia informacion (`GET /api/auth/me`). | Todos | Implementado |
| **RF-AUTH-05** | El sistema debe permitir a cualquier usuario autenticado cambiar **su propia** contrasena, exigiendo la contrasena actual. | Todos | Implementado |
| **RF-AUTH-06** | El frontend debe cerrar la sesion automaticamente cuando el token vence o el backend responde `401 NO_AUTENTICADO`. | Todos | Implementado |

### 4.2 Gestion de usuarios (modulo `usuario`)

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-USR-01** | El sistema debe permitir al administrador crear usuarios con nombre, correo, contrasena inicial, telefono y rol. | ADMINISTRADOR | Implementado |
| **RF-USR-02** | El correo debe ser unico (comparado sin distinguir mayusculas); un correo repetido responde `409 CONFLICTO`. | ADMINISTRADOR | Implementado |
| **RF-USR-03** | El sistema debe permitir listar usuarios con filtros por rol y estado y con paginacion. | ADMINISTRADOR | Implementado |
| **RF-USR-04** | El sistema debe permitir consultar el detalle de un usuario por identificador. | ADMINISTRADOR | Implementado |
| **RF-USR-05** | El sistema debe permitir actualizar los datos de un usuario (nombre, telefono y rol). | ADMINISTRADOR | Implementado |
| **RF-USR-06** | El sistema debe permitir activar y desactivar usuarios mediante **borrado logico** (campo `activo`); nunca se borra fisicamente. | ADMINISTRADOR | Implementado |
| **RF-USR-07** | El sistema debe impedir que un administrador se desactive a si mismo o se quite su propio rol (`409`). | ADMINISTRADOR | Implementado |
| **RF-USR-08** | El sistema no debe permitir dejar el sistema sin administradores activos (`409`). | ADMINISTRADOR | Implementado |
| **RF-USR-09** | El sistema debe crear un administrador inicial de forma idempotente en el arranque si no existe ninguno y estan definidas las variables de entorno correspondientes. | Sistema | Implementado |
### 4.3 Perfiles de residentes (modulo `paciente`)

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-PAC-01** | El sistema debe permitir registrar un residente con documento, nombres, apellidos, fecha de nacimiento, sexo, situacion clinica y, opcionalmente, habitacion. | ADMINISTRADOR | Implementado |
| **RF-PAC-02** | El documento de identidad debe ser unico; un documento repetido responde `409`. | ADMINISTRADOR | Implementado |
| **RF-PAC-03** | La fecha de nacimiento no puede ser futura (`400 VALIDACION`). | ADMINISTRADOR | Implementado |
| **RF-PAC-04** | El sistema debe calcular y mostrar la edad del residente a partir de su fecha de nacimiento. | Todos | Implementado |
| **RF-PAC-05** | Un residente activo debe tener situacion clinica registrada; la habitacion es opcional. | ADMINISTRADOR | Implementado |
| **RF-PAC-06** | El sistema debe permitir listar residentes de forma paginada, con busqueda por texto y filtro por estado, respetando el alcance del rol. | Todos | Implementado |
| **RF-PAC-07** | El sistema debe permitir consultar el detalle de un residente, incluida su situacion clinica, solo dentro del alcance del rol. | Todos | Implementado |
| **RF-PAC-08** | El sistema debe permitir editar los datos de un residente. | ADMINISTRADOR | Implementado |
| **RF-PAC-09** | El sistema debe permitir activar y desactivar residentes (borrado logico); la desactivacion exige un motivo que se registra en auditoria. | ADMINISTRADOR | Implementado |
| **RF-PAC-10** | El resumen del listado no debe exponer la situacion clinica; solo el detalle lo hace. | Sistema | Implementado |

### 4.4 Contactos de emergencia

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-CON-01** | El sistema debe permitir registrar contactos de emergencia de un residente (nombre, parentesco, telefono). | ADMINISTRADOR | Implementado |
| **RF-CON-02** | Debe existir como maximo **un contacto principal** por residente; al marcar uno nuevo, el anterior deja de ser principal de forma atomica. | ADMINISTRADOR | Implementado |
| **RF-CON-03** | El sistema debe permitir listar, editar y eliminar contactos de emergencia. | ADMINISTRADOR | Implementado |
| **RF-CON-04** | Los contactos de emergencia solo son visibles dentro del alcance del rol del usuario. | Todos | Implementado |

### 4.5 Vinculacion de familiares

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-FAM-01** | El sistema debe permitir vincular un usuario con rol `FAMILIAR_AUTORIZADO` a un residente, indicando parentesco. | ADMINISTRADOR | Implementado |
| **RF-FAM-02** | Solo se puede vincular a usuarios activos con rol `FAMILIAR_AUTORIZADO`; en otro caso responde `409`. | ADMINISTRADOR | Implementado |
| **RF-FAM-03** | El sistema no debe permitir dos vinculaciones vigentes del mismo familiar al mismo residente. | ADMINISTRADOR | Implementado |
| **RF-FAM-04** | El sistema debe permitir **revocar** una vinculacion sin borrado fisico (marca `autorizado = false`); revincular reactiva el mismo registro. | ADMINISTRADOR | Implementado |
| **RF-FAM-05** | Un familiar autorizado solo puede ver los residentes con vinculacion autorizada. | FAMILIAR_AUTORIZADO | Implementado |

### 4.6 Asignacion de cuidadores

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-CUI-01** | El sistema debe permitir asignar un cuidador a un residente con fecha de inicio de vigencia. | ADMINISTRADOR | Implementado |
| **RF-CUI-02** | Solo se puede asignar a usuarios activos con rol `CUIDADOR_ENFERMERO`; en otro caso responde `409`. | ADMINISTRADOR | Implementado |
| **RF-CUI-03** | Debe existir como maximo una asignacion vigente por par cuidador-residente. | ADMINISTRADOR | Implementado |
| **RF-CUI-04** | El sistema debe permitir **desasignar** un cuidador cerrando la vigencia con la fecha del dia, sin borrado fisico. | ADMINISTRADOR | Implementado |
| **RF-CUI-05** | Un cuidador solo puede ver y operar sobre residentes con asignacion vigente. | CUIDADOR_ENFERMERO | Implementado |

### 4.7 Catalogo de medicamentos (modulo `medicacion`)

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-MED-01** | El sistema debe permitir administrar un catalogo de medicamentos (nombre y presentacion). | ADMINISTRADOR | Implementado |
| **RF-MED-02** | El nombre del medicamento debe ser unico (sin distinguir mayusculas); un nombre repetido responde `409`. | ADMINISTRADOR | Implementado |
| **RF-MED-03** | El sistema debe permitir listar el catalogo de forma paginada y filtrada por texto y estado. | ADMINISTRADOR, CUIDADOR_ENFERMERO | Implementado |
| **RF-MED-04** | El sistema debe permitir editar y activar o desactivar medicamentos del catalogo. | ADMINISTRADOR | Implementado |

### 4.8 Esquemas de medicacion

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-ESQ-01** | El sistema debe permitir crear un esquema de medicacion para un residente: medicamento, dosis, frecuencia en horas (1-24), via de administracion, horarios, vigencia e indicaciones. | ADMINISTRADOR | Implementado |
| **RF-ESQ-02** | Cada esquema debe tener al menos un horario; las horas del dia se registran sin duplicados. | ADMINISTRADOR | Implementado |
| **RF-ESQ-03** | El sistema no debe permitir dos esquemas del mismo medicamento con vigencias solapadas para un residente (`409`). | ADMINISTRADOR | Implementado |
| **RF-ESQ-04** | El sistema debe permitir listar los esquemas de un residente filtrando por estado y vigencia. | Todos (por alcance) | Implementado |
| **RF-ESQ-05** | El sistema debe permitir consultar el detalle de un esquema. | Todos (por alcance) | Implementado |
| **RF-ESQ-06** | El sistema debe permitir editar un esquema y activarlo o desactivarlo. | ADMINISTRADOR | Implementado |
| **RF-ESQ-07** | Solo el administrador define esquemas; el cuidador los ejecuta. | ADMINISTRADOR | Implementado |

### 4.9 Tomas de medicacion

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-TOM-01** | El sistema debe mostrar la **agenda diaria** de tomas de un residente con el estado calculado de cada slot. | Todos (por alcance) | Implementado |
| **RF-TOM-02** | El estado de cada slot se deriva: `ADMINISTRADA` u `OMITIDA` si hay registro; `ATRASADA` si no hay registro y la hora ya paso; `PENDIENTE` si la hora es futura. | Sistema | Implementado |
| **RF-TOM-03** | El sistema debe permitir registrar una toma como `ADMINISTRADA` u `OMITIDA` para una hora programada. | ADMINISTRADOR, CUIDADOR_ENFERMERO | Implementado |
| **RF-TOM-04** | No se puede registrar una toma para una hora que no corresponda a un horario del esquema (`400`) ni para una hora futura (`400`). | Administrador, Cuidador | Implementado |
| **RF-TOM-05** | No se puede registrar dos veces la toma de la misma hora programada (`409`). | Administrador, Cuidador | Implementado |
| **RF-TOM-06** | Al registrar una toma como `OMITIDA` se debe exigir un motivo (observacion). | Administrador, Cuidador | Implementado |
| **RF-TOM-07** | El sistema debe mostrar el **historial** de tomas con filtros y paginacion. | Todos (por alcance) | Implementado |
| **RF-TOM-08** | El autor de una toma debe ser siempre el usuario autenticado, nunca un dato del cuerpo de la peticion. | Sistema | Implementado |
| **RF-TOM-09** | Los registros de toma son inmutables: no se editan ni se borran. | Sistema | Implementado |
### 4.10 Citas medicas (modulo `cita`)

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-CIT-01** | El sistema debe mostrar un **calendario** de citas por rango de fechas, paginado y filtrable por estado y por residente. | Todos (por alcance) | Implementado |
| **RF-CIT-02** | El sistema debe permitir agendar una cita para un residente con fecha/hora, especialidad, profesional, lugar y motivo. | ADMINISTRADOR | Implementado |
| **RF-CIT-03** | La fecha/hora de la cita no puede estar en el pasado al agendar o editar (`400`). | ADMINISTRADOR | Implementado |
| **RF-CIT-04** | El sistema debe permitir consultar el detalle de una cita. | Todos (por alcance) | Implementado |
| **RF-CIT-05** | El sistema debe permitir editar una cita solo mientras este en estado `PROGRAMADA` (en otro estado `409`). | ADMINISTRADOR | Implementado |
| **RF-CIT-06** | El sistema debe permitir marcar una cita como `CUMPLIDA` o `INCUMPLIDA`, solo si su fecha/hora ya llego. | ADMINISTRADOR, CUIDADOR_ENFERMERO | Implementado |
| **RF-CIT-07** | Cancelar una cita es exclusivo del administrador; otro rol recibe `403`. | ADMINISTRADOR | Implementado |
| **RF-CIT-08** | El sistema debe ofrecer el catalogo de especialidades medicas en modo lectura. | Todos | Implementado |
| **RF-CIT-09** | El autor de una cita debe ser siempre el usuario autenticado. | Sistema | Implementado |

### 4.11 Bitacora de signos vitales (modulo `bitacora`)

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-BIT-01** | El sistema debe permitir registrar signos vitales de un residente: presion sistolica, presion diastolica, glucosa y temperatura. | ADMINISTRADOR, CUIDADOR_ENFERMERO | Implementado |
| **RF-BIT-02** | Al menos un signo vital debe informarse por registro (`400` si no llega ninguno). | Administrador, Cuidador | Implementado |
| **RF-BIT-03** | Los valores deben estar en rangos de plausibilidad: sistolica 50-300 mmHg, diastolica 30-200 mmHg y menor que la sistolica, glucosa 20-800 mg/dL, temperatura 30.0-45.0 grados C (`400` fuera de rango). | Sistema | Implementado |
| **RF-BIT-04** | La fecha de medicion es opcional: si falta se usa el reloj del servidor; no puede ser futura (`400`). | Administrador, Cuidador | Implementado |
| **RF-BIT-05** | El sistema debe permitir consultar el historial paginado de signos vitales, ordenado por fecha descendente y filtrable por rango de fechas. | Todos (por alcance) | Implementado |
| **RF-BIT-06** | El sistema debe permitir consultar el ultimo registro de signos vitales de un residente (`404` si no tiene ninguno). | Todos (por alcance) | Implementado |
| **RF-BIT-07** | Los registros de signos vitales son inmutables: no hay edicion ni borrado. | Sistema | Implementado |
| **RF-BIT-08** | El frontend debe ofrecer un selector de residente para abrir su bitacora y usar `inputmode=\"decimal\"` en los campos numericos. | Todos | Implementado |

### 4.12 Alertas, dashboard y reportes (Proceso 3)

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-ALE-01** | El sistema debe detectar condiciones que requieren atencion y generar alertas: dosis atrasada, bitacora incompleta, signo fuera de rango y cita proxima. | Sistema | Pendiente |
| **RF-ALE-02** | El sistema debe permitir revisar y descartar alertas, registrando quien y cuando. | CUIDADOR_ENFERMERO, ADMINISTRADOR | Pendiente |
| **RF-ALE-03** | La generacion automatica de alertas debe ser idempotente: una misma fuente no debe generar dos veces la misma alerta. | Sistema | Pendiente |
| **RF-DAS-01** | El sistema debe mostrar un dashboard con los indicadores operativos y clinicos definidos en la seccion 7.1. | ADMINISTRADOR | Pendiente |
| **RF-DAS-02** | El frontend puede mostrar un panel con los datos reales ya disponibles; lo que dependa de modulos inexistentes debe rotularse como pendiente y no inventar cifras. | ADMINISTRADOR | En construccion |
| **RF-REP-01** | El sistema debe generar reportes clinicos en PDF (historia clinica, bitacora, medicacion y general) por residente y rango de fechas. | ADMINISTRADOR | Pendiente |
| **RF-REP-02** | El sistema debe registrar los metadatos de cada reporte generado (tipo, rango, autor y fecha) sin almacenar el archivo en la base de datos. | Sistema | Pendiente |

### 4.13 Trazabilidad y auditoria

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-AUD-01** | El sistema debe registrar en una tabla **append-only** los accesos y operaciones sensibles: login/logout, gestion de usuarios y roles y acceso a datos clinicos. | Sistema | Implementado |
| **RF-AUD-02** | Cada registro de auditoria debe incluir usuario, accion, entidad afectada, fecha/hora, resultado (EXITOSO/DENEGADO) y un detalle sin datos personales ni secretos. | Sistema | Implementado |
| **RF-AUD-03** | Los intentos de acceso a un residente fuera de alcance deben auditarse como `READ` DENEGADO. | Sistema | Implementado |

### 4.14 Navegacion e interfaz por rol

| ID | Requisito | Actores | Estado |
| --- | --- | --- | --- |
| **RF-UI-01** | El sistema debe redirigir al usuario a la ruta inicial de su rol tras iniciar sesion y bloquear las rutas no permitidas. | Todos | Implementado |
| **RF-UI-02** | La navegacion debe filtrarse por rol (sidebar en escritorio y barra inferior en movil). | Todos | Implementado |
| **RF-UI-03** | El familiar autorizado nunca debe ver botones de crear, editar ni eliminar. | FAMILIAR_AUTORIZADO | Implementado |
| **RF-UI-04** | Los estados de carga, error y vacio deben mostrarse de forma accesible y consistente en todas las pantallas. | Todos | Implementado |
| **RF-UI-05** | Las pantallas de alertas y reportes deben mostrar un aviso de seccion en construccion mientras no existan. | ADMINISTRADOR | En construccion |
---

## 5. Requisitos no funcionales

Se agrupan por categoria. Cada uno tiene un identificador `RNF-<categoria>-<n>`.

### 5.1 Seguridad

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-SEG-01** | La API debe ser **stateless** y autenticarse con **JWT firmado con HS256**; el token viaja en la cabecera `Authorization: Bearer`. | Implementado |
| **RNF-SEG-02** | El token debe contener solo los claims minimos (`sub`, `rol`, `iat`, `exp`) y **ningun dato personal ni clinico**. | Implementado |
| **RNF-SEG-03** | El secreto `JWT_SECRET` debe tener al menos 32 bytes; el arranque debe fallar si es mas corto. | Implementado |
| **RNF-SEG-04** | Las contrasenas deben almacenarse como **hash BCrypt**; nunca en texto plano ni en logs. | Implementado |
| **RNF-SEG-05** | La autorizacion debe aplicarse en el **backend** por rol (`@PreAuthorize`) y por alcance; nunca delegarse al cliente. | Implementado |
| **RNF-SEG-06** | Un residente inexistente o fuera de alcance debe responder **404 (nunca 403)** para no revelar su existencia. | Implementado |
| **RNF-SEG-07** | El login con credenciales invalidas debe responder con un mensaje generico, sin revelar si el correo existe. | Implementado |
| **RNF-SEG-08** | CORS debe permitir solo origenes explicitos definidos en `CORS_ALLOWED_ORIGINS`; nunca el comodin `*`. | Implementado |
| **RNF-SEG-09** | El sistema debe minimizar la exposicion de datos: el listado de residentes no expone la situacion clinica y la auditoria no almacena datos sensibles. | Implementado |
| **RNF-SEG-10** | La sesion del frontend debe guardarse en `sessionStorage` (no en `localStorage`) y cerrarse al vencer el token. | Implementado |

### 5.2 Privacidad y cumplimiento (Ley 1581 de 2012)

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-PRI-01** | Toda operacion sobre datos clinicos debe quedar trazada en `auditoria_acceso`. | Implementado |
| **RNF-PRI-02** | La tabla de auditoria debe ser **append-only** (no se actualiza ni se borra desde la aplicacion). | Implementado |
| **RNF-PRI-03** | Los movimientos clinicos (signos vitales, tomas, historial de citas) no deben borrarse fisicamente; se conserva el historial. | Implementado |
| **RNF-PRI-04** | El campo de detalle de la auditoria no debe contener contrasenas, tokens ni datos personales. | Implementado |
| **RNF-PRI-05** | Los datos de prueba del frontend no deben confundirse con datos reales; las cifras de ejemplo deben rotularse como no reales. | Implementado |

### 5.3 Rendimiento y escalabilidad

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-REN-01** | Los listados deben ser **paginados** en el backend, con tamano de pagina por defecto de 20 y maximo de 100. | Implementado |
| **RNF-REN-02** | El filtrado por alcance y la paginacion deben resolverse en la consulta (sin cargar toda la tabla en memoria). | Implementado |
| **RNF-REN-03** | Los filtros opcionales deben resolverse con consultas separadas por combinacion para evitar enviar parametros nulos a PostgreSQL. | Implementado |
| **RNF-REN-04** | El chunk JavaScript del frontend debe optimizarse (carga diferida de librerias de graficos) para reducir el peso inicial. | Pendiente |

### 5.4 Usabilidad y accesibilidad

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-USA-01** | La interfaz debe ser **responsive mobile-first**: barra inferior en movil y sidebar en escritorio. | Implementado |
| **RNF-USA-02** | Los botones tactiles deben medir al menos **44 px** de alto. | Implementado |
| **RNF-USA-03** | La fuente base debe ser de **16 px** en movil (17 px en escritorio), con tipografia legible para baja vision (Atkinson Hyperlegible). | Implementado |
| **RNF-USA-04** | Los formularios deben ser de una columna en movil y usar `inputmode=\"decimal\"` en los campos de signos vitales. | Implementado |
| **RNF-USA-05** | Todos los textos visibles de la interfaz deben estar en **espanol**. | Implementado |
| **RNF-USA-06** | Los estados de carga, error y vacio deben usar roles accesibles (`role=\"status\"` / `role=\"alert\"`). | Implementado |

### 5.5 Mantenibilidad y arquitectura

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-MAN-01** | El backend debe ser un **monolito modular** con arquitectura **hexagonal por feature** (dominio sin Spring ni JPA, casos de uso POJO, puertos y adaptadores). | Implementado |
| **RNF-MAN-02** | Cada modulo debe respetar la regla de dependencias: el dominio no depende de la infraestructura. | Implementado |
| **RNF-MAN-03** | El frontend debe organizarse por `features` y mantener un componente por archivo (maximo ~150 lineas). | Implementado |
| **RNF-MAN-04** | Los errores de la API deben seguir un formato unico `ApiError` (`timestamp`, `status`, `codigo`, `mensaje`, `detalles?`). | Implementado |
| **RNF-MAN-05** | El contrato de la API debe documentarse con OpenAPI 3.0.3 y Swagger UI (solo en desarrollo). | Implementado |
| **RNF-MAN-06** | El codigo no debe contener URLs, tokens ni credenciales escritas a mano; todo por variables de entorno. | Implementado |
| **RNF-MAN-07** | El esquema de base de datos debe gobernnarse con migraciones **Flyway**; JPA solo valida (`ddl-auto: validate`). | Implementado |

### 5.6 Portabilidad y despliegue

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-POR-01** | El backend (Java 21 + Spring Boot 3.4.1) debe desplegarse en **Azure App Service**. | Pendiente |
| **RNF-POR-02** | El frontend debe desplegarse como sitio estatico en **Azure Static Web Apps** con regla de reescritura para el ruteo de la SPA. | Pendiente |
| **RNF-POR-03** | La base de datos debe ser **PostgreSQL 16** y poder levantarse con `docker compose` en desarrollo. | Implementado |
| **RNF-POR-04** | El proyecto debe tener integracion continua (lint, build y pruebas) para backend y frontend. | Pendiente |

### 5.7 Calidad y pruebas

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-CAL-01** | El backend debe tener pruebas unitarias, de slice web (MockMvc) y de integracion de persistencia (Testcontainers con PostgreSQL real). | Implementado |
| **RNF-CAL-02** | El frontend debe tener pruebas con Vitest y Testing Library sobre el cliente HTTP, los servicios, el contexto de autenticacion y los componentes clave. | Implementado |
| **RNF-CAL-03** | `mvn clean verify` (backend) y `npm run lint`, `npm run build` y `npm run test` (frontend) deben terminar sin errores. | Implementado |
| **RNF-CAL-04** | Las pruebas de integracion del backend requieren Docker (Testcontainers). | Implementado |

### 5.8 Operacion y observabilidad

| ID | Requisito | Estado |
| --- | --- | --- |
| **RNF-OPE-01** | El backend debe exponer un endpoint de salud publico `GET /actuator/health`. | Implementado |
| **RNF-OPE-02** | Swagger UI y `/v3/api-docs/**` deben estar habilitados solo en el perfil `dev`. | Implementado |
| **RNF-OPE-03** | La configuracion debe separarse por perfiles (`dev`, `prod`) sin secretos versionados. | Implementado |
---

## 6. Reglas de negocio

Reglas transversales que el backend aplica y que **no** se delegan al cliente.

### 6.1 Alcance por rol (visibilidad)

| Rol | Residentes que ve |
| --- | --- |
| **ADMINISTRADOR** | Todos, activos e inactivos. |
| **CUIDADOR_ENFERMERO** | Solo residentes con **asignacion vigente** (`hasta IS NULL`). Nunca ve inactivos. |
| **FAMILIAR_AUTORIZADO** | Solo residentes con **vinculacion autorizada** (`autorizado = true`). Nunca ve inactivos. |

- El filtrado se hace en la consulta (paginacion y conteo coherentes), no
  ocultando campos despues de traerlos.
- Un residente **inexistente o fuera de alcance responde 404 (nunca 403)** y se
  audita como `READ` DENEGADO.
- Un usuario autenticado sin rol conocido **no obtiene alcance** (no se otorga
  acceso por defecto).

### 6.2 Reglas de estado

- Un residente **inactivo** responde **409** en cualquier operacion de escritura
  sobre el (nuevos esquemas, tomas, citas, signos).
- No hay **borrado fisico** de usuarios, residentes, medicamentos ni esquemas;
  el estado se controla con el campo `activo`.
- Las **tomas** y los **signos vitales** son **inmutables** (append-only): un
  error se corrige con un nuevo registro u observacion, nunca editando.
- Los estados **PENDIENTE** y **ATRASADA** de una toma **no se persisten**: se
  derivan de los horarios y de las tomas registradas.

### 6.3 Reglas de integridad

- **Documento** y **correo** son unicos; el correo se compara sin distinguir
  mayusculas.
- **Nombres de medicamento** unicos sin distinguir mayusculas.
- **Un solo contacto principal** por residente.
- **Una sola asignacion vigente** por par cuidador-residente.
- **Una sola vinculacion** vigente por par familiar-residente.
- **Sin vencimientos solapados** de esquemas del mismo medicamento para un
  residente.
- **Frecuencia** de medicacion entre 1 y 24 horas; **al menos un horario** por
  esquema.

### 6.4 Reglas temporales

- La **fecha de nacimiento** no puede ser futura.
- La **cita** no puede agendarse ni editarse en el pasado.
- La fecha de una **toma** o de un **signo vital** no puede ser futura.
- `CUMPLIDA` o `INCUMPLIDA` solo sobre citas cuya fecha/hora ya llego.
- Las reglas basadas en fecha usan un `Clock` inyectado con zona horaria
  `America/Bogota`, para que sean deterministas y coherentes con el centro.

### 6.5 Reglas de seguridad de la cuenta

- Un administrador **no puede desactivarse a si mismo**.
- **No se puede dejar el sistema sin administradores activos**.
- Un administrador **no puede quitarse su propio rol**.
- El acceso a la gestion de usuarios exige `ADMINISTRADOR`.

---

## 7. Indicadores (KPIs) y modelo de datos

### 7.1 KPIs previstos para el dashboard (Proceso 3)

| KPI | Como se calcula |
| --- | --- |
| **Citas cumplidas (%)** | Citas `CUMPLIDA` sobre el total del periodo. |
| **Bitacoras registradas (%)** | Residentes con al menos un registro hoy sobre residentes activos. |
| **Tiempo de alerta (promedio)** | `revisada_en - creado_en` de las alertas. |
| **Reportes generados** | Conteo de reportes en el periodo. |
| **Alertas activas** | Conteo de alertas en estado `ACTIVA`, por severidad. |
| **Cumplimiento de medicacion (%)** | Tomas `ADMINISTRADA` sobre tomas programadas. |
| **Alertas por dosis atrasada** | Alertas de tipo `DOSIS_ATRASADA`. |
| **Tiempo de registro de perfil** | `paciente.actualizado_en - paciente.creado_en`. |
| **Esquemas configurados sin errores** | Esquemas activos con al menos un horario y frecuencia coherente sobre el total de esquemas activos. |

### 7.2 Enumeraciones del dominio

| Enumeracion | Valores |
| --- | --- |
| **Rol** | `ADMINISTRADOR`, `CUIDADOR_ENFERMERO`, `FAMILIAR_AUTORIZADO`. |
| **Sexo** | `MASCULINO`, `FEMENINO`, `OTRO`, `NO_ESPECIFICA`. |
| **Estado de cita** | `PROGRAMADA`, `CUMPLIDA`, `INCUMPLIDA`, `CANCELADA`, `REPROGRAMADA`. |
| **Estado de toma** (persistido) | `ADMINISTRADA`, `OMITIDA`. |
| **Estado de slot** (derivado) | `PENDIENTE`, `ATRASADA`, `ADMINISTRADA`, `OMITIDA`. |
| **Via de administracion** | `ORAL`, `SUBCUTANEA`, `INTRAMUSCULAR`, `TOPICA`, `OTRA`. |
| **Tipo de alerta** | `DOSIS_ATRASADA`, `BITACORA_INCOMPLETA`, `SIGNO_FUERA_DE_RANGO`, `CITA_PROXIMA`, `OTRA`. |
| **Severidad de alerta** | `ALTA`, `MEDIA`, `INFO`. |
| **Estado de alerta** | `ACTIVA`, `REVISADA`, `DESCARTADA`. |
| **Accion de auditoria** | `CREATE`, `READ`, `UPDATE`, `DELETE`, `LOGIN`, `LOGOUT`. |
| **Resultado de auditoria** | `EXITOSO`, `DENEGADO`. |

### 7.3 Entidades principales del modelo de datos

El esquema consta de **16 tablas** gobernadas por Flyway. El detalle completo
(columna por columna, indices y restricciones) esta en `docs/modelo-datos.md`.

| Entidad | Proposito | Proceso |
| --- | --- | --- |
| `rol` | Catalogo de roles. | Transversal |
| `usuario` | Cuentas que acceden al sistema. | Transversal |
| `paciente` | Perfil del adulto mayor residente. | P1 |
| `contacto_emergencia` | Contactos de emergencia de un residente. | P1 |
| `vinculacion_familiar` | Relacion N:M familiar-residente. | P1 |
| `asignacion_cuidador` | Relacion N:M cuidador-residente. | P1, P2 |
| `catalogo_medicamento` | Catalogo de medicamentos. | P1 |
| `esquema_medicacion` | Receta de un residente. | P1 |
| `horario_medicacion` | Horas del dia de cada toma. | P1 |
| `registro_toma` | Toma administrada u omitida. | P1 |
| `catalogo_especialidad` | Catalogo de especialidades. | P2 |
| `cita` | Agenda de citas medicas. | P2 |
| `registro_signos_vitales` | Bitacora de signos vitales. | P2 |
| `alerta` | Alertas operativas. | P3 |
| `reporte_generado` | Metadatos de reportes generados. | P3 |
| `auditoria_acceso` | Trazabilidad de accesos (append-only). | Transversal |
---

## 8. Matriz de trazabilidad

Correspondencia entre modulos, requisitos funcionales, endpoints principales y
pruebas automatizadas. Las rutas y contratos exactos estan en
`docs/api/openapi.yaml` y `docs/api-guia.md`.

| Modulo | RF | Endpoints principales | Pruebas (resumen) |
| --- | --- | --- | --- |
| **Autenticacion** (`usuario`) | RF-AUTH-01..06 | `POST /api/auth/login`, `GET /api/auth/me`, `PUT /api/auth/contrasena` | `AuthControllerTest`, `AutenticarUsuarioUseCaseTest`, `JwtServiceTest`, `JwtAuthenticationFilterTest`, `UsuarioDetailsServiceTest`, `AuthContext.test.tsx` |
| **Usuarios** (`usuario`) | RF-USR-01..09 | `/api/usuarios` (+ `{id}`, `{id}/estado`) | `ReglasAdministradorTest`, `UsuarioControllerAutorizacionTest`, `usuarioService.test.ts`, `ListadoUsuarios.test.tsx`, `FormularioUsuario.test.tsx` |
| **Residentes** (`paciente`) | RF-PAC-01..10 | `/api/pacientes` (+ `{id}`, `{id}/estado`) | `PacienteUseCaseTest`, `AlcancePacienteTest`, `PacienteControllerTest`, `RepositoriosPacienteTest`, `pacienteService.test.ts`, `ListadoResidentes.test.tsx`, `DetalleResidente.test.tsx` |
| **Contactos** | RF-CON-01..04 | `/api/pacientes/{id}/contactos` | `ContactoPrincipalTest`, `ContactoEmergenciaControllerTest` |
| **Familiares** | RF-FAM-01..05 | `/api/pacientes/{id}/familiares` | `GestionarFamiliarUseCaseTest` |
| **Cuidadores** | RF-CUI-01..05 | `/api/pacientes/{id}/cuidadores` | `GestionarCuidadorUseCaseTest` |
| **Medicamentos** | RF-MED-01..04 | `/api/medicamentos` (+ `{id}`, `{id}/estado`) | `RepositoriosMedicacionTest`, `medicamentoService.test.ts`, `ListadoMedicamentos.test.tsx`, `FormularioMedicamento.test.tsx` |
| **Esquemas** | RF-ESQ-01..07 | `/api/pacientes/{id}/esquemas` (+ detalle, `estado`) | `RepositoriosMedicacionTest`, `esquemaService.test.ts`, `FormularioEsquema.test.tsx` |
| **Tomas** | RF-TOM-01..09 | `/api/pacientes/{id}/tomas/agenda`, `/tomas/historial`, `POST .../tomas` | `RegistrarTomaUseCaseTest`, `RepositoriosMedicacionTest`, `tomaService.test.ts`, `TomasResidente.test.tsx` |
| **Citas** | RF-CIT-01..09 | `GET /api/citas`, `/api/pacientes/{id}/citas` (+ `{citaId}`, `estado`), `/api/especialidades` | `CitasUseCaseTest`, `CitaControllerTest`, `RepositoriosCitaTest`, `citaService.test.ts`, `Calendario.test.tsx`, `FormularioCita.test.tsx` |
| **Bitacora** | RF-BIT-01..08 | `/api/pacientes/{id}/signos-vitales` (+ `/ultimo`) | `BitacoraUseCaseTest`, `BitacoraControllerTest`, `RepositoriosBitacoraTest`, `signosVitales.test.ts`, `FormularioSignosVitales.test.tsx`, `SelectorBitacora.test.tsx` |
| **Alertas** (P3) | RF-ALE-01..03 | Por definir | Pendiente |
| **Dashboard** (P3) | RF-DAS-01..02 | Por definir | `resumenOperativo.test.ts`, `PanelAdministrador.test.tsx` (parcial) |
| **Reportes** (P3) | RF-REP-01..02 | Por definir | Pendiente |
| **Auditoria** | RF-AUD-01..03 | (interna, via `AuditoriaPort`) | Verificada por pruebas de casos de uso y de controladores |

---

## 9. Estado de implementacion

### 9.1 Modulos del backend

| Modulo | Estado | Notas |
| --- | --- | --- |
| `usuario` | Implementado | Login JWT, CRUD de usuarios, cambio de contrasena, administrador inicial. |
| `paciente` | Implementado | Perfiles, contactos, familiares y cuidadores con alcance por rol. |
| `medicacion` | Implementado | Catalogo, esquemas, horarios, agenda e historial de tomas. |
| `cita` | Implementado | Calendario paginado, agenda, edicion y cambio de estado. |
| `bitacora` | Implementado | Registro, historial y ultimo registro de signos vitales. |
| `alerta` | Pendiente | Solo estructura de carpetas. |
| `reporte` | Pendiente | Solo estructura de carpetas. |
| `dashboard` | Pendiente | Solo estructura de carpetas. |

### 9.2 Pantallas del frontend

| Pantalla / ruta | Roles | Estado |
| --- | --- | --- |
| `/login` | Publico | Implementado |
| `/dashboard` | ADMINISTRADOR | Implementado (datos reales disponibles; resto rotulado) |
| `/residentes`, `/residentes/:id` | Todos (por alcance) | Implementado |
| `/residentes/nuevo`, `/residentes/:id/editar` | ADMINISTRADOR | Implementado |
| `/residentes/:id/esquemas/nuevo`, `.../editar` | ADMINISTRADOR | Implementado |
| `/residentes/:id/tomas` | Todos (por alcance) | Implementado |
| `/residentes/:id/signos-vitales` | Todos (por alcance) | Implementado |
| `/medicamentos` (+ nuevo, editar) | Lectura ADMIN/CUIDADOR; escritura ADMIN | Implementado |
| `/calendario` (+ nuevo, editar) | Lectura todos; escritura ADMIN | Implementado |
| `/bitacora` | Todos | Implementado |
| `/usuarios` (+ nuevo, editar) | ADMINISTRADOR | Implementado |
| `/cuenta/contrasena` | Todos | Implementado |
| `/cuidador` | CUIDADOR_ENFERMERO | Implementado |
| `/alertas` | ADMINISTRADOR | En construccion |
| `/reportes` | ADMINISTRADOR | En construccion |

### 9.3 Resumen cuantitativo de pruebas

| Capa | Herramienta | Cantidad |
| --- | --- | --- |
| Backend | JUnit 5 + Mockito + MockMvc + Testcontainers | 17 clases de prueba activas en los modulos implementados |
| Frontend | Vitest + Testing Library | 27 archivos `*.test.ts(x)` |

Las pruebas de integracion del backend usan un PostgreSQL 16 real con
Testcontainers y requieren Docker en ejecucion.

---

## 10. Supuestos y limitaciones

### 10.1 Supuestos

- **S1.** El PDF de definicion del proyecto (`docs/definicion-proyecto.pdf`) no
  es legible en el repositorio; el alcance se deriva de la documentacion de
  contexto y del codigo. Debe confirmarse cuando el PDF este disponible.
- **S2.** El sistema atiende **un solo centro geriatrico**. Multiples centros
  quedan fuera de alcance.
- **S3.** El centro opera en la zona horaria **America/Bogota**; el backend fija
  esa zona en su reloj.
- **S4.** No existe el concepto de especialidad inactiva; el catalogo de
  especialidades no tiene columna de estado.

### 10.2 Limitaciones conocidas

- **L1.** No hay *refresh tokens*: al vencer el JWT, el usuario debe volver a
  iniciar sesion.
- **L2.** `sessionStorage` no es inmune a XSS; es una medida de reduccion de
  exposicion, no una defensa completa.
- **L3.** El panel de administrador consulta la agenda por residente (primera
  pagina, maximo 100) y avisa si hay mas; es una limitacion aceptada hasta que
  exista un endpoint de resumen.
- **L4.** El chunk JavaScript del frontend es grande por la libreria de graficos
  (`recharts`); falta la carga diferida (RNF-REN-04).
- **L5.** Los modulos de Proceso 3 (alertas, dashboard real y reportes PDF) no
  estan implementados: sus pantallas muestran un aviso de seccion en
  construccion.
- **L6.** La infraestructura como codigo (`infra/main.bicep`) y los flujos de
  integracion continua (`.github/workflows`) estan pendientes.

---

## 11. Documentos relacionados

| Documento | Contenido |
| --- | --- |
| `docs/modelo-datos.md` | Modelo relacional completo, decision D1-D8 y trazabilidad de KPIs. |
| `docs/seguridad.md` | Autenticacion JWT, roles, CORS, auditoria y alcance por modulo. |
| `docs/api/openapi.yaml` | Contrato OpenAPI 3.0.3 de la API REST. |
| `docs/api-guia.md` | Guia tecnica de la API (convenciones, errores, endpoints). |
| `docs/diagramas/` | Diagramas C4 (contexto, contenedores, componentes) y workspace. |

---

> Documento de Especificacion de Requisitos de MediTrack Senior. Los estados
> reflejan el repositorio al momento de su redaccion y deben actualizarse al
> cerrar cada iteracion.