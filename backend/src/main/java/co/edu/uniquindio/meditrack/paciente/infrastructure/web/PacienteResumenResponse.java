package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

/**
 * Resumen de paciente para el listado: no incluye la situacion clinica.
 */
public record PacienteResumenResponse(
        Long id,
        String nombreCompleto,
        int edad,
        String habitacion,
        boolean activo) {
}
