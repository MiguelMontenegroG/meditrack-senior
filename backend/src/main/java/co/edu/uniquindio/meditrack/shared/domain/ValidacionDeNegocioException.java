package co.edu.uniquindio.meditrack.shared.domain;

/**
 * Violación de una regla de formato o coherencia de los datos de entrada
 * (por ejemplo, una frecuencia de medicación incoherente con sus horarios, o
 * una toma OMITIDA sin observaciones). Se traduce a HTTP 400 con el código
 * {@code VALIDACION}.
 *
 * <p>Se distingue de {@link ConflictoDeNegocioException} (409), reservada a
 * conflictos de estado o de unicidad (duplicados, solapamientos, estado
 * incorrecto). Un dato mal formado es un problema de la petición, no del
 * estado del recurso.</p>
 */
public class ValidacionDeNegocioException extends DomainException {

    private static final String CODIGO = "VALIDACION";

    public ValidacionDeNegocioException(String mensaje) {
        super(CODIGO, mensaje);
    }
}
