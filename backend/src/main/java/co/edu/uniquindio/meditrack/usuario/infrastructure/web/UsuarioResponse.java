package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import java.time.Instant;

/**
 * Datos publicos de un usuario devueltos por los endpoints de administracion.
 * Nunca exponen el hash de la contrasena.
 */
public record UsuarioResponse(
        Long id,
        String nombreCompleto,
        String correo,
        String telefono,
        String rol,
        boolean activo,
        Instant ultimoAccesoEn,
        Instant creadoEn,
        Instant actualizadoEn) {
}
