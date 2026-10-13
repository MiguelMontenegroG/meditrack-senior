package co.edu.uniquindio.meditrack.medicacion.domain;

/**
 * Estado de una toma registrada. Coincide con la restriccion CHECK de
 * {@code registro_toma.estado} en la migracion V2.
 *
 * <p>Lo "pendiente" y "atrasado" NO se guarda: se calcula a partir de los
 * horarios y las tomas registradas (ver {@link CalendarioTomas}).</p>
 */
public enum EstadoToma {
    ADMINISTRADA,
    OMITIDA
}
