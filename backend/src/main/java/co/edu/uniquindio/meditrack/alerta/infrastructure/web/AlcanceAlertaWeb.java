package co.edu.uniquindio.meditrack.alerta.infrastructure.web;

import co.edu.uniquindio.meditrack.alerta.domain.AlcanceAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.RolAlerta;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Construye el {@link AlcanceAlerta} a partir de la autenticacion de Spring
 * Security (id = claim {@code sub}; rol = authority {@code ROLE_*}).
 */
final class AlcanceAlertaWeb {

    private AlcanceAlertaWeb() {
        // Utilidad: no se instancia.
    }

    static AlcanceAlerta de(Authentication autenticacion) {
        return AlcanceAlerta.de(Long.valueOf(autenticacion.getName()), rol(autenticacion));
    }

    private static RolAlerta rol(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(RolAlerta::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un rol conocido"));
    }
}
