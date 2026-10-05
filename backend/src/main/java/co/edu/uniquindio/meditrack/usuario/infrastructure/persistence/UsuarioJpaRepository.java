package co.edu.uniquindio.meditrack.usuario.infrastructure.persistence;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio Spring Data de usuarios.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    /** Busca por correo comparando en minusculas (indice uq_usuario_correo_lower). */
    @Query("select u from UsuarioEntity u where lower(u.correo) = lower(:correo)")
    Optional<UsuarioEntity> buscarPorCorreo(@Param("correo") String correo);

    @Query("select count(u) > 0 from UsuarioEntity u where lower(u.correo) = lower(:correo)")
    boolean existePorCorreo(@Param("correo") String correo);

    /**
     * Listado paginado con filtros opcionales por rol y por estado activo.
     */
    @Query("""
            select u from UsuarioEntity u
            where (:rolNombre is null or u.rol.nombre = :rolNombre)
              and (:activo is null or u.activo = :activo)
            """)
    Page<UsuarioEntity> listar(
            @Param("rolNombre") RolNombre rolNombre,
            @Param("activo") Boolean activo,
            Pageable pageable);

    @Query("""
            select count(u) from UsuarioEntity u
            where (:rolNombre is null or u.rol.nombre = :rolNombre)
              and (:activo is null or u.activo = :activo)
            """)
    long contar(
            @Param("rolNombre") RolNombre rolNombre,
            @Param("activo") Boolean activo);

    @Query("""
            select count(u) from UsuarioEntity u
            where u.rol.nombre = co.edu.uniquindio.meditrack.usuario.domain.RolNombre.ADMINISTRADOR
              and u.activo = true
            """)
    long contarAdministradoresActivos();
}
