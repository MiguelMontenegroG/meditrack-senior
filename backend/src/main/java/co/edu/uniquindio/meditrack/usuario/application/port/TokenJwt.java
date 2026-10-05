package co.edu.uniquindio.meditrack.usuario.application.port;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;

/**
 * Datos extraidos de un token JWT valido.
 *
 * @param usuarioId id del usuario (claim sub)
 * @param rol       rol del usuario (claim rol)
 */
public record TokenJwt(Long usuarioId, RolNombre rol) {
}
