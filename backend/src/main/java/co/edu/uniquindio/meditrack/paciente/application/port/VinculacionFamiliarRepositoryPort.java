package co.edu.uniquindio.meditrack.paciente.application.port;

import co.edu.uniquindio.meditrack.paciente.domain.VinculacionFamiliar;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de vinculaciones familiar-paciente.
 */
public interface VinculacionFamiliarRepositoryPort {

    VinculacionFamiliar guardar(VinculacionFamiliar vinculacion);

    Optional<VinculacionFamiliar> buscarPorUsuarioYPaciente(Long usuarioId, Long pacienteId);

    List<VinculacionFamiliar> listarPorPaciente(Long pacienteId);

    List<VinculacionFamiliar> listarPorUsuario(Long usuarioId);
}
