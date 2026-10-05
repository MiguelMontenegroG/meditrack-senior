package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.application.port.TokenJwt;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del filtro JWT.
 *
 * Cubren: token valido autentica; token invalido no autentica; peticion sin
 * encabezado no autentica. En todos los casos la cadena de filtros continua.
 */
class JwtAuthenticationFilterTest {

    private final UsuarioDetailsService usuarioDetailsService = mock(UsuarioDetailsService.class);
    private final FilterChain cadena = mock(FilterChain.class);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private JwtAuthenticationFilter filtroCon(JwtPort jwt) {
        return new JwtAuthenticationFilter(jwt, usuarioDetailsService);
    }

    @Test
    void tokenValidoEstableceAutenticacion() throws Exception {
        JwtPort jwt = mock(JwtPort.class);
        when(jwt.validarToken(anyString())).thenReturn(new TokenJwt(5L, RolNombre.ADMINISTRADOR));
        when(usuarioDetailsService.loadUserByUsername("5")).thenReturn(
                org.springframework.security.core.userdetails.User
                        .withUsername("5")
                        .password("x")
                        .authorities("ROLE_ADMINISTRADOR")
                        .build());

        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader("Authorization", "Bearer token.valido");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtroCon(jwt).doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("5");
    }

    @Test
    void tokenInvalidoNoAutentica() throws Exception {
        JwtPort jwt = mock(JwtPort.class);
        when(jwt.validarToken(anyString()))
                .thenThrow(new io.jsonwebtoken.JwtException("invalido"));

        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader("Authorization", "Bearer token.invalido");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtroCon(jwt).doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void sinEncabezadoNoAutentica() throws Exception {
        JwtPort jwt = mock(JwtPort.class);

        MockHttpServletRequest peticion = new MockHttpServletRequest();
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtroCon(jwt).doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void encabezadoSinPrefijoBearerNoAutentica() throws Exception {
        JwtPort jwt = mock(JwtPort.class);

        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.addHeader("Authorization", "token-sin-prefijo");
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtroCon(jwt).doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
