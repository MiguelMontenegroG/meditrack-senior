package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.CredencialesInvalidasException;
import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import co.edu.uniquindio.meditrack.usuario.application.port.TokenJwt;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de autenticacion.
 */
class AutenticarUsuarioUseCaseTest {

    private UsuarioRepositoryPort usuarios;
    private PasswordEncoderPort contrasenas;
    private JwtPort jwt;
    private AuditoriaPort auditoria;
    private AutenticarUsuarioUseCase casoDeUso;

    @BeforeEach
    void configurar() {
        usuarios = mock(UsuarioRepositoryPort.class);
        contrasenas = mock(PasswordEncoderPort.class);
        jwt = mock(JwtPort.class);
        auditoria = mock(AuditoriaPort.class);
        casoDeUso = new AutenticarUsuarioUseCase(usuarios, contrasenas, jwt, auditoria);
    }

    private Usuario usuarioActivo() {
        Rol rol = new Rol(1L, RolNombre.ADMINISTRADOR, "Acceso completo");
        return new Usuario(10L, Correo.de("admin@meditrack.co"), "Admin", null,
                "$2a$hash", rol, true, null, null, null);
    }

    @Test
    void loginExitosoDevuelveToken() {
        Usuario usuario = usuarioActivo();
        when(usuarios.buscarPorCorreo(any())).thenReturn(Optional.of(usuario));
        when(contrasenas.coincide(eq("secreta123"), eq("$2a$hash"))).thenReturn(true);
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generarToken(eq(10L), eq(RolNombre.ADMINISTRADOR))).thenReturn("jwt.token");
        when(jwt.expiracionEnSegundos()).thenReturn(3600L);

        ResultadoLogin resultado = casoDeUso.ejecutar("ADMIN@meditrack.co", "secreta123");

        assertThat(resultado.token()).isEqualTo("jwt.token");
        assertThat(resultado.expiraEnSegundos()).isEqualTo(3600L);
        assertThat(resultado.usuario().getId()).isEqualTo(10L);
        verify(auditoria).registrar(eq(10L), eq(AccionAuditoria.LOGIN), eq("usuario"), eq(10L),
                eq(ResultadoAuditoria.EXITOSO), any());
    }

    @Test
    void contrasenaIncorrectaLanzaExcepcionYAudita() {
        Usuario usuario = usuarioActivo();
        when(usuarios.buscarPorCorreo(any())).thenReturn(Optional.of(usuario));
        when(contrasenas.coincide(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> casoDeUso.ejecutar("admin@meditrack.co", "mala"))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(auditoria).registrar(eq(10L), eq(AccionAuditoria.LOGIN), eq("usuario"), eq(10L),
                eq(ResultadoAuditoria.DENEGADO), any());
    }

    @Test
    void usuarioInexistenteLanzaExcepcionGenerica() {
        when(usuarios.buscarPorCorreo(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.ejecutar("nadie@meditrack.co", "x"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void usuarioInactivoNoPuedeIniciarSesion() {
        Usuario inactivo = new Usuario(10L, Correo.de("admin@meditrack.co"), "Admin", null,
                "$2a$hash", new Rol(1L, RolNombre.ADMINISTRADOR, null), false, null, null, null);
        when(usuarios.buscarPorCorreo(any())).thenReturn(Optional.of(inactivo));
        when(contrasenas.coincide(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> casoDeUso.ejecutar("admin@meditrack.co", "secreta123"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
