package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.application.port.RegistroTomaRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Adaptador de persistencia de tomas (historial clinico, solo insercion).
 *
 * <p>El historial elige la consulta segun los filtros informados (esquema y
 * cada extremo del rango por separado) en lugar de sustituir los extremos
 * ausentes por instantes centinela o comparar parametros nulos dentro de la
 * consulta: ambas tecnicas fallaban en PostgreSQL
 * ({@code cannot cast type bytea to timestamp} y filtrado incorrecto contra
 * {@code EPOCH}).</p>
 */
@Component
public class RegistroTomaRepositoryAdapter implements RegistroTomaRepositoryPort {

    private final RegistroTomaJpaRepository repositorio;

    public RegistroTomaRepositoryAdapter(RegistroTomaJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public RegistroToma guardar(RegistroToma toma) {
        RegistroTomaEntity entity = new RegistroTomaEntity();
        entity.setEsquemaId(toma.getEsquemaId());
        entity.setRegistradoPor(toma.getRegistradoPor());
        entity.setFechaHoraProgramada(toma.getFechaHoraProgramada());
        entity.setFechaHoraRegistro(toma.getFechaHoraRegistro());
        entity.setEstado(toma.getEstado());
        entity.setObservaciones(toma.getObservaciones());
        return aDominio(repositorio.save(entity));
    }

    @Override
    public boolean existeParaEsquemaYProgramada(Long esquemaId, Instant fechaHoraProgramada) {
        return repositorio.existsByEsquemaIdAndFechaHoraProgramada(esquemaId, fechaHoraProgramada);
    }

    @Override
    public List<RegistroToma> listarPorEsquemaYRango(Long esquemaId, Instant desde, Instant hasta) {
        return repositorio.findByEsquemaIdAndFechaHoraProgramadaBetween(esquemaId, desde, hasta)
                .stream().map(RegistroTomaRepositoryAdapter::aDominio).toList();
    }

    @Override
    public List<RegistroToma> listarHistorial(
            Long pacienteId, Long esquemaId, Instant desde, Instant hasta, int pagina, int tamano) {
        PageRequest pageable = PageRequest.of(pagina, tamano);
        Page<RegistroTomaEntity> resultado = elegirListado(pacienteId, esquemaId, desde, hasta, pageable);
        return resultado.getContent().stream().map(RegistroTomaRepositoryAdapter::aDominio).toList();
    }

    @Override
    public long contarHistorial(Long pacienteId, Long esquemaId, Instant desde, Instant hasta) {
        if (esquemaId != null) {
            if (desde != null && hasta != null) {
                return repositorio.contarHistorialPorEsquemaYRango(pacienteId, esquemaId, desde, hasta);
            }
            if (desde != null) {
                return repositorio.contarHistorialPorEsquemaDesde(pacienteId, esquemaId, desde);
            }
            if (hasta != null) {
                return repositorio.contarHistorialPorEsquemaHasta(pacienteId, esquemaId, hasta);
            }
            return repositorio.contarHistorialPorEsquema(pacienteId, esquemaId);
        }
        if (desde != null && hasta != null) {
            return repositorio.contarHistorialPorRango(pacienteId, desde, hasta);
        }
        if (desde != null) {
            return repositorio.contarHistorialDesde(pacienteId, desde);
        }
        if (hasta != null) {
            return repositorio.contarHistorialHasta(pacienteId, hasta);
        }
        return repositorio.contarHistorialSinFiltros(pacienteId);
    }

    /**
     * Elige la consulta paginada segun los filtros presentes (esquema y cada
     * extremo del rango). Nunca se envia un parametro nulo a PostgreSQL.
     */
    private Page<RegistroTomaEntity> elegirListado(
            Long pacienteId, Long esquemaId, Instant desde, Instant hasta, PageRequest pageable) {
        if (esquemaId != null) {
            if (desde != null && hasta != null) {
                return repositorio.historialPorEsquemaYRango(pacienteId, esquemaId, desde, hasta, pageable);
            }
            if (desde != null) {
                return repositorio.historialPorEsquemaDesde(pacienteId, esquemaId, desde, pageable);
            }
            if (hasta != null) {
                return repositorio.historialPorEsquemaHasta(pacienteId, esquemaId, hasta, pageable);
            }
            return repositorio.historialPorEsquema(pacienteId, esquemaId, pageable);
        }
        if (desde != null && hasta != null) {
            return repositorio.historialPorRango(pacienteId, desde, hasta, pageable);
        }
        if (desde != null) {
            return repositorio.historialDesde(pacienteId, desde, pageable);
        }
        if (hasta != null) {
            return repositorio.historialHasta(pacienteId, hasta, pageable);
        }
        return repositorio.historialSinFiltros(pacienteId, pageable);
    }

    @Override
    public List<RegistroToma> listarPorEsquemasYRango(List<Long> esquemaIds, Instant desde, Instant hasta) {
        return repositorio.findByEsquemaIdInAndFechaHoraProgramadaBetween(esquemaIds, desde, hasta)
                .stream().map(RegistroTomaRepositoryAdapter::aDominio).toList();
    }

    private static RegistroToma aDominio(RegistroTomaEntity entity) {
        return new RegistroToma(
                entity.getId(), entity.getEsquemaId(), entity.getRegistradoPor(),
                entity.getFechaHoraProgramada(), entity.getFechaHoraRegistro(),
                entity.getEstado(), entity.getObservaciones());
    }
}
