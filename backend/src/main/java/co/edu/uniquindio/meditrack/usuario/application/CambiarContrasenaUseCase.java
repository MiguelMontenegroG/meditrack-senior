package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.NoAutenticadoException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: el propio usuario cambia su contrasena (contrasena actual + nueva).
 *
 * <p>La nueva contrasena debe tener al menos 8 caracteres.</p>
 */
public class CambiarContrasenaUseCase {

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private final UsuarioRepositoryPort usuarios;
    private final PasswordEncoderPort contrasenas;
    private final AuditoriaPort auditoria;

    public CambiarContrasenaUseCase(
            UsuarioRepositoryPort usuarios,
            PasswordEncoderPort contrasenas,
            AuditoriaPort auditoria) {
        this.usuarios = usuarios;
        this.contrasenas = contrasenas;
        this.auditoria = auditoria;
    }

    public void ejecutar(Long usuarioId, String contrasenaActual, String contrasenaNueva) {
        if (usuarioId == null) {
            throw new NoAutenticadoException("No hay usuario autenticado");
        }
        if (contrasenaNueva == null || contrasenaNueva.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ConflictoDeNegocioException("La contrasena nueva debe tener al menos 8 caracteres");
        }

        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!contrasenas.coincide(contrasenaActual, usuario.getHashContrasena())) {
            throw new ConflictoDeNegocioException("La contrasena actual no es correcta");
        }

        usuario.actualizarHashContrasena(contrasenas.cifrar(contrasenaNueva));
        usuarios.guardar(usuario);

        auditoria.registrar(
                usuarioId,
                AccionAuditoria.UPDATE,
                "usuario",
                usuarioId,
                ResultadoAuditoria.EXITOSO,
                "Cambio de contrasena");
    }
}
