package co.edu.uniquindio.meditrack.bitacora.infrastructure.persistence;

import co.edu.uniquindio.meditrack.bitacora.application.PaginaSignosVitalesDominio;
import co.edu.uniquindio.meditrack.bitacora.application.port.SignosVitalesRepositoryPort;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de la bitacora de signos vitales: implementa el
 * puerto de aplicacion sobre el repositorio Spring Data y traduce entidad JPA a
 * modelo de dominio.
 *
 * <p>Solo hay insercion (los registros son inmutables). El total de la pagina lo
 * resuelve Spring Data con el mismo predicado que el contenido, de modo que
 * contenido y total son coherentes. La lectura no usa relaciones JPA perezosas:
 * la entidad guarda {@code pacienteId} y {@code registradoPor} como columnas
 * planas, por lo que no hay consultas N+1 al mapear a dominio.</p>
 */
@Component
public class RegistroSignosVitalesRepositoryAdapter implements SignosVitalesRepositoryPort {

    private final RegistroSignosVitalesJpaRepository signos;

    public RegistroSignosVitalesRepositoryAdapter(RegistroSignosVitalesJpaRepository signos) {
        this.signos = signos;
    }

    @Override
    @Transactional
    public RegistroSignosVitales guardar(RegistroSignosVitales registro) {
        RegistroSignosVitalesEntity entidad = new RegistroSignosVitalesEntity();
        entidad.setPacienteId(registro.getPacienteId());
        entidad.setRegistradoPor(registro.getRegistradoPor());
        entidad.setFechaHora(registro.getFechaHora());
        entidad.setPresionSistolica(registro.getPresionSistolica());
        entidad.setPresionDiastolica(registro.getPresionDiastolica());
        entidad.setGlucosaMgDl(registro.getGlucosaMgDl());
        entidad.setTemperaturaC(registro.getTemperaturaC());
        entidad.setObservaciones(registro.getObservaciones());
        return aDominio(signos.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSignosVitalesDominio listarPorPaciente(Long pacienteId, int pagina, int tamano) {
        return aPagina(signos.listarPorPaciente(pacienteId, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSignosVitalesDominio listarPorPacienteDesde(
            Long pacienteId, Instant desde, int pagina, int tamano) {
        return aPagina(signos.listarPorPacienteDesde(pacienteId, desde, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSignosVitalesDominio listarPorPacienteHasta(
            Long pacienteId, Instant hasta, int pagina, int tamano) {
        return aPagina(signos.listarPorPacienteHasta(pacienteId, hasta, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSignosVitalesDominio listarPorPacienteYRango(
            Long pacienteId, Instant desde, Instant hasta, int pagina, int tamano) {
        return aPagina(signos.listarPorPacienteYRango(pacienteId, desde, hasta, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroSignosVitales> buscarUltimoPorPaciente(Long pacienteId) {
        return signos.buscarUltimo(pacienteId).map(this::aDominio);
    }

    private PaginaSignosVitalesDominio aPagina(Page<RegistroSignosVitalesEntity> pagina) {
        List<RegistroSignosVitales> contenido = pagina.getContent().stream().map(this::aDominio).toList();
        return new PaginaSignosVitalesDominio(contenido, pagina.getTotalElements());
    }

    private static Pageable pageable(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano);
    }

    private RegistroSignosVitales aDominio(RegistroSignosVitalesEntity entidad) {
        return new RegistroSignosVitales(
                entidad.getId(),
                entidad.getPacienteId(),
                entidad.getRegistradoPor(),
                entidad.getFechaHora(),
                entidad.getPresionSistolica(),
                entidad.getPresionDiastolica(),
                entidad.getGlucosaMgDl(),
                entidad.getTemperaturaC(),
                entidad.getObservaciones());
    }
}
