package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion para activar o desactivar un paciente.
 *
 * <p>Al desactivar, la aplicacion exige un {@code motivo} (dato de auditoria de
 * la Ley 1581); al activar el motivo se ignora.</p>
 */
public record CambiarEstadoPacienteRequest(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo,

        @Size(max = 200, message = "El motivo no debe superar 200 caracteres")
        String motivo) {
}
