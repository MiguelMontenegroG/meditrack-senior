-- V3__datos_semilla.sql
-- Datos semilla de catalogos. NO siembra usuarios: el administrador inicial
-- lo creara un ApplicationRunner idempotente en el paso de seguridad
-- (basado en la variable de entorno ADMIN_INITIAL_PASSWORD).

-- Roles del sistema.
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMINISTRADOR',       'Acceso completo; gestiona usuarios y operacion del centro'),
    ('CUIDADOR_ENFERMERO',  'Registra bitacoras, administra medicacion y atiende tareas del dia'),
    ('FAMILIAR_AUTORIZADO', 'Solo lectura; consulta la informacion de sus familiares');

-- Especialidades medicas basicas.
INSERT INTO catalogo_especialidad (nombre) VALUES
    ('Medicina general'),
    ('Cardiologia'),
    ('Geriatria'),
    ('Neurologia'),
    ('Endocrinologia'),
    ('Oftalmologia'),
    ('Traumatologia'),
    ('Nutricion');