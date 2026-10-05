package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

/**
 * Traduce el modelo de dominio a DTOs de respuesta.
 *
 * <p>Es el unico punto donde el modelo se convierte en respuesta web; aqui se
 * garantiza que el hash de la contrasena nunca se exponga.</p>
 */
public final class UsuarioWebMapper {

    private UsuarioWebMapper() {
        // Utilidad de mapeo: no se instancia.
    }

    public static UsuarioAutenticadoResponse aAutenticado(Usuario usuario) {
        return new UsuarioAutenticadoResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo().valor(),
                usuario.getRol().getNombre().name());
    }

    public static UsuarioResponse aResponse(Usuario usuario) {
        // No se incluyen creadoEn/actualizadoEn desde el dominio (no los modela);
        // se rellenan en null y se completan en el controlador cuando aplique.
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo().valor(),
                usuario.getTelefono(),
                usuario.getRol().getNombre().name(),
                usuario.isActivo(),
                usuario.getUltimoAccesoEn(),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn());
    }
}
