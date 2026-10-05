package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.RolRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: actualizar nombre, telefono y rol de un usuario (solo administrador).
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Un administrador no puede quitarse a si mismo el rol ADMINISTRADOR.</li>
 *   <li>Siempre debe quedar al menos un administrador activo.</li>
 * </ul>
 */
public class ActualizarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final RolRepositoryPort roles;
    private final AuditoriaPort auditoria;

    public ActualizarUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            RolRepositoryPort roles,
            AuditoriaPort auditoria) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.auditoria = auditoria;
    }

    public Usuario ejecutar(Long usuarioId, ComandoActualizarUsuario comando, Long autorId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        RolNombre nuevoRolNombre = comando.rol();
        boolean pierdeAdministrador = usuario.esAdministrador()
                && nuevoRolNombre != RolNombre.ADMINISTRADOR;

        if (pierdeAdministrador && usuarioId.equals(autorId)) {
            throw new ConflictoDeNegocioException("Un administrador no puede quitarse su propio rol");
        }
        if (pierdeAdministrador && usuarios.contarAdministradoresActivos() <= 1) {
            throw new ConflictoDeNegocioException("Debe existir al menos un administrador activo");
        }

        usuario.actualizarDatos(comando.nombreCompleto(), comando.telefono());

        boolean cambioDeRol = usuario.getRol() == null
                || usuario.getRol().getNombre() != nuevoRolNombre;
        if (cambioDeRol) {
            Rol nuevoRol = roles.buscarPorNombre(nuevoRolNombre)
                    .orElseThrow(() -> new ConflictoDeNegocioException("El rol indicado no existe"));
            usuario.asignarRol(nuevoRol);
        }

        Usuario persistido = usuarios.guardar(usuario);

        auditoria.registrar(
                autorId,
                AccionAuditoria.UPDATE,
                "usuario",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                cambioDeRol
                        ? "Actualizacion de usuario y cambio de rol a " + nuevoRolNombre.name()
                        : "Actualizacion de datos de usuario");

        return persistido;
    }
}
