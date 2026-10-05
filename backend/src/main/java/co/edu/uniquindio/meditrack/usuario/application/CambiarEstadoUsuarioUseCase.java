package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: activar o desactivar un usuario (solo administrador).
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Un administrador no puede desactivarse a si mismo.</li>
 *   <li>Siempre debe quedar al menos un administrador activo.</li>
 *   <li>Nunca se borra fisicamente un usuario.</li>
 * </ul>
 */
public class CambiarEstadoUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final AuditoriaPort auditoria;

    public CambiarEstadoUsuarioUseCase(UsuarioRepositoryPort usuarios, AuditoriaPort auditoria) {
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    public Usuario ejecutar(Long usuarioId, boolean activar, Long autorId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!activar && usuarioId.equals(autorId)) {
            throw new ConflictoDeNegocioException("Un administrador no puede desactivar su propia cuenta");
        }

        if (!activar && usuario.esAdministrador() && usuarios.contarAdministradoresActivos() <= 1) {
            throw new ConflictoDeNegocioException("Debe existir al menos un administrador activo");
        }

        if (activar) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }

        Usuario persistido = usuarios.guardar(usuario);

        auditoria.registrar(
                autorId,
                AccionAuditoria.UPDATE,
                "usuario",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                activar ? "Activacion de usuario" : "Desactivacion de usuario");

        return persistido;
    }
}
