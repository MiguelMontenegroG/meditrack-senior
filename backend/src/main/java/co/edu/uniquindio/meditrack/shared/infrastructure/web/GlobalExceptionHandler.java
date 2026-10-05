package co.edu.uniquindio.meditrack.shared.infrastructure.web;

import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.CredencialesInvalidasException;
import co.edu.uniquindio.meditrack.shared.domain.DomainException;
import co.edu.uniquindio.meditrack.shared.domain.NoAutenticadoException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejador global de excepciones.
 *
 * <p>Traduce las excepciones a un unico formato de error {@link ApiError}:
 * cuerpo no legible (400), validacion (400), no autenticado (401), sin permiso (403),
 * no encontrado (404),
 * conflicto de negocio (409) y error interno (500, sin stack traces ni datos internos).</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Cuerpo de la peticion no legible (JSON malformado, tipo incorrecto, etc.). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarCuerpoNoLegible(HttpMessageNotReadableException ex) {
        log.debug("Cuerpo de peticion no legible: {}", ex.getClass().getSimpleName());
        return ResponseEntity.badRequest()
                .body(ApiError.de(400, "CUERPO_INVALIDO", "El cuerpo de la peticion no es valido"));
    }

    /** Errores de validacion de los DTOs de entrada (jakarta.validation). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::describirError)
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.de(400, "VALIDACION", "La peticion no es valida", detalles));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.de(401, ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<ApiError> manejarNoAutenticado(NoAutenticadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.de(401, ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> manejarAccesoDenegado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.de(403, "ACCESO_DENEGADO", "No tiene permisos para realizar esta operacion"));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.de(404, ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(ConflictoDeNegocioException.class)
    public ResponseEntity<ApiError> manejarConflicto(ConflictoDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.de(409, ex.getCodigo(), ex.getMessage()));
    }

    /** Cualquier otra regla de dominio no listada arriba se trata como conflicto. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> manejarDominio(DomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.de(409, ex.getCodigo(), ex.getMessage()));
    }

    /** Error interno: se registra el detalle en el log, nunca en la respuesta. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorInterno(Exception ex) {
        log.error("Error interno no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.de(500, "ERROR_INTERNO", "Ocurrio un error interno"));
    }

    private String describirError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
