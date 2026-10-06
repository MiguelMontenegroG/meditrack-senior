package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de contactos de emergencia.
 */
public interface ContactoEmergenciaJpaRepository extends JpaRepository<ContactoEmergenciaEntity, Long> {

    List<ContactoEmergenciaEntity> findByPacienteIdOrderByIdAsc(Long pacienteId);

    Optional<ContactoEmergenciaEntity> findFirstByPacienteIdAndEsPrincipalTrue(Long pacienteId);
}
