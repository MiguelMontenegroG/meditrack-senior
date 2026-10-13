package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de entrada para activar o desactivar un medicamento o un esquema.
 */
public record CambiarEstadoMedicacionRequest(
        @NotNull(message = "El estado es obligatorio")
        Boolean activo) {
}
