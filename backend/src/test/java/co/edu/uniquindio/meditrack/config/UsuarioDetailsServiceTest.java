package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias de UsuarioDetailsService.
 *
 * Cubren: carga de un usuario activo (con su autoridad de rol), usuario
 * inexistente, identificador no numerico y usuario desactivado.
 */
class UsuarioDetailsServiceTest {

    private Usuario usuario(boolean activo) {
        return new Usuario(10L, Correo.de("user@meditrack.co"), "Usuario", null,
                "$2a$hash", new Rol(2L, RolNombre.CUIDADOR_ENFERMERO, null), activo, null, null, null);
    }

    private UsuarioRepositoryPort repositorio(Optional<Usuario> resultado) {
        return new UsuarioRepositoryPort() {
            @Override
            public Usuario guardar(Usuario usuario) {
                return usuario;
            }

            @Override
            public Optional<Usuario> buscarPorId(Long id) {
                return resultado;
            }

            @Override
            public Optional<Usuario> buscarPorCorreo(Correo correo) {
                return Optional.empty();
            }

            @Override
            public boolean existePorCorreo(Correo correo) {
                return false;
            }

            @Override
            public java.util.List<Usuario> listar(RolNombre rol, Boolean activo, int pagina, int tamano) {
                return java.util.List.of();
            }

            @Override
            public long contar(RolNombre rol, Boolean activo) {
                return 0;
            }

            @Override
            public long contarAdministradoresActivos() {
                return 0;
            }
        };
    }

    @Test
    void cargaUsuarioActivoConSuAutoridadDeRol() {
        UsuarioDetailsService servicio = new UsuarioDetailsService(repositorio(Optional.of(usuario(true))));

        UserDetails detalles = servicio.loadUserByUsername("10");

        assertThat(detalles.getUsername()).isEqualTo("10");
        assertThat(detalles.isEnabled()).isTrue();
        assertThat(detalles.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUIDADOR_ENFERMERO");
    }

    @Test
    void usuarioInexistenteLanzaExcepcion() {
        UsuarioDetailsService servicio = new UsuarioDetailsService(repositorio(Optional.empty()));

        assertThatThrownBy(() -> servicio.loadUserByUsername("99"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void identificadorNoNumericoLanzaExcepcion() {
        UsuarioDetailsService servicio = new UsuarioDetailsService(repositorio(Optional.of(usuario(true))));

        assertThatThrownBy(() -> servicio.loadUserByUsername("abc"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void usuarioDesactivadoLanzaExcepcion() {
        UsuarioDetailsService servicio = new UsuarioDetailsService(repositorio(Optional.of(usuario(false))));

        assertThatThrownBy(() -> servicio.loadUserByUsername("10"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
