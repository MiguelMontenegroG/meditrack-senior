package co.edu.uniquindio.meditrack.reporte.infrastructure.web;

import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;
import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;

import java.time.Instant;

/**
 * Vista de los metadatos de un reporte generado. No incluye el PDF (que se
 * entrega como descarga aparte): solo identificacion y trazabilidad.
 */
public record ReporteResponse(
        Long id,
        Long pacienteId,
        Long generadoPor,
        TipoReporte tipo,
        Instant rangoDesde,
        Instant rangoHasta,
        Instant creadoEn) {

    public static ReporteResponse de(ReporteGenerado reporte) {
        return new ReporteResponse(
                reporte.getId(),
                reporte.getPacienteId(),
                reporte.getGeneradoPor(),
                reporte.getTipo(),
                reporte.getRangoDesde(),
                reporte.getRangoHasta(),
                reporte.getCreadoEn());
    }
}