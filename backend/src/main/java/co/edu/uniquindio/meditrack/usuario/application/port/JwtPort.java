package co.edu.uniquindio.meditrack.usuario.application.port;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;

/**
 * Puerto de generacion y validacion de tokens JWT.
 */
public interface JwtPort {

    /**
     * Genera un token firmado con: sub (id de usuario), rol, iat y exp.
     * No incluye correo ni otros datos sensibles.
     */
    String generarToken(Long usuarioId, RolNombre rol);

    /**
     * Valida la firma y vigencia del token y devuelve sus datos.
     * Lanza una excepcion si el token es invalido, expirado o manipulado.
     */
    TokenJwt validarToken(String token);

    /** Segundos de vigencia configurados para los tokens. */
    long expiracionEnSegundos();
}
