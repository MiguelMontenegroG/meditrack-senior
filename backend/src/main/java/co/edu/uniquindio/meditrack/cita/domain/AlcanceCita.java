package co.edu.uniquindio.meditrack.cita.domain;

/**
 * Alcance de visibilidad de un usuario sobre una cita dentro del modulo de citas
 * (regla de seguridad aplicada en el backend).
 *
 * <ul>
 *   <li>{@code ADMINISTRADOR}: agenda, edita, cancela y marca estado de
 *   cualquier cita; ve todo el calendario.</li>
 *   <li>{@code CUIDADOR_ENFERMERO}: lee y marca CUMPLIDA/INCUMPLIDA solo citas
 *   de residentes con asignacion vigente; no agenda ni edita.</li>
 *   <li>{@code FAMILIAR_AUTORIZADO}: solo lectura de citas de residentes
 *   vinculados y autorizados; nunca escribe ni marca estado.</li>
 * </ul>
 */
public record AlcanceCita(Long usuarioId, RolCita rol) {

    public static AlcanceCita de(Long usuarioId, RolCita rol) {
        return new AlcanceCita(usuarioId, rol);
    }

    public boolean esAdministrador() {
        return rol == RolCita.ADMINISTRADOR;
    }

    /** Solo el administrador agenda o edita citas. */
    public boolean puedeAgendar() {
        return rol == RolCita.ADMINISTRADOR;
    }

    /** Administrador y cuidador pueden marcar estado; el familiar nunca. */
    public boolean puedeMarcarEstado() {
        return rol == RolCita.ADMINISTRADOR || rol == RolCita.CUIDADOR_ENFERMERO;
    }

    /** El familiar nunca escribe. */
    public boolean soloLectura() {
        return rol == RolCita.FAMILIAR_AUTORIZADO;
    }
}
