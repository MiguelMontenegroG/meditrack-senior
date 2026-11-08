package co.edu.uniquindio.meditrack.alerta.application.port;

import java.time.Instant;
import java.util.List;

/**
 * Puerto de consulta de las fuentes que disparan alertas automaticas.
 *
 * <p>Cada metodo es <b>una sola consulta</b> que ya resuelve el filtro de
 * "residentes activos" y el de "sin alerta previa para la misma fuente" en la
 * base de datos: no hay N+1 ni se traen candidatos que luego se descarten en
 * memoria.</p>
 *
 * <p>Las tres fuentes son de lectura: la generacion de alertas nunca modifica
 * tomas, citas ni signos vitales.</p>
 */
public interface FuentesAlertaPort {

    /**
     * Tomas programadas antes de {@code limite} que pertenecen a esquemas activos
     * de residentes activos y que aun no tienen registro ni alerta.
     *
     * @param limite      instante a partir del cual una toma cuenta como atrasada
     * @param fechaCentro fecha del centro (America/Bogota) del instante de
     *                    referencia, en ISO-8601 ({@code yyyy-MM-dd}); la toma
     *                    programada de hoy es esta fecha mas la hora del horario
     * @param zona        zona horaria del centro (se usa como literal SQL de zona)
     */
    List<FuenteToma> tomasAtrasadas(Instant limite, String fechaCentro, String zona);

    /**
     * Citas en estado PROGRAMADA con fecha/hora anterior a {@code limite} de
     * residentes activos que aun no tienen alerta. No se modifica su estado.
     */
    List<FuenteCita> citasProgramadasVencidas(Instant limite);

    /**
     * Signos vitales registrados desde {@code desde} que estan fuera de los
     * umbrales de referencia y que aun no tienen alerta. El filtro de umbrales se
     * aplica en SQL para no traer la bitacora completa.
     */
    List<FuenteSignos> signosFueraDeRango(Instant desde, UmbralesConsulta umbrales);

    /**
     * Referencia de una toma atrasada (fuente 1).
     *
     * @param horarioId       id de {@code horario_medicacion} que origino la toma
     * @param pacienteId      residente dueno del esquema
     * @param registroTomaId  id de {@code registro_toma} del momento programado si
     *                        ya existe (una toma registrada nunca es atrasada, solo
     *                        puede ser una OMITIDA que revoca la alerta); {@code null}
     *                        si hay que materializarlo como OMITIDA para poder
     *                        referenciarlo desde {@code alerta.toma_id}
     */
    record FuenteToma(Long horarioId, Long pacienteId, Long registroTomaId) {
    }

    /** Referencia de una cita vencida (fuente 2). */
    record FuenteCita(Long citaId, Long pacienteId) {
    }

    /** Referencia de un registro de signos fuera de rango (fuente 3). */
    record FuenteSignos(Long signosId, Long pacienteId) {
    }

    /**
     * Umbrales de referencia en el formato que espera la consulta SQL (los mismos
     * valores de {@code meditrack.alertas.umbrales.*}).
     */
    record UmbralesConsulta(
            int sistolicaMin,
            int sistolicaMax,
            int diastolicaMin,
            int diastolicaMax,
            int glucosaMin,
            int glucosaMax,
            double temperaturaMin,
            double temperaturaMax) {
    }
}