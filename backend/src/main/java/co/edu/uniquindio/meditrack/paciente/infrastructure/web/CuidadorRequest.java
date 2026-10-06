package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de la peticion para asignar un cuidador a un paciente.
 */
public record CuidadorRequest(
        @NotNull(message = "El usuarioId es obligatorio")
        Long usuarioId) {
}
