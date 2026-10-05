package co.edu.uniquindio.meditrack.usuario.application;

import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas del "ultimo administrador" y de la autodesactivacion.
 */
class ReglasAdministradorTest {

    private UsuarioRepositoryPort usuarios;
    private AuditoriaPort auditoria;

    @BeforeEach
    void configurar() {
        usuarios = mock(UsuarioRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
    }

    private Usuario administrador(Long id) {
        return new Usuario(id, Correo.de("admin" + id + "@meditrack.co"), "Admin " + id, null,
                "$2a$hash", new Rol(1L, RolNombre.ADMINISTRADOR, null), true, null, null, null);
    }

    @Test
    void administradorNoPuedeDesactivarseASiMismo() {
        when(usuarios.buscarPorId(5L)).thenReturn(Optional.of(administrador(5L)));
        CambiarEstadoUsuarioUseCase caso = new CambiarEstadoUsuarioUseCase(usuarios, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(5L, false, 5L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("su propia cuenta");
    }

    @Test
    void noSePuedeQuedarSinAdministradoresActivos() {
        when(usuarios.buscarPorId(5L)).thenReturn(Optional.of(administrador(5L)));
        when(usuarios.contarAdministradoresActivos()).thenReturn(1L);
        CambiarEstadoUsuarioUseCase caso = new CambiarEstadoUsuarioUseCase(usuarios, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(5L, false, 99L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("al menos un administrador activo");
    }

    @Test
    void desactivarOtroAdministradorHabiendoVariosEsValido() {
        Usuario objetivo = administrador(5L);
        when(usuarios.buscarPorId(5L)).thenReturn(Optional.of(objetivo));
        when(usuarios.contarAdministradoresActivos()).thenReturn(2L);
        when(usuarios.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        CambiarEstadoUsuarioUseCase caso = new CambiarEstadoUsuarioUseCase(usuarios, auditoria);

        Usuario resultado = caso.ejecutar(5L, false, 99L);

        assertThat(resultado.isActivo()).isFalse();
    }

    @Test
    void administradorNoPuedeQuitarseSuPropioRol() {
        when(usuarios.buscarPorId(5L)).thenReturn(Optional.of(administrador(5L)));
        when(usuarios.contarAdministradoresActivos()).thenReturn(2L);
        RolRepositoryPortFalso roles = new RolRepositoryPortFalso();
        ActualizarUsuarioUseCase caso = new ActualizarUsuarioUseCase(usuarios, roles, auditoria);

        ComandoActualizarUsuario comando =
                new ComandoActualizarUsuario("Admin", null, RolNombre.CUIDADOR_ENFERMERO);

        assertThatThrownBy(() -> caso.ejecutar(5L, comando, 5L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("su propio rol");
    }

    /** Doble de prueba minimo del puerto de roles. */
    private static final class RolRepositoryPortFalso
            implements co.edu.uniquindio.meditrack.usuario.application.port.RolRepositoryPort {

        @Override
        public Optional<Rol> buscarPorNombre(RolNombre nombre) {
            return Optional.of(new Rol(2L, nombre, null));
        }
    }
}
