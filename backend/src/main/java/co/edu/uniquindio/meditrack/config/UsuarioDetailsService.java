package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Carga un usuario para Spring Security a partir de su id (claim sub del JWT).
 *
 * <p>Un usuario inexistente o con activo = false lanza
 * {@link UsernameNotFoundException}: asi, un usuario desactivado no puede usar
 * un token vigente.</p>
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepositoryPort usuarios;

    public UsuarioDetailsService(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String usuarioId) throws UsernameNotFoundException {
        Long id;
        try {
            id = Long.valueOf(usuarioId);
        } catch (NumberFormatException ex) {
            throw new UsernameNotFoundException("Identificador de usuario invalido");
        }

        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado");
        }

        return User.withUsername(String.valueOf(usuario.getId()))
                .password(usuario.getHashContrasena())
                .authorities(List.of(new SimpleGrantedAuthority(usuario.getRol().getNombre().authority())))
                .disabled(!usuario.isActivo())
                .build();
    }
}
