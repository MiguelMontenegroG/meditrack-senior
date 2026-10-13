package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import java.util.List;

/**
 * Pagina del historial de tomas de un residente.
 */
public record PaginaTomasResponse(
        List<RegistroTomaResponse> contenido,
        int pagina,
        int tamano,
        long total,
        int totalPaginas) {
}
