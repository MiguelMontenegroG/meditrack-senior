package co.edu.uniquindio.meditrack.reporte.config;

import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.reporte.application.GenerarReporteUseCase;
import co.edu.uniquindio.meditrack.reporte.application.ListarReportesUseCase;
import co.edu.uniquindio.meditrack.reporte.application.port.DatosReportePort;
import co.edu.uniquindio.meditrack.reporte.application.port.GeneradorPdfPort;
import co.edu.uniquindio.meditrack.reporte.application.port.ReporteGeneradoRepositoryPort;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra los casos de uso del modulo de reportes como beans de Spring.
 *
 * <p>Los casos de uso son POJOs sin anotaciones de Spring; se cablean aqui. Se
 * reutilizan el {@link VerificadorAlcanceResidente} (modulo medicacion), el bean
 * {@code Clock} compartido (America/Bogota) y los adaptadores del propio modulo
 * (persistencia de metadatos, recoleccion de datos y generacion de PDF).</p>
 */
@Configuration
public class ReporteUseCaseConfig {

    @Bean
    public ListarReportesUseCase listarReportesUseCase(
            ReporteGeneradoRepositoryPort reportes,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ListarReportesUseCase(reportes, alcance, auditoria);
    }

    @Bean
    public GenerarReporteUseCase generarReporteUseCase(
            VerificadorAlcanceResidente alcance,
            DatosReportePort datos,
            GeneradorPdfPort generador,
            ReporteGeneradoRepositoryPort reportes,
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria,
            Clock clock) {
        return new GenerarReporteUseCase(alcance, datos, generador, reportes, usuarios, auditoria, clock);
    }
}