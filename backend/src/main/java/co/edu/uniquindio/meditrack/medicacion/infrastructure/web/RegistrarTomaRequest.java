package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Cuerpo de entrada para registrar una toma. El autor no viene en el cuerpo:
 * sale del usuario autenticado.
 */
public record RegistrarTomaRequest(
        @NotNull(message = "La fecha y hora programada es obligatoria")
        Instant fechaHoraProgramada,

        @NotNull(message = "El estado es obligatorio")
        EstadoToma estado,

        String observaciones) {
}
