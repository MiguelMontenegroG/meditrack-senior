package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.domain.Sexo;

import java.time.LocalDate;

/**
 * Datos de entrada para crear o actualizar un paciente.
 */
public record ComandoCrearPaciente(
        String documento,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        Sexo sexo,
        String habitacion,
        String situacionClinica,
        LocalDate fechaIngreso) {
}
