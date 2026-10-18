package co.edu.uniquindio.meditrack.cita.domain;

/**
 * Rol del usuario que opera el modulo de citas. Es una copia minima y sin
 * dependencias de framework, para que el dominio de cita no dependa del modulo
 * usuario ni de Spring Security.
 *
 * <p>Los valores coinciden con {@code RolNombre} del modulo usuario. Se
 * declara propio (igual que hace medicacion) para no acoplar el dominio de cita
 * a otro modulo; el mapeo desde la autenticacion de Spring se hace en la capa
 * web.</p>
 */
public enum RolCita {
    ADMINISTRADOR,
    CUIDADOR_ENFERMERO,
    FAMILIAR_AUTORIZADO
}
