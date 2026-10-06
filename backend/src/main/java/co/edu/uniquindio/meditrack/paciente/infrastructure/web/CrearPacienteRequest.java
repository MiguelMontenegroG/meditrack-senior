package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Cuerpo de la peticion para crear o actualizar un paciente (solo administrador).
 */
public record CrearPacienteRequest(
        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 30, message = "El documento no debe superar 30 caracteres")
        String documento,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben superar 100 caracteres")
        String apellidos,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,

        @NotNull(message = "El sexo es obligatorio")
        Sexo sexo,

        @Size(max = 20, message = "La habitacion no debe superar 20 caracteres")
        String habitacion,

        String situacionClinica,

        LocalDate fechaIngreso) {
}
