package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import co.edu.uniquindio.meditrack.cita.application.PaginaCitasDominio;
import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de citas: implementa el puerto de aplicacion sobre
 * el repositorio Spring Data y traduce entidad JPA a modelo de dominio.
 *
 * <p>Elige la consulta segun que filtros llegan informados (residente concreto o
 * conjunto de residentes visibles, y presencia de estado) para no enviar nunca a
 * PostgreSQL un parametro nulo. El total de la pagina lo resuelve Spring Data
 * con el mismo predicado que el contenido, de modo que contenido y total son
 * coherentes. La lectura no usa relaciones JPA perezosas: la cita guarda
 * {@code pacienteId} y {@code especialidadId} como columnas planas, por lo que no
 * hay consultas N+1 al mapear a dominio.</p>
 */
@Component
public class CitaRepositoryAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository citas;

    public CitaRepositoryAdapter(CitaJpaRepository citas) {
        this.citas = citas;
    }

    @Override
    @Transactional
    public Cita guardar(Cita cita) {
        CitaEntity entidad = cita.getId() == null
                ? new CitaEntity()
                : citas.findById(cita.getId()).orElseThrow();
        entidad.setPacienteId(cita.getPacienteId());
        entidad.setEspecialidadId(cita.getEspecialidadId());
        entidad.setRegistradaPor(cita.getRegistradaPor());
        entidad.setProfesional(cita.getProfesional());
        entidad.setFechaHora(cita.getFechaHora());
        entidad.setLugar(cita.getLugar());
        entidad.setMotivo(cita.getMotivo());
        entidad.setEstado(cita.getEstado());
        return aDominio(citas.save(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cita> buscarPorId(Long id) {
        return citas.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaCitasDominio listarPorPaciente(
            Long pacienteId, EstadoCita estado, Instant desde, Instant hasta, int pagina, int tamano) {
        Pageable pageable = pageable(pagina, tamano);
        Page<CitaEntity> resultado = estado != null
                ? citas.listarPorPacienteEstadoYRango(pacienteId, estado, desde, hasta, pageable)
                : citas.listarPorPacienteYRango(pacienteId, desde, hasta, pageable);
        return aPagina(resultado);
    }
    @Override
    @Transactional(readOnly = true)
    public PaginaCitasDominio listarPorPacientes(
            List<Long> pacienteIds, EstadoCita estado, Instant desde, Instant hasta,
            int pagina, int tamano) {
        // El caso de uso garantiza que pacienteIds no es vacio.
        Pageable pageable = pageable(pagina, tamano);
        Page<CitaEntity> resultado = estado != null
                ? citas.listarPorPacientesEstadoYRango(pacienteIds, estado, desde, hasta, pageable)
                : citas.listarPorPacientesYRango(pacienteIds, desde, hasta, pageable);
        return aPagina(resultado);
    }

    private PaginaCitasDominio aPagina(Page<CitaEntity> pagina) {
        List<Cita> contenido = pagina.getContent().stream().map(this::aDominio).toList();
        return new PaginaCitasDominio(contenido, pagina.getTotalElements());
    }

    private static Pageable pageable(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano);
    }

    private Cita aDominio(CitaEntity entidad) {
        return new Cita(
                entidad.getId(),
                entidad.getPacienteId(),
                entidad.getRegistradaPor(),
                entidad.getEspecialidadId(),
                entidad.getProfesional(),
                entidad.getFechaHora(),
                entidad.getLugar(),
                entidad.getMotivo(),
                entidad.getEstado());
    }
}
