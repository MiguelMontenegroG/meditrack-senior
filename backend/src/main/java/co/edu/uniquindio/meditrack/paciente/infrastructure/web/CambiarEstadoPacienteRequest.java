package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de la peticion para activar o desactivar un paciente.
 */
public record CambiarEstadoPacienteRequest(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo) {
}
