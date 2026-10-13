package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.domain.ViaAdministracion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos de entrada para crear o actualizar un esquema de medicacion, incluidos
 * sus horarios (en la misma operacion transaccional).
 */
public record ComandoEsquema(
        Long medicamentoId,
        String dosis,
        int frecuenciaHoras,
        ViaAdministracion via,
        String indicaciones,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<LocalTime> horarios) {
}
