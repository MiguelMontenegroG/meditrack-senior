package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.application.port.TokenJwt;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Implementacion de {@link JwtPort}: genera y valida tokens JWT firmados con HS256.
 *
 * <p>El token incluye unicamente: sub (id de usuario), rol, iat y exp. No se
 * incluyen correo ni otros datos sensibles. El secreto se lee de {@code JWT_SECRET}
 * y la expiracion de {@code JWT_EXPIRATION_MINUTES}.</p>
 *
 * <p>Si el secreto tiene menos de 32 bytes, el arranque falla a proposito: HS256
 * exige una clave de al menos 256 bits.</p>
 */
@Component
public class JwtService implements JwtPort {

    private static final int BYTES_MINIMOS_SECRETO = 32;
    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(
            @Value("${JWT_SECRET}") String secreto,
            @Value("${JWT_EXPIRATION_MINUTES}") long expiracionMinutos) {
        byte[] bytes = secreto == null ? new byte[0] : secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < BYTES_MINIMOS_SECRETO) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos " + BYTES_MINIMOS_SECRETO
                            + " bytes (256 bits) para firmar con HS256");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMinutos = expiracionMinutos;
    }

    @Override
    public String generarToken(Long usuarioId, RolNombre rol) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plusSeconds(expiracionEnSegundos());
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
    }

    @Override
    public TokenJwt validarToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long usuarioId = Long.valueOf(claims.getSubject());
        RolNombre rol = RolNombre.valueOf(claims.get(CLAIM_ROL, String.class));
        return new TokenJwt(usuarioId, rol);
    }

    @Override
    public long expiracionEnSegundos() {
        return expiracionMinutos * 60L;
    }
}
