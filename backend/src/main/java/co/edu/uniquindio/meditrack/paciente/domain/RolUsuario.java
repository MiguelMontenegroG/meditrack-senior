package co.edu.uniquindio.meditrack.paciente.domain;

/**
 * Rol del usuario que consulta el modulo de paciente. Es una copia minima y
 * sin dependencias de framework del rol de seguridad, para que el dominio de
 * paciente no dependa del modulo usuario ni de Spring Security.
 *
 * <p>Los valores coinciden con {@code RolNombre} del modulo usuario.</p>
 */
public enum RolUsuario {
    ADMINISTRADOR,
    CUIDADOR_ENFERMERO,
    FAMILIAR_AUTORIZADO
}
