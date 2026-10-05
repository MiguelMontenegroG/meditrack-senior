package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de la peticion para activar o desactivar un usuario (solo administrador).
 */
public record CambiarEstadoRequest(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo) {
}
