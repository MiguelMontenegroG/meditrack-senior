package co.edu.uniquindio.meditrack.paciente.domain;

/**
 * Alcance de visibilidad de un usuario sobre los pacientes (regla de seguridad
 * aplicada en el backend).
 *
 * <ul>
 *   <li>{@code ADMINISTRADOR}: ve y gestiona todos los pacientes.</li>
 *   <li>{@code CUIDADOR_ENFERMERO}: solo lectura de los pacientes con asignacion
 *   vigente; no crea ni edita perfiles.</li>
 *   <li>{@code FAMILIAR_AUTORIZADO}: solo lectura de los pacientes con
 *   vinculacion autorizada; nunca ve acciones de escritura.</li>
 * </ul>
 *
 * <p>Este value object concentra la regla para que el filtrado se haga en la
 * consulta o en el caso de uso, nunca ocultando campos despues de traerlos.</p>
 */
public record AlcancePaciente(Long usuarioId, RolUsuario rol) {

    public static AlcancePaciente de(Long usuarioId, RolUsuario rol) {
        return new AlcancePaciente(usuarioId, rol);
    }

    /**
     * Indica si el usuario es administrador y, por tanto, ve todo.
     */
    public boolean esAdministrador() {
        return rol == RolUsuario.ADMINISTRADOR;
    }

    /**
     * Indica si el usuario solo tiene permiso de lectura sobre su alcance.
     */
    public boolean soloLectura() {
        return rol == RolUsuario.CUIDADOR_ENFERMERO || rol == RolUsuario.FAMILIAR_AUTORIZADO;
    }

    /**
     * Indica si el usuario puede ver tambien pacientes inactivos.
     * Solo el administrador puede ver inactivos.
     */
    public boolean puedeVerInactivos() {
        return esAdministrador();
    }
}
