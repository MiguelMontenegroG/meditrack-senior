package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma;

import java.time.Instant;

/**
 * Vista de una toma registrada.
 */
public record RegistroTomaResponse(
        Long id,
        Long esquemaId,
        Long registradoPor,
        Instant fechaHoraProgramada,
        Instant fechaHoraRegistro,
        EstadoToma estado,
        String observaciones) {
}
