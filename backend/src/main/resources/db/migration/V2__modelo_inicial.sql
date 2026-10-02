-- V2__modelo_inicial.sql
-- Modelo de datos inicial de MediTrack Senior (PostgreSQL).
-- Crea todas las tablas del dominio segun docs/modelo-datos.md.
-- Sin codigos de tarea; comentarios en espanol.

-- ---------------------------------------------------------------------------
-- Proceso 1: perfiles y esquemas de medicacion
-- ---------------------------------------------------------------------------

-- Catalogo de roles del sistema.
CREATE TABLE rol (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    CONSTRAINT chk_rol_nombre CHECK (nombre IN ('ADMINISTRADOR','CUIDADOR_ENFERMERO','FAMILIAR_AUTORIZADO'))
);

-- Cuentas que acceden al sistema (personal del centro y familiares).
CREATE TABLE usuario (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rol_id          BIGINT NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    correo          VARCHAR(150) NOT NULL,
    hash_contrasena VARCHAR(255) NOT NULL,
    telefono        VARCHAR(30),
    activo          BOOLEAN NOT NULL DEFAULT true,
    ultimo_acceso_en TIMESTAMPTZ,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id) ON DELETE RESTRICT
);
-- Correo unico sin distinguir mayusculas.
CREATE UNIQUE INDEX uq_usuario_correo_lower ON usuario (lower(correo));

-- Perfil del adulto mayor residente.
CREATE TABLE paciente (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    documento         VARCHAR(30) NOT NULL UNIQUE,
    nombres           VARCHAR(100) NOT NULL,
    apellidos         VARCHAR(100) NOT NULL,
    fecha_nacimiento  DATE NOT NULL,
    sexo              VARCHAR(20) NOT NULL,
    habitacion        VARCHAR(20),
    situacion_clinica TEXT,
    fecha_ingreso     DATE NOT NULL DEFAULT current_date,
    activo            BOOLEAN NOT NULL DEFAULT true,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_paciente_sexo CHECK (sexo IN ('MASCULINO','FEMENINO','OTRO','NO_ESPECIFICA'))
);
CREATE INDEX ix_paciente_nombre ON paciente (apellidos, nombres);

-- Contactos de emergencia de un paciente.
CREATE TABLE contacto_emergencia (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    parentesco  VARCHAR(60),
    telefono    VARCHAR(30) NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT false,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_contacto_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE
);
CREATE INDEX ix_contacto_paciente ON contacto_emergencia (paciente_id);
-- Un solo contacto principal por paciente.
CREATE UNIQUE INDEX uq_contacto_principal ON contacto_emergencia (paciente_id) WHERE es_principal;

-- Vinculacion N:M usuario (familiar) - paciente.
CREATE TABLE vinculacion_familiar (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    parentesco  VARCHAR(60),
    autorizado  BOOLEAN NOT NULL DEFAULT true,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_vincfam_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario(id)  ON DELETE CASCADE,
    CONSTRAINT fk_vincfam_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_vincfam_usuario_paciente ON vinculacion_familiar (usuario_id, paciente_id);
CREATE INDEX ix_vincfam_paciente ON vinculacion_familiar (paciente_id);

-- Asignacion N:M cuidador - paciente.
CREATE TABLE asignacion_cuidador (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    desde       DATE NOT NULL DEFAULT current_date,
    hasta       DATE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_asicuid_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario(id)  ON DELETE CASCADE,
    CONSTRAINT fk_asicuid_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id) ON DELETE CASCADE,
    CONSTRAINT chk_asicuid_rango CHECK (hasta IS NULL OR hasta >= desde)
);
-- Solo una asignacion vigente por par usuario/paciente.
CREATE UNIQUE INDEX uq_asicuid_vigente ON asignacion_cuidador (usuario_id, paciente_id) WHERE hasta IS NULL;
CREATE INDEX ix_asicuid_paciente ON asignacion_cuidador (paciente_id);
-- ---------------------------------------------------------------------------
-- Medicacion (Proceso 1)
-- ---------------------------------------------------------------------------

-- Catalogo de medicamentos.
CREATE TABLE catalogo_medicamento (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL UNIQUE,
    presentacion VARCHAR(100),
    activo       BOOLEAN NOT NULL DEFAULT true
);

-- Tratamiento de un paciente con medicamento, dosis, frecuencia y vigencia.
CREATE TABLE esquema_medicacion (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id      BIGINT NOT NULL,
    medicamento_id   BIGINT NOT NULL,
    dosis            VARCHAR(50) NOT NULL,
    frecuencia_horas SMALLINT NOT NULL,
    via              VARCHAR(30) NOT NULL,
    indicaciones     TEXT,
    fecha_inicio     DATE NOT NULL,
    fecha_fin        DATE,
    activo           BOOLEAN NOT NULL DEFAULT true,
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_esquema_paciente    FOREIGN KEY (paciente_id)    REFERENCES paciente(id) ON DELETE RESTRICT,
    CONSTRAINT fk_esquema_medicamento FOREIGN KEY (medicamento_id) REFERENCES catalogo_medicamento(id) ON DELETE RESTRICT,
    CONSTRAINT chk_esquema_frecuencia CHECK (frecuencia_horas BETWEEN 1 AND 24),
    CONSTRAINT chk_esquema_via        CHECK (via IN ('ORAL','SUBCUTANEA','INTRAMUSCULAR','TOPICA','OTRA')),
    CONSTRAINT chk_esquema_rango      CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);
CREATE INDEX ix_esquema_paciente    ON esquema_medicacion (paciente_id);
CREATE INDEX ix_esquema_medicamento ON esquema_medicacion (medicamento_id);
CREATE INDEX ix_esquema_activo      ON esquema_medicacion (paciente_id) WHERE activo;

-- Horas exactas del dia en que corresponde una toma.
CREATE TABLE horario_medicacion (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    esquema_id BIGINT NOT NULL,
    hora       TIME NOT NULL,
    CONSTRAINT fk_horario_esquema FOREIGN KEY (esquema_id) REFERENCES esquema_medicacion(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_horario_esquema_hora ON horario_medicacion (esquema_id, hora);

-- Registro de cada toma efectivamente administrada u omitida.
-- Lo pendiente/atrasado se deriva de horario_medicacion.
CREATE TABLE registro_toma (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    esquema_id             BIGINT NOT NULL,
    registrado_por         BIGINT NOT NULL,
    fecha_hora_programada  TIMESTAMPTZ NOT NULL,
    fecha_hora_registro    TIMESTAMPTZ,
    estado                 VARCHAR(20) NOT NULL,
    observaciones          TEXT,
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_toma_esquema    FOREIGN KEY (esquema_id)     REFERENCES esquema_medicacion(id) ON DELETE RESTRICT,
    CONSTRAINT fk_toma_usuario    FOREIGN KEY (registrado_por) REFERENCES usuario(id) ON DELETE RESTRICT,
    CONSTRAINT chk_toma_estado    CHECK (estado IN ('ADMINISTRADA','OMITIDA'))
);
CREATE INDEX ix_toma_esquema ON registro_toma (esquema_id);
CREATE UNIQUE INDEX uq_toma_esquema_programada ON registro_toma (esquema_id, fecha_hora_programada);
CREATE INDEX ix_toma_programada ON registro_toma (fecha_hora_programada);

-- ---------------------------------------------------------------------------
-- Citas (Proceso 2)
-- ---------------------------------------------------------------------------

-- Catalogo de especialidades medicas.
CREATE TABLE catalogo_especialidad (
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- Agenda de citas medicas de un paciente.
CREATE TABLE cita (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id     BIGINT NOT NULL,
    especialidad_id BIGINT,
    registrada_por  BIGINT NOT NULL,
    profesional     VARCHAR(150),
    fecha_hora      TIMESTAMPTZ NOT NULL,
    lugar           VARCHAR(150),
    motivo          TEXT,
    estado          VARCHAR(20) NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_cita_paciente     FOREIGN KEY (paciente_id)     REFERENCES paciente(id) ON DELETE RESTRICT,
    CONSTRAINT fk_cita_especialidad FOREIGN KEY (especialidad_id) REFERENCES catalogo_especialidad(id) ON DELETE RESTRICT,
    CONSTRAINT fk_cita_usuario      FOREIGN KEY (registrada_por)  REFERENCES usuario(id) ON DELETE RESTRICT,
    CONSTRAINT chk_cita_estado CHECK (estado IN ('PROGRAMADA','CUMPLIDA','INCUMPLIDA','CANCELADA','REPROGRAMADA'))
);
CREATE INDEX ix_cita_paciente ON cita (paciente_id);
CREATE INDEX ix_cita_fecha    ON cita (fecha_hora);
CREATE INDEX ix_cita_estado   ON cita (estado);
-- ---------------------------------------------------------------------------
-- Bitacora de signos vitales (Proceso 2)
-- ---------------------------------------------------------------------------

-- Bitacora de signos vitales por paciente.
CREATE TABLE registro_signos_vitales (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id        BIGINT NOT NULL,
    registrado_por     BIGINT NOT NULL,
    fecha_hora         TIMESTAMPTZ NOT NULL DEFAULT now(),
    presion_sistolica  SMALLINT,
    presion_diastolica SMALLINT,
    glucosa_mg_dl      SMALLINT,
    temperatura_c      NUMERIC(4,1),
    observaciones      TEXT,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_signos_paciente FOREIGN KEY (paciente_id)    REFERENCES paciente(id) ON DELETE RESTRICT,
    CONSTRAINT fk_signos_usuario  FOREIGN KEY (registrado_por) REFERENCES usuario(id) ON DELETE RESTRICT,
    CONSTRAINT chk_signos_sistolica  CHECK (presion_sistolica  BETWEEN 50 AND 300),
    CONSTRAINT chk_signos_diastolica CHECK (presion_diastolica BETWEEN 30 AND 200),
    CONSTRAINT chk_signos_glucosa    CHECK (glucosa_mg_dl      BETWEEN 20 AND 800),
    CONSTRAINT chk_signos_temp       CHECK (temperatura_c      BETWEEN 30.0 AND 45.0),
    CONSTRAINT chk_signos_presion_coherente CHECK (
        presion_sistolica IS NULL OR presion_diastolica IS NULL
        OR presion_diastolica < presion_sistolica
    )
);
CREATE INDEX ix_signos_paciente_fecha ON registro_signos_vitales (paciente_id, fecha_hora);

-- ---------------------------------------------------------------------------
-- Reportes y alertas (Proceso 3)
-- ---------------------------------------------------------------------------

-- Metadatos de los reportes clinicos generados (no almacena el archivo).
CREATE TABLE reporte_generado (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id         BIGINT NOT NULL,
    generado_por        BIGINT NOT NULL,
    tipo                VARCHAR(40) NOT NULL,
    rango_desde         TIMESTAMPTZ,
    rango_hasta         TIMESTAMPTZ,
    ruta_almacenamiento VARCHAR(300),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_reporte_paciente FOREIGN KEY (paciente_id)  REFERENCES paciente(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reporte_usuario  FOREIGN KEY (generado_por) REFERENCES usuario(id)  ON DELETE RESTRICT,
    CONSTRAINT chk_reporte_tipo CHECK (tipo IN ('HISTORIA_CLINICA','BITACORA','MEDICACION','GENERAL'))
);
CREATE INDEX ix_reporte_paciente ON reporte_generado (paciente_id);
CREATE INDEX ix_reporte_creado   ON reporte_generado (creado_en);

-- Alertas operativas generadas por el sistema o el personal.
CREATE TABLE alerta (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id  BIGINT NOT NULL,
    tipo         VARCHAR(40) NOT NULL,
    severidad    VARCHAR(10) NOT NULL,
    mensaje      TEXT NOT NULL,
    origen       VARCHAR(20) NOT NULL,
    signos_id    BIGINT,
    toma_id      BIGINT,
    cita_id      BIGINT,
    estado       VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT now(),
    revisada_en  TIMESTAMPTZ,
    revisada_por BIGINT,
    CONSTRAINT fk_alerta_paciente FOREIGN KEY (paciente_id)  REFERENCES paciente(id) ON DELETE RESTRICT,
    CONSTRAINT fk_alerta_signos   FOREIGN KEY (signos_id)    REFERENCES registro_signos_vitales(id) ON DELETE RESTRICT,
    CONSTRAINT fk_alerta_toma     FOREIGN KEY (toma_id)      REFERENCES registro_toma(id) ON DELETE RESTRICT,
    CONSTRAINT fk_alerta_cita     FOREIGN KEY (cita_id)      REFERENCES cita(id) ON DELETE RESTRICT,
    CONSTRAINT fk_alerta_revisor  FOREIGN KEY (revisada_por) REFERENCES usuario(id) ON DELETE RESTRICT,
    CONSTRAINT chk_alerta_tipo      CHECK (tipo IN ('DOSIS_ATRASADA','BITACORA_INCOMPLETA','SIGNO_FUERA_DE_RANGO','CITA_PROXIMA','OTRA')),
    CONSTRAINT chk_alerta_severidad CHECK (severidad IN ('ALTA','MEDIA','INFO')),
    CONSTRAINT chk_alerta_origen    CHECK (origen IN ('SISTEMA','USUARIO')),
    CONSTRAINT chk_alerta_estado    CHECK (estado IN ('ACTIVA','REVISADA','DESCARTADA')),
    -- A lo sumo una de las tres fuentes informada.
    CONSTRAINT chk_alerta_fuente_unica CHECK (
        (CASE WHEN toma_id   IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN cita_id   IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN signos_id IS NOT NULL THEN 1 ELSE 0 END) <= 1
    )
);
CREATE INDEX ix_alerta_paciente_estado ON alerta (paciente_id, estado);
CREATE INDEX ix_alerta_severidad       ON alerta (severidad);
CREATE INDEX ix_alerta_creado          ON alerta (creado_en);
-- Idempotencia del scheduler: una alerta por fuente.
CREATE UNIQUE INDEX uq_alerta_toma   ON alerta (toma_id)   WHERE toma_id   IS NOT NULL;
CREATE UNIQUE INDEX uq_alerta_cita   ON alerta (cita_id)   WHERE cita_id   IS NOT NULL;
CREATE UNIQUE INDEX uq_alerta_signos ON alerta (signos_id) WHERE signos_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- Auditoria (Transversal, Ley 1581)
-- ---------------------------------------------------------------------------

-- Trazabilidad de accesos y operaciones sensibles. Solo insercion (append-only).
CREATE TABLE auditoria_acceso (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT,
    accion      VARCHAR(20) NOT NULL,
    entidad     VARCHAR(60) NOT NULL,
    entidad_id  BIGINT,
    fecha_hora  TIMESTAMPTZ NOT NULL DEFAULT now(),
    resultado   VARCHAR(20) NOT NULL,
    detalle     TEXT,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    CONSTRAINT chk_auditoria_accion    CHECK (accion IN ('CREATE','READ','UPDATE','DELETE','LOGIN','LOGOUT')),
    CONSTRAINT chk_auditoria_resultado CHECK (resultado IN ('EXITOSO','DENEGADO'))
);
CREATE INDEX ix_auditoria_usuario_fecha ON auditoria_acceso (usuario_id, fecha_hora);
CREATE INDEX ix_auditoria_entidad       ON auditoria_acceso (entidad, entidad_id);