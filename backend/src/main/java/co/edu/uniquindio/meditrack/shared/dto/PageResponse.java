package co.edu.uniquindio.meditrack.shared.dto;

import java.util.List;

/**
 * Envoltura de respuesta paginada.
 *
 * @param contenido elementos de la pagina actual
 * @param pagina    numero de pagina (base 0)
 * @param tamano    tamano de pagina
 * @param total     total de elementos
 * @param totalPaginas numero total de paginas
 */
public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long total,
        int totalPaginas) {

    public static <T> PageResponse<T> de(List<T> contenido, int pagina, int tamano, long total) {
        int totalPaginas = tamano > 0 ? (int) Math.ceil((double) total / tamano) : 0;
        return new PageResponse<>(contenido, pagina, tamano, total, totalPaginas);
    }
}
