package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion para vincular a un familiar con un paciente.
 */
public record FamiliarRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId,

        @Size(max = 60, message = "El parentesco no debe superar 60 caracteres")
        String parentesco) {
}
