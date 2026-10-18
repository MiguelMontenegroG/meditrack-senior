package co.edu.uniquindio.meditrack.cita.application;

import java.time.Instant;

/**
 * Datos editables de una cita para agendar o editar. El autor
 * ({@code registradaPor}) y el estado no forman parte del comando: el autor sale
 * del usuario autenticado y el estado lo gestiona el dominio.
 */
public record ComandoCita(
        Long especialidadId,
        String profesional,
        Instant fechaHora,
        String lugar,
        String motivo) {
}
