package co.edu.uniquindio.meditrack.reporte.application;

import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;

import java.time.Instant;

/**
 * Datos de entrada para generar un reporte clinico. El autor del reporte no viaja
 * aqui: sale siempre del usuario autenticado.
 *
 * @param tipo   tipo de reporte (ya convertido de texto a enum en la capa web).
 * @param desde  inicio del rango (incluido).
 * @param hasta  fin del rango (incluido).
 */
public record ComandoReporte(TipoReporte tipo, Instant desde, Instant hasta) {
}
