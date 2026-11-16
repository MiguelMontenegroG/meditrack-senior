package co.edu.uniquindio.meditrack.reporte.infrastructure.web;

import co.edu.uniquindio.meditrack.reporte.domain.AlcanceReporte;
import co.edu.uniquindio.meditrack.reporte.domain.RolReporte;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Construye el {@link AlcanceReporte} a partir de la autenticacion de Spring
 * Security (id = claim sub; rol = authority ROLE_*). El id del usuario
 * autenticado es el que se guarda como autor del reporte (generadoPor).
 */
final class AlcanceReporteWeb {

    private AlcanceReporteWeb() {
        // Utilidad: no se instancia.
    }

    static AlcanceReporte de(Authentication autenticacion) {
        return AlcanceReporte.de(Long.valueOf(autenticacion.getName()), rol(autenticacion));
    }

    static Long usuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }

    private static RolReporte rol(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(RolReporte::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un rol conocido"));
    }
}