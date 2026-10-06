package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import java.time.LocalDate;

/**
 * Asignacion de cuidador expuesta en la API.
 */
public record AsignacionCuidadorResponse(
        Long id,
        Long usuarioId,
        Long pacienteId,
        LocalDate desde,
        LocalDate hasta) {
}
