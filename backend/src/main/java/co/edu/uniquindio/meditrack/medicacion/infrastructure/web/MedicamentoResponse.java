package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

/**
 * Vista de un medicamento del catalogo.
 */
public record MedicamentoResponse(
        Long id,
        String nombre,
        String presentacion,
        boolean activo) {
}
