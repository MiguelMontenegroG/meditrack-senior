package co.edu.uniquindio.meditrack.shared.application.port;

/**
 * Puerto de auditoria. Escribe filas de solo insercion en auditoria_acceso.
 * Nunca debe recibir contrasenas ni tokens en el campo detalle.
 */
public interface AuditoriaPort {

    /**
     * Registra un evento de auditoria.
     *
     * @param usuarioId id del usuario afectado o que ejecuta la accion (puede ser null en un login por correo inexistente)
     * @param accion    tipo de accion
     * @param entidad   entidad afectada (por ejemplo "usuario")
     * @param entidadId id de la entidad afectada (puede ser null)
     * @param resultado resultado de la operacion
     * @param detalle   detalle no sensible (nunca contrasenas ni tokens)
     */
    void registrar(
            Long usuarioId,
            AccionAuditoria accion,
            String entidad,
            Long entidadId,
            ResultadoAuditoria resultado,
            String detalle);
}
