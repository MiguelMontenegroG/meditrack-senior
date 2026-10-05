package co.edu.uniquindio.meditrack.shared.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Escribe respuestas de error en JSON con el mismo formato de {@link ApiError}.
 * Se usa en los puntos de entrada (401) y en el manejador de acceso denegado (403),
 * donde no interviene el {@code @RestControllerAdvice}.
 */
@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void escribir(HttpServletResponse respuesta, int status, String codigo, String mensaje)
            throws IOException {
        respuesta.setStatus(status);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(respuesta.getWriter(), ApiError.de(status, codigo, mensaje));
    }
}
