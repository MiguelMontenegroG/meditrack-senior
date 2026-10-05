package co.edu.uniquindio.meditrack.usuario.domain;

/**
 * Roles del sistema. El nombre coincide exactamente con el valor almacenado
 * en la tabla {@code rol} (migracion V3) y con la restriccion CHECK de V2.
 *
 * <p>Se exponen como authorities de Spring Security con el prefijo ROLE_
 * (por ejemplo ROLE_ADMINISTRADOR).</p>
 */
public enum RolNombre {
    ADMINISTRADOR,
    CUIDADOR_ENFERMERO,
    FAMILIAR_AUTORIZADO;

    /**
     * Nombre de la authority de Spring Security para este rol.
     */
    public String authority() {
        return "ROLE_" + name();
    }
}
