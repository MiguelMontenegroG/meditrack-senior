package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: activar o desactivar un paciente (solo administrador).
 *
 * <p>Nunca se borra fisicamente: el borrado es logico con {@code activo}.</p>
 */
public class CambiarEstadoPacienteUseCase {

    private final PacienteRepositoryPort pacientes;
    private final AuditoriaPort auditoria;

    public CambiarEstadoPacienteUseCase(PacienteRepositoryPort pacientes, AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.auditoria = auditoria;
    }

    public Paciente ejecutar(Long pacienteId, boolean activar, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        if (activar) {
            paciente.activar();
        } else {
            paciente.desactivar();
        }

        Paciente persistido = pacientes.guardar(paciente);

        auditoria.registrar(
                autorId,
                AccionAuditoria.UPDATE,
                "paciente",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                activar ? "Activacion de paciente" : "Desactivacion de paciente");

        return persistido;
    }
}
