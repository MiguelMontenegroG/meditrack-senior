package co.edu.uniquindio.meditrack.reporte.application;

import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.reporte.application.port.ReporteGeneradoRepositoryPort;
import co.edu.uniquindio.meditrack.reporte.domain.AlcanceReporte;
import co.edu.uniquindio.meditrack.reporte.domain.PaginaReportesDominio;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;

/**
 * Caso de uso: historial paginado de los reportes generados de un residente.
 *
 * <p>Es solo lectura de metadatos: valida el alcance del residente (404 si no es
 * visible, auditado como READ DENEGADO), pagina el listado por fecha de creacion
 * descendente y lo audita como READ EXITOSO aunque no haya reportes.</p>
 */
public class ListarReportesUseCase {

    private final ReporteGeneradoRepositoryPort reportes;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ListarReportesUseCase(
            ReporteGeneradoRepositoryPort reportes,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.reportes = reportes;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public PaginaReportesDominio ejecutar(
            Long pacienteId, int pagina, int tamano, AlcanceReporte autor) {
        alcance.exigirVisible(pacienteId, aAlcanceMedicacion(autor));
        PaginaReportesDominio resultado = reportes.listarPorPaciente(pacienteId, pagina, tamano);
        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "reporte",
                pacienteId, ResultadoAuditoria.EXITOSO, "Historial de reportes");
        return resultado;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceReporte alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}