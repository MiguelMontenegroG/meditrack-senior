package co.edu.uniquindio.meditrack.usuario.config;

import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.usuario.application.ActualizarUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.AutenticarUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CambiarContrasenaUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CambiarEstadoUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ConsultarUsuarioActualUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CrearUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ListarUsuariosUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ObtenerUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import co.edu.uniquindio.meditrack.usuario.application.port.RolRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso de usuario como beans de Spring.
 *
 * <p>Los casos de uso viven en la capa de aplicacion y no llevan anotaciones de
 * Spring; se cablean aqui para mantener la independencia del framework.</p>
 */
@Configuration
public class UsuarioUseCaseConfig {

    @Bean
    public AutenticarUsuarioUseCase autenticarUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            PasswordEncoderPort contrasenas,
            JwtPort jwt,
            AuditoriaPort auditoria) {
        return new AutenticarUsuarioUseCase(usuarios, contrasenas, jwt, auditoria);
    }

    @Bean
    public ConsultarUsuarioActualUseCase consultarUsuarioActualUseCase(UsuarioRepositoryPort usuarios) {
        return new ConsultarUsuarioActualUseCase(usuarios);
    }

    @Bean
    public CambiarContrasenaUseCase cambiarContrasenaUseCase(
            UsuarioRepositoryPort usuarios,
            PasswordEncoderPort contrasenas,
            AuditoriaPort auditoria) {
        return new CambiarContrasenaUseCase(usuarios, contrasenas, auditoria);
    }

    @Bean
    public CrearUsuarioUseCase crearUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            RolRepositoryPort roles,
            PasswordEncoderPort contrasenas,
            AuditoriaPort auditoria) {
        return new CrearUsuarioUseCase(usuarios, roles, contrasenas, auditoria);
    }

    @Bean
    public ListarUsuariosUseCase listarUsuariosUseCase(UsuarioRepositoryPort usuarios) {
        return new ListarUsuariosUseCase(usuarios);
    }

    @Bean
    public ObtenerUsuarioUseCase obtenerUsuarioUseCase(UsuarioRepositoryPort usuarios) {
        return new ObtenerUsuarioUseCase(usuarios);
    }

    @Bean
    public ActualizarUsuarioUseCase actualizarUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            RolRepositoryPort roles,
            AuditoriaPort auditoria) {
        return new ActualizarUsuarioUseCase(usuarios, roles, auditoria);
    }

    @Bean
    public CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase(
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria) {
        return new CambiarEstadoUsuarioUseCase(usuarios, auditoria);
    }
}
