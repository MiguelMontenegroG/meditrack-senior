package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Cuerpo de entrada para agendar o editar una cita.
 *
 * <p>El autor ({@code registradaPor}) y el estado no forman parte de la
 * peticion: el autor sale del usuario autenticado y el estado lo gestiona el
 * dominio. {@code especialidadId} es opcional (una cita puede registrarse sin
 * especialidad, por ejemplo una visita general).</p>
 */
public record CitaRequest(
        Long especialidadId,

        @Size(max = 150, message = "El profesional no puede superar 150 caracteres")
        String profesional,

        Instant fechaHora,

        @Size(max = 150, message = "El lugar no puede superar 150 caracteres")
        String lugar,

        String motivo) {
}
