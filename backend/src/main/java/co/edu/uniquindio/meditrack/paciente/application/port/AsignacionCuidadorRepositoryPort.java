package co.edu.uniquindio.meditrack.paciente.application.port;

import co.edu.uniquindio.meditrack.paciente.domain.AsignacionCuidador;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de asignaciones cuidador-paciente.
 */
public interface AsignacionCuidadorRepositoryPort {

    AsignacionCuidador guardar(AsignacionCuidador asignacion);

    /** Busca la asignacion vigente (hasta == null) de un cuidador con un paciente. */
    Optional<AsignacionCuidador> buscarVigente(Long usuarioId, Long pacienteId);

    List<AsignacionCuidador> listarVigentesPorPaciente(Long pacienteId);

    List<AsignacionCuidador> listarVigentesPorUsuario(Long usuarioId);
}
