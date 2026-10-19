package co.edu.uniquindio.meditrack.bitacora.domain;

/**
 * Rol del usuario que opera el modulo de bitacora de signos vitales. Es una copia
 * minima y sin dependencias de framework, para que el dominio de bitacora no
 * dependa del modulo usuario ni de Spring Security.
 *
 * <p>Los valores coinciden con {@code RolNombre} del modulo usuario. Se declara
 * propio (igual que hacen citas y medicacion) para no acoplar el dominio de
 * bitacora a otro modulo; el mapeo desde la autenticacion de Spring se hace en
 * la capa web.</p>
 */
public enum RolBitacora {
    ADMINISTRADOR,
    CUIDADOR_ENFERMERO,
    FAMILIAR_AUTORIZADO
}
