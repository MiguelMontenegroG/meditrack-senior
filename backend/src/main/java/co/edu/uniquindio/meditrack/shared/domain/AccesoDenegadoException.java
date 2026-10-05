package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Peticion autenticada pero sin permisos suficientes. Se traduce a HTTP 403.
 */
public class AccesoDenegadoException extends DomainException {

    private static final String CODIGO = "ACCESO_DENEGADO";

    public AccesoDenegadoException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
