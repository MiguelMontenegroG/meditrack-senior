package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

/**
 * Contacto de emergencia expuesto en la API.
 */
public record ContactoEmergenciaResponse(
        Long id,
        String nombre,
        String parentesco,
        String telefono,
        boolean principal) {
}
