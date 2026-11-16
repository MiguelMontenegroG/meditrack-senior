package co.edu.uniquindio.meditrack.alerta.domain;

/**
 * Alcance de un usuario sobre las alertas (regla de seguridad aplicada en el
 * backend).
 *
 * <ul>
 *   <li>{@code ADMINISTRADOR}: lee y atiende alertas de cualquier residente.</li>
 *   <li>{@code CUIDADOR_ENFERMERO}: lee y atiende alertas solo de residentes con
 *   asignacion vigente.</li>
 *   <li>{@code FAMILIAR_AUTORIZADO}: solo lectura de residentes con vinculacion
 *   autorizada; nunca atiende.</li>
 * </ul>
 */
public record AlcanceAlerta(Long usuarioId, RolAlerta rol) {

    public static AlcanceAlerta de(Long usuarioId, RolAlerta rol) {
        return new AlcanceAlerta(usuarioId, rol);
    }

    public boolean esAdministrador() {
        return rol == RolAlerta.ADMINISTRADOR;
    }

    /** Administrador y cuidador atienden alertas; el familiar nunca. */
    public boolean puedeAtender() {
        return rol == RolAlerta.ADMINISTRADOR || rol == RolAlerta.CUIDADOR_ENFERMERO;
    }

    /** El familiar nunca escribe. */
    public boolean soloLectura() {
        return rol == RolAlerta.FAMILIAR_AUTORIZADO;
    }
}
