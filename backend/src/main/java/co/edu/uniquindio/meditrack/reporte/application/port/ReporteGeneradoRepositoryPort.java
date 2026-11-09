package co.edu.uniquindio.meditrack.reporte.application.port;

import co.edu.uniquindio.meditrack.reporte.domain.PaginaReportesDominio;
import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;

import java.util.Optional;

/**
 * Puerto de persistencia de los metadatos de reportes generados
 * ({@code reporte_generado} de la migracion V2).
 *
 * <p>Los reportes solo se insertan (append-only): nunca se editan ni se borran.
 * La tabla no guarda el PDF, solo metadatos y la ruta de almacenamiento, por lo
 * que {@code guardar} no escribe binarios.</p>
 */
public interface ReporteGeneradoRepositoryPort {

    /** Persiste los metadatos de un reporte nuevo y lo devuelve con su id. */
    ReporteGenerado guardar(ReporteGenerado reporte);

    Optional<ReporteGenerado> buscarPorId(Long id);

    /** Historial paginado de reportes de un residente (mas reciente primero). */
    PaginaReportesDominio listarPorPaciente(Long pacienteId, int pagina, int tamano);

    /** Total de reportes de un residente (para resumen de la generacion). */
    long contarPorPaciente(Long pacienteId);
}
