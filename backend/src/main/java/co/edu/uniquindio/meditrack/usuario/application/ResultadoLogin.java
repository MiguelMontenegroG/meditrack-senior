package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Resultado del caso de uso de inicio de sesion: el token emitido, su vigencia
 * y el usuario autenticado.
 *
 * @param token               token JWT firmado
 * @param expiraEnSegundos    segundos de vigencia del token
 * @param usuario             usuario autenticado
 */
public record ResultadoLogin(String token, long expiraEnSegundos, Usuario usuario) {
}
