package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.domain.ViaAdministracion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Vista de un esquema de medicacion con sus horarios.
 */
public record EsquemaResponse(
        Long id,
        Long pacienteId,
        Long medicamentoId,
        String medicamentoNombre,
        String dosis,
        int frecuenciaHoras,
        ViaAdministracion via,
        String indicaciones,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activo,
        boolean vigente,
        List<LocalTime> horarios) {
}
