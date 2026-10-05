package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.application.port.TokenJwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT: lee el encabezado Authorization (Bearer), valida el token y
 * establece la autenticacion en el contexto de Spring Security.
 *
 * <p>Si el token falta, es invalido o corresponde a un usuario inexistente o
 * desactivado, no se autentica: la peticion continuara y el
 * {@link HandlersSeguridad.AutenticacionEntryPoint} devolvera 401 si la ruta lo exige.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String PREFIJO = "Bearer ";

    private final JwtPort jwt;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtPort jwt, UsuarioDetailsService usuarioDetailsService) {
        this.jwt = jwt;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = encabezado.substring(PREFIJO.length()).trim();
            try {
                TokenJwt datos = jwt.validarToken(token);
                UserDetails detalles = usuarioDetailsService.loadUserByUsername(String.valueOf(datos.usuarioId()));
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(detalles, null, detalles.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                // Token invalido/expirado o usuario inactivo: se ignora y se limpia el contexto.
                SecurityContextHolder.clearContext();
                log.debug("Token JWT rechazado: {}", ex.getClass().getSimpleName());
            }
        }

        filterChain.doFilter(request, response);
    }
}
