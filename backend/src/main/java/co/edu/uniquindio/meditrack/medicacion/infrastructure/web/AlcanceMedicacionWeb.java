package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Construye el {@link AlcanceMedicacion} a partir de la autenticacion de Spring
 * Security (id = claim {@code sub}; rol = authority {@code ROLE_*}).
 */
final class AlcanceMedicacionWeb {

    private AlcanceMedicacionWeb() {
        // Utilidad: no se instancia.
    }

    static AlcanceMedicacion de(Authentication autenticacion) {
        return AlcanceMedicacion.de(usuarioId(autenticacion), rol(autenticacion));
    }

    static Long usuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }

    private static RolUsuario rol(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(RolUsuario::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un rol conocido"));
    }
}
