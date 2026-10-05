package co.edu.uniquindio.meditrack.shared.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Formato unico de error de la API.
 *
 * <pre>
 * {
 *   "timestamp": "2025-01-01T00:00:00Z",
 *   "status": 400,
 *   "codigo": "VALIDACION",
 *   "mensaje": "La peticion no es valida",
 *   "detalles": ["correo: no puede estar vacio"]
 * }
 * </pre>
 *
 * <p>El campo {@code detalles} solo se incluye cuando hay informacion adicional
 * (por ejemplo, errores de validacion). Nunca expone stack traces ni datos internos.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String codigo,
        String mensaje,
        List<String> detalles) {

    public static ApiError de(int status, String codigo, String mensaje) {
        return new ApiError(Instant.now(), status, codigo, mensaje, null);
    }

    public static ApiError de(int status, String codigo, String mensaje, List<String> detalles) {
        return new ApiError(Instant.now(), status, codigo, mensaje, detalles);
    }
}
