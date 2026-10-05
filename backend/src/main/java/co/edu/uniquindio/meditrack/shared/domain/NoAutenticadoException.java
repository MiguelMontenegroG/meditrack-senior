package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Peticion sin autenticacion valida. Se traduce a HTTP 401.
 */
public class NoAutenticadoException extends DomainException {

    private static final String CODIGO = "NO_AUTENTICADO";

    public NoAutenticadoException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
