package co.edu.uniquindio.meditrack.medicacion.domain;

/**
 * Via de administracion de un medicamento. Los valores coinciden exactamente
 * con la restriccion CHECK de {@code esquema_medicacion.via} en la migracion V2.
 */
public enum ViaAdministracion {
    ORAL,
    SUBCUTANEA,
    INTRAMUSCULAR,
    TOPICA,
    OTRA
}
