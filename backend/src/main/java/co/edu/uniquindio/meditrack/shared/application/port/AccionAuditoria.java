package co.edu.uniquindio.meditrack.shared.application.port;

/**
 * Tipos de accion registrables en auditoria_acceso (coinciden con el CHECK de V2).
 */
public enum AccionAuditoria {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT
}
