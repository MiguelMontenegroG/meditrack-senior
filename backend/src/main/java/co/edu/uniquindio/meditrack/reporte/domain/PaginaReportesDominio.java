package co.edu.uniquindio.meditrack.reporte.domain;

import java.util.List;

/**
 * Pagina del historial de reportes generados: contenido y total calculados con
 * el mismo predicado (misma consulta), igual que en los demas modulos.
 *
 * @param contenido reportes de la pagina actual
 * @param total     total de reportes que cumplen el filtro
 */
public record PaginaReportesDominio(List<ReporteGenerado> contenido, long total) {
}
