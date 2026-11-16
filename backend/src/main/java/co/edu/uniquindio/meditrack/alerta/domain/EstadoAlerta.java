package co.edu.uniquindio.meditrack.alerta.domain;

/**
 * Estado de una alerta. Coincide con el {@code CHECK chk_alerta_estado} de la
 * tabla {@code alerta} (migracion V2).
 *
 * <p>El ciclo de vida del producto es ACTIVA -> ATENDIDA, donde ATENDIDA se
 * persiste con el valor {@code REVISADA} y las columnas {@code revisada_en} y
 * {@code revisada_por} de V2 (el esquema no tiene columnas de "atencion" ni
 * estado ATENDIDA; ver docs/seguridad.md, seccion 17). {@code DESCARTADA} existe
 * en el esquema pero no lo produce este modulo.</p>
 */
public enum EstadoAlerta {
    ACTIVA,
    REVISADA,
    DESCARTADA
}
