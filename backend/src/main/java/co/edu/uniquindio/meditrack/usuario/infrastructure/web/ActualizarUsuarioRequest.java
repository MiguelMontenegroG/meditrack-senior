package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion para actualizar un usuario (solo administrador).
 */
public record ActualizarUsuarioRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre completo no debe superar 150 caracteres")
        String nombreCompleto,

        @Size(max = 30, message = "El telefono no debe superar 30 caracteres")
        String telefono,

        @NotNull(message = "El rol es obligatorio")
        RolNombre rol) {
}
