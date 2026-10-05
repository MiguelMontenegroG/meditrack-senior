package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Credenciales invalidas en el inicio de sesion. El mensaje es siempre
 * generico para no revelar si el correo existe. Se traduce a HTTP 401.
 */
public class CredencialesInvalidasException extends DomainException {

    private static final String CODIGO = "CREDENCIALES_INVALIDAS";

    public CredencialesInvalidasException() {
        super(CODIGO, "Credenciales invalidas");
    }
}
