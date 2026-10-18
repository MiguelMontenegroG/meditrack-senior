package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;

import java.time.Instant;

/**
 * Vista de una cita. Incluye el nombre de la especialidad (calculado) cuando la
 * cita la tiene; nunca expone datos clinicos del residente.
 */
public record CitaResponse(
        Long id,
        Long pacienteId,
        Long especialidadId,
        String especialidadNombre,
        Long registradaPor,
        String profesional,
        Instant fechaHora,
        String lugar,
        String motivo,
        EstadoCita estado) {
}
