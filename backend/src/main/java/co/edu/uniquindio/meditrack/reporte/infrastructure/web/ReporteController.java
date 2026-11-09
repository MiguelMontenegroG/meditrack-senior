package co.edu.uniquindio.meditrack.reporte.infrastructure.web;

import co.edu.uniquindio.meditrack.reporte.application.ComandoReporte;
import co.edu.uniquindio.meditrack.reporte.application.GenerarReporteUseCase;
import co.edu.uniquindio.meditrack.reporte.application.ListarReportesUseCase;
import co.edu.uniquindio.meditrack.reporte.domain.AlcanceReporte;
import co.edu.uniquindio.meditrack.reporte.domain.PaginaReportesDominio;
import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;
import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;
import co.edu.uniquindio.meditrack.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Endpoints del modulo de reportes clinicos en PDF (Proceso 3).
 *
 * <ul>
 *   <li>GET /api/pacientes/{pacienteId}/reportes: historial paginado de reportes
 *   generados de un residente (los tres roles, lectura segun alcance).</li>
 *   <li>GET /api/pacientes/{pacienteId}/reportes/{tipo}.pdf: genera y descarga
 *   un reporte del residente en PDF para el rango indicado (los tres roles,
 *   lectura segun alcance).</li>
 * </ul>
 *
 * <p>La generacion es una lectura agregada (no modifica datos clinicos). El
 * filtrado por alcance se hace en el backend: un residente inexistente o fuera de
 * alcance responde 404 (nunca 403), sin revelar su existencia. El campo
 * generadoPor sale siempre del usuario autenticado.</p>
 */
@RestController
@Tag(name = "Reportes",
        description = "Generacion y descarga de reportes clinicos en PDF de un residente, e historial de reportes")
public class ReporteController {

    /** Tamano de pagina por defecto y maximo, iguales a los demas listados. */
    private static final int TAMANO_PAGINA_DEFECTO = 20;
    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final GenerarReporteUseCase generar;
    private final ListarReportesUseCase historial;

    public ReporteController(
            GenerarReporteUseCase generar,
            ListarReportesUseCase historial) {
        this.generar = generar;
        this.historial = historial;
    }

    @GetMapping("/api/pacientes/{pacienteId}/reportes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Historial paginado de reportes generados de un residente (lectura segun alcance)")
    public PageResponse<ReporteResponse> historial(
            @PathVariable Long pacienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication autenticacion) {
        AlcanceReporte alcance = AlcanceReporteWeb.de(autenticacion);
        int tamanoAcotado = acotarTamano(tamano);
        PaginaReportesDominio resultado = historial.ejecutar(pacienteId, pagina, tamanoAcotado, alcance);
        List<ReporteResponse> contenido = resultado.contenido().stream()
                .map(ReporteResponse::de)
                .toList();
        return PageResponse.de(contenido, pagina, tamanoAcotado, resultado.total());
    }

    @GetMapping("/api/pacientes/{pacienteId}/reportes/{tipo}.pdf")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Genera y descarga un reporte clinico en PDF (lectura segun alcance)")
    public ResponseEntity<byte[]> generar(
            @PathVariable Long pacienteId,
            @PathVariable String tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            Authentication autenticacion) {
        AlcanceReporte alcance = AlcanceReporteWeb.de(autenticacion);
        TipoReporte tipoReporte = TipoReporte.desde(tipo);
        GenerarReporteUseCase.Resultado resultado = generar.generar(
                pacienteId, new ComandoReporte(tipoReporte, desde, hasta), alcance);

        ReporteGenerado reporte = resultado.reporte();
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(reporte.nombreArchivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resultado.pdf());
    }

    private static int acotarTamano(int tamano) {
        if (tamano < 1) {
            return TAMANO_PAGINA_DEFECTO;
        }
        return Math.min(tamano, TAMANO_PAGINA_MAXIMO);
    }
}