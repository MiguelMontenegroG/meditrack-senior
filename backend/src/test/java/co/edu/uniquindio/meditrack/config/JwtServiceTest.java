package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.TokenJwt;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias del servicio de JWT.
 *
 * Cubren: token valido, token expirado, token manipulado y secreto demasiado corto.
 */
class JwtServiceTest {

    private static final String SECRETO_VALIDO = "una-clave-secreta-suficientemente-larga-de-prueba-123456";
    private static final String SECRETO_CORTO = "corta";

    @Test
    void generaYValidaTokenCorrectamente() {
        JwtService servicio = new JwtService(SECRETO_VALIDO, 60);

        String token = servicio.generarToken(42L, RolNombre.ADMINISTRADOR);

        assertThat(token).isNotBlank();
        TokenJwt datos = servicio.validarToken(token);
        assertThat(datos.usuarioId()).isEqualTo(42L);
        assertThat(datos.rol()).isEqualTo(RolNombre.ADMINISTRADOR);
    }

    @Test
    void rechazaTokenExpirado() {
        // Se construye un token ya expirado con la misma clave para probar solo la validacion.
        SecretKey clave = Keys.hmacShaKeyFor(SECRETO_VALIDO.getBytes(StandardCharsets.UTF_8));
        long ahora = System.currentTimeMillis();
        String tokenExpirado = Jwts.builder()
                .subject("1")
                .claim("rol", RolNombre.CUIDADOR_ENFERMERO.name())
                .issuedAt(new Date(ahora - 120_000))
                .expiration(new Date(ahora - 60_000))
                .signWith(clave)
                .compact();
        JwtService servicio = new JwtService(SECRETO_VALIDO, 60);
        assertThatThrownBy(() -> servicio.validarToken(tokenExpirado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaTokenManipulado() {
        JwtService servicio = new JwtService(SECRETO_VALIDO, 60);
        String token = servicio.generarToken(7L, RolNombre.FAMILIAR_AUTORIZADO);

        // Se altera un caracter de la firma.
        String manipulado = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> servicio.validarToken(manipulado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaSecretoCortoAlArrancar() {
        assertThatThrownBy(() -> new JwtService(SECRETO_CORTO, 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}
