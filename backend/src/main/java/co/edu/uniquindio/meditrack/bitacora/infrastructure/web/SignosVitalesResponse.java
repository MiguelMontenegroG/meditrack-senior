package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vista de un registro de signos vitales. Incluye el autor ({@code registradoPor})
 * y el momento de la medicion ({@code fechaHora}). La decision vigente del patron
 * de cita es mostrar las observaciones de texto libre tambien en el listado y el
 * detalle; aqui no hay datos clinicos de otro tipo que ocultar.
 */
public record SignosVitalesResponse(
        Long id,
        Long pacienteId,
        Long registradoPor,
        Instant fechaHora,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer glucosaMgDl,
        BigDecimal temperaturaC,
        String observaciones) {
}
