package co.edu.uniquindio.meditrack.usuario.application.port;

import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de usuarios. La implementacion vive en
 * infrastructure/persistence (adaptador sobre Spring Data JPA).
 */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    /** Busca por correo sin distinguir mayusculas. */
    Optional<Usuario> buscarPorCorreo(Correo correo);

    /** Indica si ya existe un usuario con ese correo (sin distinguir mayusculas). */
    boolean existePorCorreo(Correo correo);

    List<Usuario> listar(RolNombre rol, Boolean activo, int pagina, int tamano);

    long contar(RolNombre rol, Boolean activo);

    /** Numero de administradores activos; usado por la regla del ultimo administrador. */
    long contarAdministradoresActivos();
}
