package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import java.time.LocalDate;
import java.util.List;

/**
 * Detalle completo de un paciente, con edad calculada y contactos.
 */
public record PacienteDetalleResponse(
        Long id,
        String documento,
        String nombres,
        String apellidos,
        String nombreCompleto,
        LocalDate fechaNacimiento,
        int edad,
        String sexo,
        String habitacion,
        String situacionClinica,
        LocalDate fechaIngreso,
        boolean activo,
        List<ContactoEmergenciaResponse> contactos) {
}
