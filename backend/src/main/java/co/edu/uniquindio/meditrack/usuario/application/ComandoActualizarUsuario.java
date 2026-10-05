package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;

/**
 * Datos de entrada para actualizar los campos editables de un usuario.
 *
 * @param nombreCompleto nuevo nombre completo
 * @param telefono       nuevo telefono (opcional)
 * @param rol            nuevo rol
 */
public record ComandoActualizarUsuario(
        String nombreCompleto,
        String telefono,
        RolNombre rol) {
}
