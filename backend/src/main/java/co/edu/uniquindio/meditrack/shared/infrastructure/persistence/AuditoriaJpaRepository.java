package co.edu.uniquindio.meditrack.shared.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data de auditoria_acceso (solo insercion).
 */
public interface AuditoriaJpaRepository extends JpaRepository<AuditoriaEntity, Long> {
}
