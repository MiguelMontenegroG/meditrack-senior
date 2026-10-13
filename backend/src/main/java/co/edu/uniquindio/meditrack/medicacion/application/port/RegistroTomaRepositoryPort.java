package co.edu.uniquindio.meditrack.medicacion.application.port;

import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;

import java.time.Instant;
import java.util.List;

/**
 * Puerto de persistencia de tomas (historial clinico, solo insercion; nunca se
 * edita ni se borra).
 */
public interface RegistroTomaRepositoryPort {

    RegistroToma guardar(RegistroToma toma);

    /** Indica si ya hay una toma registrada para ese esquema en esa hora programada. */
    boolean existeParaEsquemaYProgramada(Long esquemaId, Instant fechaHoraProgramada);

    /** Tomas de un esquema en un rango de instantes programados (para el calculo de pendientes). */
    List<RegistroToma> listarPorEsquemaYRango(Long esquemaId, Instant desde, Instant hasta);

    /**
     * Historial paginado de tomas de un residente (a traves de sus esquemas),
     * con rango de fechas y esquema opcionales.
     */
    List<RegistroToma> listarHistorial(
            Long pacienteId, Long esquemaId, Instant desde, Instant hasta, int pagina, int tamano);

    long contarHistorial(Long pacienteId, Long esquemaId, Instant desde, Instant hasta);

    /** Tomas de todos los esquemas indicados dentro de un rango (para pendientes). */
    List<RegistroToma> listarPorEsquemasYRango(List<Long> esquemaIds, Instant desde, Instant hasta);
}
