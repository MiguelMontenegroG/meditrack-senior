package co.edu.uniquindio.meditrack.reporte.domain;

import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;

/**
 * Alcance de visibilidad de un usuario dentro del modulo de reportes (regla de
 * seguridad aplicada en el backend).
 *
 * <p>La generacion de un reporte es una <b>lectura agregada</b> de los datos del
 * residente: no crea ni modifica datos clinicos. Por eso los tres roles pueden
 * generarlo, siempre que el residente este dentro de su alcance, y el autor del
 * reporte (auditoria) sea el usuario autenticado. No existe un rol de solo lectura
 * del reporte: el familiar autorizado puede generarlo para el mismo (si tiene un
 * PDF ya descargado, es su copia).</p>
 *
 * <ul>
 *   <li>{@code ADMINISTRADOR}: genera reportes de cualquier residente existente.</li>
 *   <li>{@code CUIDADOR_ENFERMERO}: genera reportes de residentes con asignacion
 *   vigente.</li>
 *   <li>{@code FAMILIAR_AUTORIZADO}: genera reportes de residentes con vinculacion
 *   autorizada.</li>
 * </ul>
 *
 * <p>Este alcance no se confunde con el de escritura: no hay operacion de
 * escritura en el modulo (los reportes son un derivado de los datos).</p>
 */
public record AlcanceReporte(Long usuarioId, RolReporte rol) {

    public static AlcanceReporte de(Long usuarioId, RolReporte rol) {
        return new AlcanceReporte(usuarioId, rol);
    }

    public boolean esAdministrador() {
        return rol == RolReporte.ADMINISTRADOR;
    }

    /**
     * Antes de generar, el caso de uso verifica el alcance del residente con
     * {@code VerificadorAlcanceResidente}. Este metodo documenta que ningun rol
     * queda excluido por si mismo; la decision depende del residente.
     */
    public void exigirPuedeGenerar() {
        if (rol == null) {
            throw new AccesoDenegadoException("El usuario no tiene un rol conocido");
        }
    }
}
