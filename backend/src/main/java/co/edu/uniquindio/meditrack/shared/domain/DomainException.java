package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Excepcion base de las reglas de negocio del dominio.
 *
 * <p>Toda violacion de una regla de negocio debe expresarse con una subclase
 * de esta excepcion, para que el manejador global de errores la traduzca a
 * una respuesta HTTP coherente (por ejemplo 409 Conflicto).</p>
 */
public class DomainException extends RuntimeException {

    private final String codigo;

    public DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
