package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.application.port.RolRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial de forma idempotente.
 *
 * <p>Solo crea el usuario si se cumplen TODAS las condiciones:</p>
 * <ul>
 *   <li>no existe ningun usuario con rol ADMINISTRADOR, y</li>
 *   <li>estan definidas las variables ADMIN_INITIAL_EMAIL y ADMIN_INITIAL_PASSWORD.</li>
 * </ul>
 *
 * <p>Si faltan variables, registra un aviso sin valores y continua el arranque.
 * La contrasena y el hash nunca se registran en el log.</p>
 */
@Component
public class AdministradorInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicialRunner.class);

    private final UsuarioRepositoryPort usuarios;
    private final RolRepositoryPort roles;
    private final PasswordEncoderPort contrasenas;
    private final String correoInicial;
    private final String contrasenaInicial;

    public AdministradorInicialRunner(
            UsuarioRepositoryPort usuarios,
            RolRepositoryPort roles,
            PasswordEncoderPort contrasenas,
            @Value("${ADMIN_INITIAL_EMAIL:}") String correoInicial,
            @Value("${ADMIN_INITIAL_PASSWORD:}") String contrasenaInicial) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.contrasenas = contrasenas;
        this.correoInicial = correoInicial;
        this.contrasenaInicial = contrasenaInicial;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.contarAdministradoresActivos() > 0) {
            return;
        }

        if (correoInicial.isBlank() || contrasenaInicial.isBlank()) {
            log.warn("No hay administradores y faltan ADMIN_INITIAL_EMAIL o ADMIN_INITIAL_PASSWORD; "
                    + "no se crea el administrador inicial.");
            return;
        }

        Rol rolAdministrador = roles.buscarPorNombre(RolNombre.ADMINISTRADOR).orElse(null);
        if (rolAdministrador == null) {
            log.warn("El rol ADMINISTRADOR no existe (revisar migraciones); no se crea el administrador inicial.");
            return;
        }

        Correo correo = Correo.de(correoInicial);
        if (usuarios.existePorCorreo(correo)) {
            log.warn("Ya existe un usuario con el correo configurado para el administrador inicial; no se crea.");
            return;
        }

        Usuario administrador = Usuario.nuevo(
                correo,
                "Administrador inicial",
                null,
                contrasenas.cifrar(contrasenaInicial),
                rolAdministrador);
        usuarios.guardar(administrador);

        log.info("Administrador inicial creado satisfactoriamente.");
    }
}
