package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.PasswordEncoderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracion del cifrado de contrasenas (BCrypt) y del puerto de aplicacion.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Adapta el {@link PasswordEncoder} de Spring al puerto del dominio,
     * de modo que la capa de aplicacion no dependa de Spring Security.
     */
    @Bean
    public PasswordEncoderPort passwordEncoderPort(PasswordEncoder passwordEncoder) {
        return new PasswordEncoderPort() {
            @Override
            public String cifrar(String contrasenaPlano) {
                return passwordEncoder.encode(contrasenaPlano);
            }

            @Override
            public boolean coincide(String contrasenaPlano, String hash) {
                return passwordEncoder.matches(contrasenaPlano, hash);
            }
        };
    }
}
