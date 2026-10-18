package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de entrada para cambiar el estado de una cita.
 */
public record CambiarEstadoCitaRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoCita estado) {
}
