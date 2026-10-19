package co.edu.uniquindio.meditrack.bitacora.application;

import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;

import java.util.List;

/**
 * Resultado paginado del historial de signos vitales a nivel de
 * aplicacion/dominio. Mantiene el dominio libre de tipos de Spring
 * ({@code Page}/{@code Pageable}); el adaptador de persistencia traduce el
 * {@code Page} de Spring Data a este record. El total se calcula con el mismo
 * predicado que el contenido.
 *
 * @param contenido registros de la pagina actual (orden por fecha descendente)
 * @param total     total de registros que cumplen el filtro
 */
public record PaginaSignosVitalesDominio(List<RegistroSignosVitales> contenido, long total) {
}
