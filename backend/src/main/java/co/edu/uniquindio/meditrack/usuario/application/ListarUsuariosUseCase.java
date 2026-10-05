package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

import java.util.List;

/**
 * Caso de uso: listar usuarios de forma paginada con filtros por rol y activo.
 */
public class ListarUsuariosUseCase {

    private final UsuarioRepositoryPort usuarios;

    public ListarUsuariosUseCase(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    public List<Usuario> ejecutar(RolNombre rol, Boolean activo, int pagina, int tamano) {
        return usuarios.listar(rol, activo, pagina, tamano);
    }

    public long contar(RolNombre rol, Boolean activo) {
        return usuarios.contar(rol, activo);
    }
}
