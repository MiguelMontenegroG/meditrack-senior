package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.CredencialesInvalidasException;
import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Caso de uso: iniciar sesion con correo y contrasena.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Credenciales invalidas o usuario inexistente producen el mismo error
 *       generico (no se revela si el correo existe).</li>
 *   <li>Un usuario con activo = false no puede iniciar sesion.</li>
 *   <li>El acceso exitoso actualiza ultimo_acceso_en y se registra en auditoria.</li>
 * </ul>
 */
public class AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordEncoderPort contrasenas;
    private final JwtPort jwt;
    private final AuditoriaPort auditoria;

    public AutenticarUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            PasswordEncoderPort contrasenas,
            JwtPort jwt,
            AuditoriaPort auditoria) {
        this.usuarios = usuarios;
        this.contrasenas = contrasenas;
        this.jwt = jwt;
        this.auditoria = auditoria;
    }

    public ResultadoLogin ejecutar(String correoPlano, String contrasenaPlano) {
        Correo correo = Correo.de(correoPlano);
        Usuario usuario = usuarios.buscarPorCorreo(correo).orElse(null);

        if (usuario == null || !contrasenas.coincide(contrasenaPlano, usuario.getHashContrasena())) {
            auditoria.registrar(
                    usuario == null ? null : usuario.getId(),
                    AccionAuditoria.LOGIN,
                    "usuario",
                    usuario == null ? null : usuario.getId(),
                    ResultadoAuditoria.DENEGADO,
                    "Credenciales invalidas");
            throw new CredencialesInvalidasException();
        }

        if (!usuario.isActivo()) {
            auditoria.registrar(
                    usuario.getId(),
                    AccionAuditoria.LOGIN,
                    "usuario",
                    usuario.getId(),
                    ResultadoAuditoria.DENEGADO,
                    "Usuario inactivo");
            throw new CredencialesInvalidasException();
        }

        usuario.registrarAcceso();
        Usuario persistido = usuarios.guardar(usuario);

        String token = jwt.generarToken(persistido.getId(), persistido.getRol().getNombre());

        auditoria.registrar(
                persistido.getId(),
                AccionAuditoria.LOGIN,
                "usuario",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                "Inicio de sesion");

        return new ResultadoLogin(token, jwt.expiracionEnSegundos(), persistido);
    }
}
