package co.edu.uniquindio.meditrack.bitacora.domain;

/**
 * Alcance de un usuario sobre la bitacora de signos vitales de un residente
 * (regla de seguridad aplicada en el backend).
 *
 * <ul>
 *   <li>{@code ADMINISTRADOR}: registra y lee signos vitales de cualquier
 *   residente existente.</li>
 *   <li>{@code CUIDADOR_ENFERMERO}: registra y lee signos vitales solo de
 *   residentes con asignacion vigente; no puede registrar en inactivos.</li>
 *   <li>{@code FAMILIAR_AUTORIZADO}: solo lectura de residentes con vinculacion
 *   autorizada; nunca registra.</li>
 * </ul>
 */
public record AlcanceBitacora(Long usuarioId, RolBitacora rol) {

    public static AlcanceBitacora de(Long usuarioId, RolBitacora rol) {
        return new AlcanceBitacora(usuarioId, rol);
    }

    public boolean esAdministrador() {
        return rol == RolBitacora.ADMINISTRADOR;
    }

    /** Administrador y cuidador pueden registrar signos vitales; el familiar nunca. */
    public boolean puedeRegistrar() {
        return rol == RolBitacora.ADMINISTRADOR || rol == RolBitacora.CUIDADOR_ENFERMERO;
    }

    /** El familiar nunca escribe. */
    public boolean soloLectura() {
        return rol == RolBitacora.FAMILIAR_AUTORIZADO;
    }
}
