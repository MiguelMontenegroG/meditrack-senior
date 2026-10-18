package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort.Especialidad;
import co.edu.uniquindio.meditrack.cita.domain.Cita;

/**
 * Traduce del modelo de dominio de citas a los DTOs de salida.
 */
final class CitaWebMapper {

    private CitaWebMapper() {
        // Utilidad: no se instancia.
    }

    static CitaResponse aResponse(Cita cita, String especialidadNombre) {
        return new CitaResponse(
                cita.getId(),
                cita.getPacienteId(),
                cita.getEspecialidadId(),
                especialidadNombre,
                cita.getRegistradaPor(),
                cita.getProfesional(),
                cita.getFechaHora(),
                cita.getLugar(),
                cita.getMotivo(),
                cita.getEstado());
    }

    static EspecialidadResponse aResponse(Especialidad especialidad) {
        return new EspecialidadResponse(especialidad.id(), especialidad.nombre());
    }
}
