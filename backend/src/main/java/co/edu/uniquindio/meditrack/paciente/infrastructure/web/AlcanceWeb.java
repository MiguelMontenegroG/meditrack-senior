package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.RolUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Construye el alcance de visibilidad de paciente a partir de la autenticacion
 * de Spring Security.
 *
 * <p>El id del usuario es el {@code name} de la autenticacion (claim {@code sub}
 * del JWT) y el rol se toma de la authority {@code ROLE_*}.</p>
 */
final class AlcanceWeb {

    private AlcanceWeb() {
        // Utilidad: no se instancia.
    }

    static AlcancePaciente de(Authentication autenticacion) {
        return new AlcancePaciente(usuarioId(autenticacion), rol(autenticacion));
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
