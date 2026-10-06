package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

/**
 * Vinculacion familiar expuesta en la API.
 */
public record VinculacionFamiliarResponse(
        Long id,
        Long usuarioId,
        Long pacienteId,
        String parentesco,
        boolean autorizado) {
}
