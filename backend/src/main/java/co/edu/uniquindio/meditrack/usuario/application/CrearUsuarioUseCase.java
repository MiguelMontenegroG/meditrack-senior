package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import co.edu.uniquindio.meditrack.usuario.application.port.RolRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: crear un usuario (solo administrador).
 *
 * <p>Regla: el correo es unico sin distinguir mayusculas.</p>
 */
public class CrearUsuarioUseCase {

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private final UsuarioRepositoryPort usuarios;
    private final RolRepositoryPort roles;
    private final PasswordEncoderPort contrasenas;
    private final AuditoriaPort auditoria;

    public CrearUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            RolRepositoryPort roles,
            PasswordEncoderPort contrasenas,
            AuditoriaPort auditoria) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.contrasenas = contrasenas;
        this.auditoria = auditoria;
    }

    public Usuario ejecutar(ComandoCrearUsuario comando, Long autorId) {
        Correo correo = comando.correoNormalizado();

        if (usuarios.existePorCorreo(correo)) {
            throw new ConflictoDeNegocioException("Ya existe un usuario con ese correo");
        }
        if (comando.contrasena() == null || comando.contrasena().length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ConflictoDeNegocioException("La contrasena debe tener al menos 8 caracteres");
        }

        RolNombre rolNombre = comando.rol();
        Rol rol = roles.buscarPorNombre(rolNombre)
                .orElseThrow(() -> new ConflictoDeNegocioException("El rol indicado no existe"));

        Usuario usuario = Usuario.nuevo(
                correo,
                comando.nombreCompleto(),
                comando.telefono(),
                contrasenas.cifrar(comando.contrasena()),
                rol);

        Usuario persistido = usuarios.guardar(usuario);

        auditoria.registrar(
                autorId,
                AccionAuditoria.CREATE,
                "usuario",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                "Creacion de usuario con rol " + rolNombre.name());

        return persistido;
    }
}
