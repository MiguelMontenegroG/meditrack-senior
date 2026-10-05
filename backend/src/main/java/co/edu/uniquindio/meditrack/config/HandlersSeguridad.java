package co.edu.uniquindio.meditrack.config;

import co.edu.uniquindio.meditrack.shared.infrastructure.web.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Respuestas JSON para los errores de seguridad.
 *
 * <ul>
 *   <li>{@link AutenticacionEntryPoint}: 401 cuando falta un JWT valido.</li>
 *   <li>{@link AccesoDenegadoHandler}: 403 cuando el usuario autenticado no tiene permisos.</li>
 * </ul>
 *
 * <p>Ambas usan el mismo formato de error que el resto de la API.</p>
 */
public class HandlersSeguridad {

    private HandlersSeguridad() {
        // Utilidad: no se instancia.
    }

    @Component
    public static class AutenticacionEntryPoint implements AuthenticationEntryPoint {

        private final ErrorResponseWriter writer;

        public AutenticacionEntryPoint(ErrorResponseWriter writer) {
            this.writer = writer;
        }

        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) throws IOException {
            writer.escribir(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "NO_AUTENTICADO", "Se requiere autenticacion para acceder a este recurso");
        }
    }

    @Component
    public static class AccesoDenegadoHandler implements AccessDeniedHandler {

        private final ErrorResponseWriter writer;

        public AccesoDenegadoHandler(ErrorResponseWriter writer) {
            this.writer = writer;
        }

        @Override
        public void handle(
                HttpServletRequest request,
                HttpServletResponse response,
                AccessDeniedException accessDeniedException) throws IOException {
            writer.escribir(response, HttpServletResponse.SC_FORBIDDEN,
                    "ACCESO_DENEGADO", "No tiene permisos para realizar esta operacion");
        }
    }
}
