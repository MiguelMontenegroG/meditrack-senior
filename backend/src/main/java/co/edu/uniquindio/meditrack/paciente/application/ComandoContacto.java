package co.edu.uniquindio.meditrack.paciente.application;

/**
 * Datos de entrada para crear o actualizar un contacto de emergencia.
 */
public record ComandoContacto(
        String nombre,
        String parentesco,
        String telefono,
        boolean principal) {
}
