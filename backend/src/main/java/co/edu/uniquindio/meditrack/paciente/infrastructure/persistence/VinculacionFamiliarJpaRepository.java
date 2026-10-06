package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de vinculaciones familiar-paciente.
 */
public interface VinculacionFamiliarJpaRepository extends JpaRepository<VinculacionFamiliarEntity, Long> {

    Optional<VinculacionFamiliarEntity> findByUsuarioIdAndPacienteId(Long usuarioId, Long pacienteId);

    List<VinculacionFamiliarEntity> findByPacienteIdOrderByIdAsc(Long pacienteId);

    List<VinculacionFamiliarEntity> findByUsuarioIdAndAutorizadoTrue(Long usuarioId);
}
