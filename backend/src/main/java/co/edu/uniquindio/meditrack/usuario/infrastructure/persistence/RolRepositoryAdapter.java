package co.edu.uniquindio.meditrack.usuario.infrastructure.persistence;

import co.edu.uniquindio.meditrack.usuario.application.port.RolRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de persistencia de roles: implementa el puerto de aplicacion
 * sobre el repositorio Spring Data.
 */
@Component
public class RolRepositoryAdapter implements RolRepositoryPort {

    private final RolJpaRepository repositorio;

    public RolRepositoryAdapter(RolJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Rol> buscarPorNombre(RolNombre nombre) {
        return repositorio.findByNombre(nombre).map(this::aDominio);
    }

    private Rol aDominio(RolEntity entidad) {
        return new Rol(entidad.getId(), entidad.getNombre(), entidad.getDescripcion());
    }
}
