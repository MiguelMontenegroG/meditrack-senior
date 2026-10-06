package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion para crear o actualizar un contacto de emergencia.
 */
public record ContactoRequest(
        @NotBlank(message = "El nombre del contacto es obligatorio")
        @Size(max = 150, message = "El nombre no debe superar 150 caracteres")
        String nombre,

        @Size(max = 60, message = "El parentesco no debe superar 60 caracteres")
        String parentesco,

        @NotBlank(message = "El telefono del contacto es obligatorio")
        @Size(max = 30, message = "El telefono no debe superar 30 caracteres")
        String telefono,

        boolean principal) {
}
