package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.application.port.AsignacionCuidadorRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AsignacionCuidador;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de asignaciones cuidador-paciente.
 */
@Component
public class AsignacionCuidadorRepositoryAdapter implements AsignacionCuidadorRepositoryPort {

    private final AsignacionCuidadorJpaRepository repositorio;

    public AsignacionCuidadorRepositoryAdapter(AsignacionCuidadorJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public AsignacionCuidador guardar(AsignacionCuidador asignacion) {
        AsignacionCuidadorEntity entidad = asignacion.getId() == null
                ? new AsignacionCuidadorEntity()
                : repositorio.findById(asignacion.getId()).orElseGet(AsignacionCuidadorEntity::new);

        entidad.setUsuarioId(asignacion.getUsuarioId());
        entidad.setPacienteId(asignacion.getPacienteId());
        entidad.setDesde(asignacion.getDesde());
        entidad.setHasta(asignacion.getHasta());

        return aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<AsignacionCuidador> buscarVigente(Long usuarioId, Long pacienteId) {
        return repositorio.findByUsuarioIdAndPacienteIdAndHastaIsNull(usuarioId, pacienteId).map(this::aDominio);
    }

    @Override
    public List<AsignacionCuidador> listarVigentesPorPaciente(Long pacienteId) {
        return repositorio.findByPacienteIdAndHastaIsNullOrderByIdAsc(pacienteId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<AsignacionCuidador> listarVigentesPorUsuario(Long usuarioId) {
        return repositorio.findByUsuarioIdAndHastaIsNullOrderByIdAsc(usuarioId).stream()
                .map(this::aDominio)
                .toList();
    }

    private AsignacionCuidador aDominio(AsignacionCuidadorEntity entidad) {
        return new AsignacionCuidador(
                entidad.getId(),
                entidad.getUsuarioId(),
                entidad.getPacienteId(),
                entidad.getDesde(),
                entidad.getHasta(),
                entidad.getCreadoEn());
    }
}
