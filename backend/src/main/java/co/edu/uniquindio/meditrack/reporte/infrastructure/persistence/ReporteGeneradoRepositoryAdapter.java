package co.edu.uniquindio.meditrack.reporte.infrastructure.persistence;

import co.edu.uniquindio.meditrack.reporte.application.port.ReporteGeneradoRepositoryPort;
import co.edu.uniquindio.meditrack.reporte.domain.PaginaReportesDominio;
import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de los metadatos de reportes generados: implementa el
 * puerto de aplicacion sobre el repositorio Spring Data y traduce entidad JPA a
 * modelo de dominio.
 *
 * <p>Solo hay insercion (los reportes no se editan ni se borran). La entidad no
 * guarda el PDF: la generacion del binario es responsabilidad del adaptador de
 * PDF, no de la persistencia.</p>
 */
@Component
public class ReporteGeneradoRepositoryAdapter implements ReporteGeneradoRepositoryPort {

    private final ReporteGeneradoJpaRepository reportes;

    public ReporteGeneradoRepositoryAdapter(ReporteGeneradoJpaRepository reportes) {
        this.reportes = reportes;
    }

    @Override
    @Transactional
    public ReporteGenerado guardar(ReporteGenerado reporte) {
        ReporteGeneradoEntity entidad = new ReporteGeneradoEntity();
        entidad.setPacienteId(reporte.getPacienteId());
        entidad.setGeneradoPor(reporte.getGeneradoPor());
        entidad.setTipo(reporte.getTipo());
        entidad.setRangoDesde(reporte.getRangoDesde());
        entidad.setRangoHasta(reporte.getRangoHasta());
        entidad.setRutaAlmacenamiento(reporte.getRutaAlmacenamiento());
        return aDominio(reportes.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteGenerado> buscarPorId(Long id) {
        return reportes.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaReportesDominio listarPorPaciente(Long pacienteId, int pagina, int tamano) {
        Page<ReporteGeneradoEntity> resultado =
                reportes.listarPorPaciente(pacienteId, pageable(pagina, tamano));
        List<ReporteGenerado> contenido = resultado.getContent().stream().map(this::aDominio).toList();
        return new PaginaReportesDominio(contenido, resultado.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorPaciente(Long pacienteId) {
        return reportes.contarPorPaciente(pacienteId);
    }

    private static Pageable pageable(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano);
    }

    private ReporteGenerado aDominio(ReporteGeneradoEntity entidad) {
        return new ReporteGenerado(
                entidad.getId(),
                entidad.getPacienteId(),
                entidad.getGeneradoPor(),
                entidad.getTipo(),
                entidad.getRangoDesde(),
                entidad.getRangoHasta(),
                entidad.getRutaAlmacenamiento(),
                entidad.getCreadoEn());
    }
}