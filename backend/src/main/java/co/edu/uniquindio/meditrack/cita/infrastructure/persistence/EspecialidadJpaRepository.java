package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data del catalogo de especialidades medicas (solo lectura
 * para el modulo de citas).
 */
public interface EspecialidadJpaRepository extends JpaRepository<EspecialidadEntity, Long> {

    Optional<EspecialidadEntity> findByNombreIgnoreCase(String nombre);
}
