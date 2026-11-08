package co.edu.uniquindio.meditrack.alerta.domain;

/**
 * Tipos de alerta del producto. Los tres primeros los genera el sistema; los dos
 * ultimos existen en el catalogo de V2 pero no los produce la generacion
 * automatica.
 *
 * <p>Coinciden exactamente con el {@code CHECK chk_alerta_tipo} de la tabla
 * {@code alerta} (migracion V2).</p>
 */
public enum TipoAlerta {
    /** Toma de medicacion programada y no registrada dentro de la tolerancia. */
    DOSIS_ATRASADA,
    /** Signos vitales registrados fuera de los umbrales de referencia. */
    SIGNO_FUERA_DE_RANGO,
    /** Cita en estado PROGRAMADA cuya hora ya paso: probable incumplimiento. */
    CITA_PROXIMA,
    /** Reservado por el esquema; la generacion automatica no lo produce. */
    BITACORA_INCOMPLETA,
    /** Reservado por el esquema; la generacion automatica no lo produce. */
    OTRA
}
