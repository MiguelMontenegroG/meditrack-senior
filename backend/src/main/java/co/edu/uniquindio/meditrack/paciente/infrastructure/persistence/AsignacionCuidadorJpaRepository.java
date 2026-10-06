package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de asignaciones cuidador-paciente.
 *
 * <p>"Vigente" significa {@code hasta IS NULL} (indice unico parcial de V2).</p>
 */
public interface AsignacionCuidadorJpaRepository extends JpaRepository<AsignacionCuidadorEntity, Long> {

    Optional<AsignacionCuidadorEntity> findByUsuarioIdAndPacienteIdAndHastaIsNull(Long usuarioId, Long pacienteId);

    List<AsignacionCuidadorEntity> findByPacienteIdAndHastaIsNullOrderByIdAsc(Long pacienteId);

    List<AsignacionCuidadorEntity> findByUsuarioIdAndHastaIsNullOrderByIdAsc(Long usuarioId);
}
