package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma;

import java.time.Instant;

/**
 * Datos de entrada para registrar una toma. {@code registradoPor} NO viene del
 * cuerpo: sale del usuario autenticado en la capa web.
 */
public record ComandoRegistrarToma(
        Instant fechaHoraProgramada,
        EstadoToma estado,
        String observaciones) {
}
