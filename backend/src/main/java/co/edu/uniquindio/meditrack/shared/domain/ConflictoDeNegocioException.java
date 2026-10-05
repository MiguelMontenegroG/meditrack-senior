package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Conflicto con una regla de negocio (por ejemplo, correo duplicado o intento
 * de dejar el sistema sin administradores activos). Se traduce a HTTP 409.
 */
public class ConflictoDeNegocioException extends DomainException {

    private static final String CODIGO = "CONFLICTO";

    public ConflictoDeNegocioException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
