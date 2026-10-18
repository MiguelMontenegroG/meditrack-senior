package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.RolCita;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Construye el {@link AlcanceCita} a partir de la autenticacion de Spring
 * Security (id = claim {@code sub}; rol = authority {@code ROLE_*}).
 */
final class AlcanceCitaWeb {

    private AlcanceCitaWeb() {
        // Utilidad: no se instancia.
    }

    static AlcanceCita de(Authentication autenticacion) {
        return AlcanceCita.de(Long.valueOf(autenticacion.getName()), rol(autenticacion));
    }

    static Long usuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }

    private static RolCita rol(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(RolCita::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un rol conocido"));
    }
}
