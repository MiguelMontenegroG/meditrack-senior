package co.edu.uniquindio.meditrack.reporte.domain;

/**
 * Rol del usuario dentro del modulo de reportes. Coincide con los roles del
 * sistema ({@code rol.nombre} de la migracion V1/V3); se declara aqui para no
 * acoplar el dominio de reportes a los enums de otros modulos.
 */
public enum RolReporte {
    ADMINISTRADOR,
    CUIDADOR_ENFERMERO,
    FAMILIAR_AUTORIZADO
}
