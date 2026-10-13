package co.edu.uniquindio.meditrack.medicacion.domain;

/**
 * Alcance de visibilidad de un usuario sobre un residente dentro del modulo de
 * medicacion (regla de seguridad aplicada en el backend).
 *
 * <ul>
 *   <li>{@code ADMINISTRADOR}: gestiona catalogo, esquemas y horarios; ve todo;
 *   registra tomas.</li>
 *   <li>{@code CUIDADOR_ENFERMERO}: lectura de esquemas y registro de tomas solo
 *   de residentes con asignacion vigente.</li>
 *   <li>{@code FAMILIAR_AUTORIZADO}: solo lectura de esquemas y tomas de
 *   residentes con vinculacion autorizada; nunca escribe.</li>
 * </ul>
 */
public record AlcanceMedicacion(Long usuarioId, RolUsuario rol) {

    public static AlcanceMedicacion de(Long usuarioId, RolUsuario rol) {
        return new AlcanceMedicacion(usuarioId, rol);
    }

    public boolean esAdministrador() {
        return rol == RolUsuario.ADMINISTRADOR;
    }

    /** El cuidador puede registrar tomas (pero no crear/editar esquemas ni catalogo). */
    public boolean puedeRegistrarTomas() {
        return rol == RolUsuario.ADMINISTRADOR || rol == RolUsuario.CUIDADOR_ENFERMERO;
    }

    /** El familiar nunca escribe. */
    public boolean soloLectura() {
        return rol == RolUsuario.FAMILIAR_AUTORIZADO;
    }
}
