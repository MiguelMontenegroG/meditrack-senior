package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.domain.Cita;

import java.util.List;

/**
 * Resultado paginado del listado de citas a nivel de aplicacion/dominio.
 *
 * <p>Agrupa la pagina de citas y el total de coincidencias calculado con el
 * mismo predicado que el contenido. Mantiene el dominio libre de tipos de Spring
 * ({@code Page}/{@code Pageable}); el adaptador de persistencia traduce el
 * {@code Page} de Spring Data a este record.</p>
 *
 * @param contenido citas de la pagina actual
 * @param total     total de citas que cumplen el filtro
 */
public record PaginaCitasDominio(List<Cita> contenido, long total) {
}