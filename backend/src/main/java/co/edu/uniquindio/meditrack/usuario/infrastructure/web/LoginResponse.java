package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

/**
 * Respuesta del inicio de sesion.
 *
 * @param token              token JWT
 * @param tipo               esquema de autenticacion (siempre "Bearer")
 * @param expiraEnSegundos   vigencia del token en segundos
 * @param usuario            datos publicos del usuario
 */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioAutenticadoResponse usuario) {
}
