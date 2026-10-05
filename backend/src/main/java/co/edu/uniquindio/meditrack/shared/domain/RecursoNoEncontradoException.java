package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Recurso solicitado que no existe. Se traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends DomainException {

    private static final String CODIGO = "NO_ENCONTRADO";

    public RecursoNoEncontradoException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
