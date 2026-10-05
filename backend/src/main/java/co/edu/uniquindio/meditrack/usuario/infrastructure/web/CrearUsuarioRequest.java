package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;

/**
 * Cuerpo de la peticion para crear un usuario (solo administrador).
 */
public record CrearUsuarioRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre completo no debe superar 150 caracteres")
        String nombreCompleto,

        @Size(max = 30, message = "El telefono no debe superar 30 caracteres")
        String telefono,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String contrasena,

        @NotNull(message = "El rol es obligatorio")
        RolNombre rol) {
}
