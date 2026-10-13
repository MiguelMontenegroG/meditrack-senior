package co.edu.uniquindio.meditrack.medicacion.application.port;

import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de esquemas de medicacion (con sus horarios).
 *
 * <p>Las consultas por alcance se exponen como metodos separados (uno por rol de
 * la consulta de listado), evitando pasar el rol como parametro a la consulta.</p>
 */
public interface EsquemaMedicacionRepositoryPort {

    /** Guarda el esquema con sus horarios; devuelve el esquema persistido. */
    EsquemaMedicacion guardar(EsquemaMedicacion esquema);

    /** Reemplaza los horarios de un esquema existente por los indicados. */
    void reemplazarHorarios(Long esquemaId, List<java.time.LocalTime> horas);

    /** Busca un esquema por id, cargando sus horarios. */
    Optional<EsquemaMedicacion> buscarPorId(Long id);

    /**
     * Listado por residente para el alcance indicado. {@code activo} y
     * {@code vigente} son filtros opcionales (calculados: vigente significa
     * que hoy cae entre inicio y fin).
     */
    List<EsquemaMedicacion> listarPorPaciente(
            Long pacienteId, Boolean activo, Boolean vigente, LocalDate hoy);

    /** Esquemas activos y vigentes hoy de un residente (para tomas y pendientes). */
    List<EsquemaMedicacion> listarUtilizables(Long pacienteId, LocalDate hoy);

    /**
     * Deteccion de solapamiento de vigencia para un mismo medicamento activo del
     * residente. {@code fechaFin} nulo se trata como vigencia abierta.
     */
    boolean existeSolapamiento(
            Long pacienteId, Long medicamentoId, LocalDate fechaInicio, LocalDate fechaFin, Long idExcluido);
}
