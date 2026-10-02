-- V1__baseline.sql
-- Migracion base de MediTrack Senior.
-- Deliberadamente NO crea tablas: el esquema real es el Paso B2.
-- Su unica funcion es marcar el inicio del historial de Flyway sobre la base
-- de datos (asegura que el esquema este vacio y versionado desde el dia uno).

-- Comentario de la base de datos para dejar trazabilidad del baseline.
COMMENT ON DATABASE postgres IS 'MediTrack Senior - baseline Flyway V1 (sin tablas; esquema en Paso B2)';
