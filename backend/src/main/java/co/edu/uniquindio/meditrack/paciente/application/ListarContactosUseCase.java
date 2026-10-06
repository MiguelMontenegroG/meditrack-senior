package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;

import java.util.List;

/**
 * Caso de uso: listar los contactos de emergencia de un paciente respetando el
 * alcance del usuario (si el paciente no es visible, 404 y auditoria DENEGADO).
 */
public class ListarContactosUseCase {

    private final PacienteRepositoryPort pacientes;
    private final ContactoEmergenciaRepositoryPort contactos;
    private final AuditoriaPort auditoria;

    public ListarContactosUseCase(
            PacienteRepositoryPort pacientes,
            ContactoEmergenciaRepositoryPort contactos,
            AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.contactos = contactos;
        this.auditoria = auditoria;
    }

    public List<ContactoEmergencia> ejecutar(Long pacienteId, AlcancePaciente alcance) {
        boolean visible = pacientes.buscarPorId(pacienteId).isPresent()
                && pacientes.esVisiblePara(pacienteId, alcance.rol(), alcance.usuarioId());
        if (!visible) {
            auditoria.registrar(alcance.usuarioId(), AccionAuditoria.READ, "contacto_emergencia",
                    pacienteId, ResultadoAuditoria.DENEGADO,
                    "Lectura de contactos de paciente fuera de alcance o inexistente");
            throw new RecursoNoEncontradoException("Paciente no encontrado");
        }
        auditoria.registrar(alcance.usuarioId(), AccionAuditoria.READ, "contacto_emergencia",
                pacienteId, ResultadoAuditoria.EXITOSO, "Lectura de contactos de paciente");
        return contactos.listarPorPaciente(pacienteId);
    }
}
