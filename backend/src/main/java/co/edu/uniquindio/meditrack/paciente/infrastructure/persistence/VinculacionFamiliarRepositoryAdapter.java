package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.application.port.VinculacionFamiliarRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.VinculacionFamiliar;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de vinculaciones familiar-paciente.
 */
@Component
public class VinculacionFamiliarRepositoryAdapter implements VinculacionFamiliarRepositoryPort {

    private final VinculacionFamiliarJpaRepository repositorio;

    public VinculacionFamiliarRepositoryAdapter(VinculacionFamiliarJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public VinculacionFamiliar guardar(VinculacionFamiliar vinculacion) {
        VinculacionFamiliarEntity entidad = vinculacion.getId() == null
                ? new VinculacionFamiliarEntity()
                : repositorio.findById(vinculacion.getId()).orElseGet(VinculacionFamiliarEntity::new);

        entidad.setUsuarioId(vinculacion.getUsuarioId());
        entidad.setPacienteId(vinculacion.getPacienteId());
        entidad.setParentesco(vinculacion.getParentesco());
        entidad.setAutorizado(vinculacion.isAutorizado());

        return aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<VinculacionFamiliar> buscarPorUsuarioYPaciente(Long usuarioId, Long pacienteId) {
        return repositorio.findByUsuarioIdAndPacienteId(usuarioId, pacienteId).map(this::aDominio);
    }

    @Override
    public List<VinculacionFamiliar> listarPorPaciente(Long pacienteId) {
        return repositorio.findByPacienteIdOrderByIdAsc(pacienteId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<VinculacionFamiliar> listarPorUsuario(Long usuarioId) {
        return repositorio.findByUsuarioIdAndAutorizadoTrue(usuarioId).stream()
                .map(this::aDominio)
                .toList();
    }

    private VinculacionFamiliar aDominio(VinculacionFamiliarEntity entidad) {
        return new VinculacionFamiliar(
                entidad.getId(),
                entidad.getUsuarioId(),
                entidad.getPacienteId(),
                entidad.getParentesco(),
                entidad.isAutorizado(),
                entidad.getCreadoEn());
    }
}
