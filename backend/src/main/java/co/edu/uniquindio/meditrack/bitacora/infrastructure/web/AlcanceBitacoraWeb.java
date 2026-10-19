package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import co.edu.uniquindio.meditrack.bitacora.domain.AlcanceBitacora;
import co.edu.uniquindio.meditrack.bitacora.domain.RolBitacora;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Construye el {@link AlcanceBitacora} a partir de la autenticacion de Spring
 * Security (id = claim {@code sub}; rol = authority {@code ROLE_*}). El id del
 * usuario autenticado es el que se guarda como autor del registro
 * ({@code registradoPor}).
 */
final class AlcanceBitacoraWeb {

    private AlcanceBitacoraWeb() {
        // Utilidad: no se instancia.
    }

    static AlcanceBitacora de(Authentication autenticacion) {
        return AlcanceBitacora.de(Long.valueOf(autenticacion.getName()), rol(autenticacion));
    }

    static Long usuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }

    private static RolBitacora rol(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(RolBitacora::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un rol conocido"));
    }
}
