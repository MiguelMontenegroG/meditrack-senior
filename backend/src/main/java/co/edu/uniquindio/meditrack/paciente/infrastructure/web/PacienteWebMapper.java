package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.domain.AsignacionCuidador;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.VinculacionFamiliar;

import java.time.LocalDate;
import java.util.List;

/**
 * Traduce el modelo de dominio de paciente a DTOs de respuesta.
 *
 * <p>Es el unico punto donde el modelo se convierte en respuesta web. El
 * resumen del listado no incluye la situacion clinica.</p>
 */
public final class PacienteWebMapper {

    private PacienteWebMapper() {
        // Utilidad de mapeo: no se instancia.
    }

    public static PacienteResumenResponse aResumen(Paciente paciente, LocalDate hoy) {
        return new PacienteResumenResponse(
                paciente.getId(),
                paciente.nombreCompleto(),
                paciente.edad(hoy),
                paciente.getHabitacion(),
                paciente.isActivo());
    }

    public static PacienteDetalleResponse aDetalle(Paciente paciente, List<ContactoEmergencia> contactos, LocalDate hoy) {
        return new PacienteDetalleResponse(
                paciente.getId(),
                paciente.getDocumento(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.nombreCompleto(),
                paciente.getFechaNacimiento(),
                paciente.edad(hoy),
                paciente.getSexo() != null ? paciente.getSexo().name() : null,
                paciente.getHabitacion(),
                paciente.getSituacionClinica(),
                paciente.getFechaIngreso(),
                paciente.isActivo(),
                contactos.stream().map(PacienteWebMapper::aContacto).toList());
    }

    public static ContactoEmergenciaResponse aContacto(ContactoEmergencia contacto) {
        return new ContactoEmergenciaResponse(
                contacto.getId(),
                contacto.getNombre(),
                contacto.getParentesco(),
                contacto.getTelefono(),
                contacto.isPrincipal());
    }

    public static VinculacionFamiliarResponse aVinculacion(VinculacionFamiliar vinculacion) {
        return new VinculacionFamiliarResponse(
                vinculacion.getId(),
                vinculacion.getUsuarioId(),
                vinculacion.getPacienteId(),
                vinculacion.getParentesco(),
                vinculacion.isAutorizado());
    }

    public static AsignacionCuidadorResponse aAsignacion(AsignacionCuidador asignacion) {
        return new AsignacionCuidadorResponse(
                asignacion.getId(),
                asignacion.getUsuarioId(),
                asignacion.getPacienteId(),
                asignacion.getDesde(),
                asignacion.getHasta());
    }
}
