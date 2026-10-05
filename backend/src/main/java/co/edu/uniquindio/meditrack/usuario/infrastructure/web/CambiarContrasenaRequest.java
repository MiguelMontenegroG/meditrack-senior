package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion para que un usuario cambie su propia contrasena.
 */
public record CambiarContrasenaRequest(
        @NotBlank(message = "La contrasena actual es obligatoria")
        String contrasenaActual,

        @NotBlank(message = "La contrasena nueva es obligatoria")
        @Size(min = 8, message = "La contrasena nueva debe tener al menos 8 caracteres")
        String contrasenaNueva) {
}
