package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;

/**
 * Datos de entrada para crear un usuario.
 *
 * @param correo         correo del usuario
 * @param nombreCompleto nombre completo
 * @param telefono       telefono (opcional)
 * @param contrasena     contrasena en texto plano (se cifra antes de persistir)
 * @param rol            rol a asignar
 */
public record ComandoCrearUsuario(
        String correo,
        String nombreCompleto,
        String telefono,
        String contrasena,
        RolNombre rol) {

    public Correo correoNormalizado() {
        return Correo.de(correo);
    }
}
