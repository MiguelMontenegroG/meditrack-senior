package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.domain.NoAutenticadoException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: consultar los datos del usuario autenticado.
 */
public class ConsultarUsuarioActualUseCase {

    private final UsuarioRepositoryPort usuarios;

    public ConsultarUsuarioActualUseCase(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario ejecutar(Long usuarioId) {
        if (usuarioId == null) {
            throw new NoAutenticadoException("No hay usuario autenticado");
        }
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new NoAutenticadoException("El usuario autenticado ya no existe"));
        if (!usuario.isActivo()) {
            throw new NoAutenticadoException("La cuenta esta desactivada");
        }
        return usuario;
    }
}
