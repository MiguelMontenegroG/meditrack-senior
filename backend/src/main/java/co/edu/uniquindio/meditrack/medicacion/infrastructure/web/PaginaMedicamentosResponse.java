package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import java.util.List;

/**
 * Pagina de medicamentos del catalogo.
 */
public record PaginaMedicamentosResponse(
        List<MedicamentoResponse> contenido,
        int pagina,
        int tamano,
        long total,
        int totalPaginas) {
}
