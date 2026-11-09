package co.edu.uniquindio.meditrack.reporte.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Proyecciones de las secciones de un reporte clinico. Solo contienen texto ya
 * formateado y datos primitivos, de modo que el adaptador de PDF no dependa del
 * dominio de otros modulos ni consulte la base de datos.
 */
public final class SeccionReporte {

    private SeccionReporte() {
        // Contenedor de tipos: no se instancia.
    }

    /**
     * Cabecera del residente: identificacion y datos clinicos generales.
     * {@code edad} se calcula a la fecha de generacion del reporte.
     */
    public record Cabecera(
            String nombreCompleto,
            String documento,
            Integer edad,
            String sexo,
            String habitacion,
            String situacionClinica,
            java.time.LocalDate fechaIngreso,
            boolean activo) {
    }

    /**
     * Fila de la seccion de medicacion: un esquema del residente, con su estado de
     * vigencia a la fecha del reporte y el numero de tomas registradas del periodo
     * (administradas y omitidas).
     */
    public record FilaMedicacion(
            String medicamento,
            String dosis,
            String via,
            int frecuenciaHoras,
            String indicaciones,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            boolean activo,
            String vigencia,
            long tomasAdministradas,
            long tomasOmitidas) {
    }

    /**
     * Fila de la seccion de citas: cada cita del residente cuyo horario cae en el
     * periodo del reporte.
     */
    public record FilaCita(
            Instant fechaHora,
            String especialidad,
            String profesional,
            String lugar,
            String motivo,
            String estado) {
    }

    /**
     * Fila de la seccion de bitacora: un registro de signos vitales del periodo.
     * Las medidas son nulas cuando no se registraron.
     */
    public record FilaBitacora(
            Instant fechaHora,
            Integer presionSistolica,
            Integer presionDiastolica,
            Integer glucosaMgDl,
            BigDecimal temperaturaC,
            String observaciones) {

        /** Presion formateada, "sistolica/diastolica" o un guion. */
        public String presionFormateada() {
            if (presionSistolica == null || presionDiastolica == null) {
                return "-";
            }
            return presionSistolica + "/" + presionDiastolica;
        }

        /** Glucosa formateada con unidades o un guion. */
        public String glucosaFormateada() {
            return glucosaMgDl == null ? "-" : glucosaMgDl + " mg/dL";
        }

        /** Temperatura formateada con un decimal y unidades, o un guion. */
        public String temperaturaFormateada() {
            return temperaturaC == null ? "-" : temperaturaC.toPlainString() + " \u00b0C";
        }
    }

    /**
     * Fila de la seccion de alertas: alertas del residente creadas en el periodo.
     */
    public record FilaAlerta(
            Instant creadoEn,
            String tipo,
            String severidad,
            String estado,
            String mensaje) {
    }
}
