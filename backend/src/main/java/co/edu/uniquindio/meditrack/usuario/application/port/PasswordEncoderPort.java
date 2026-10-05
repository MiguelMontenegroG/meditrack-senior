package co.edu.uniquindio.meditrack.usuario.application.port;

/**
 * Puerto para cifrar y verificar contrasenas. La implementacion usa BCrypt
 * (delegado de Spring Security). El texto plano nunca se almacena ni se registra.
 */
public interface PasswordEncoderPort {

    String cifrar(String contrasenaPlano);

    boolean coincide(String contrasenaPlano, String hash);
}
