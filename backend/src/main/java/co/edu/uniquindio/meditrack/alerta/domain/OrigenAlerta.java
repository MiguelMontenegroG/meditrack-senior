package co.edu.uniquindio.meditrack.alerta.domain;

/**
 * Origen de una alerta. Coincide con el {@code CHECK chk_alerta_origen} de la
 * tabla {@code alerta} (migracion V2): las alertas generadas de forma automatica
 * son {@code SISTEMA}.
 */
public enum OrigenAlerta {
    SISTEMA,
    USUARIO
}
