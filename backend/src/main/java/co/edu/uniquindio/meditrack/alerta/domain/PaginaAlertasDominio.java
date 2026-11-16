package co.edu.uniquindio.meditrack.alerta.domain;

import java.util.List;

/**
 * Resultado paginado del listado de alertas a nivel de aplicacion/dominio.
 *
 * <p>Agrupa la pagina de alertas y el total de coincidencias calculado con el
 * mismo predicado que el contenido. Mantiene el dominio libre de tipos de Spring
 * ({@code Page}/{@code Pageable}).</p>
 *
 * @param contenido alertas de la pagina actual
 * @param total     total de alertas que cumplen el filtro
 */
public record PaginaAlertasDominio(List<Alerta> contenido, long total) {
}
