package co.edu.uniquindio.meditrack.usuario.application.port;

import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;

import java.util.Optional;

/**
 * Puerto de persistencia de roles. Los roles son fijos (los siembra V3).
 */
public interface RolRepositoryPort {

    Optional<Rol> buscarPorNombre(RolNombre nombre);
}
