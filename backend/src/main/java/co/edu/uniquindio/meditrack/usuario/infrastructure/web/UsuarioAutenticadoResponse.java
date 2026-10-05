package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

/**
 * Datos publicos del usuario autenticado incluidos en la respuesta de login.
 * Nunca exponen el hash de la contrasena.
 *
 * @param id             identificador del usuario
 * @param nombreCompleto nombre completo
 * @param correo         correo
 * @param rol            nombre del rol (ADMINISTRADOR, CUIDADOR_ENFERMERO, FAMILIAR_AUTORIZADO)
 */
public record UsuarioAutenticadoResponse(
        Long id,
        String nombreCompleto,
        String correo,
        String rol) {
}
