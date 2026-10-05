package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: obtener un usuario por id (solo administrador).
 */
public class ObtenerUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;

    public ObtenerUsuarioUseCase(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario ejecutar(Long id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
