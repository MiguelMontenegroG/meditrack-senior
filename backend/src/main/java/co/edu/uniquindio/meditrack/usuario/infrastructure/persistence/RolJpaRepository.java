package co.edu.uniquindio.meditrack.usuario.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data del catalogo de roles.
 */
public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {

    Optional<RolEntity> findByNombre(co.edu.uniquindio.meditrack.usuario.domain.RolNombre nombre);
}
