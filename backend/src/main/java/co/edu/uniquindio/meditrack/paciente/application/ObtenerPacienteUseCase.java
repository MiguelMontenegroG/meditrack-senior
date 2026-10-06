package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: obtener el detalle de un paciente aplicando el alcance del usuario.
 *
 * <p>Si el paciente no existe o esta fuera del alcance del usuario se responde
 * 404 (nunca 403) para no revelar su existencia, y el intento se audita como
 * DENEGADO. Toda lectura se audita como READ.</p>
 */
public class ObtenerPacienteUseCase {

    private final PacienteRepositoryPort pacientes;
    private final AuditoriaPort auditoria;

    public ObtenerPacienteUseCase(PacienteRepositoryPort pacientes, AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.auditoria = auditoria;
    }

    public Paciente ejecutar(Long pacienteId, AlcancePaciente alcance) {
        boolean existe = pacientes.buscarPorId(pacienteId).isPresent();
        boolean visible = existe && pacientes.esVisiblePara(pacienteId, alcance.rol(), alcance.usuarioId());

        if (!visible) {
            auditoria.registrar(
                    alcance.usuarioId(),
                    AccionAuditoria.READ,
                    "paciente",
                    pacienteId,
                    ResultadoAuditoria.DENEGADO,
                    "Lectura de paciente fuera de alcance o inexistente");
            throw new RecursoNoEncontradoException("Paciente no encontrado");
        }

        Paciente paciente = pacientes.buscarPorId(pacienteId).orElseThrow(
                () -> new RecursoNoEncontradoException("Paciente no encontrado"));

        auditoria.registrar(
                alcance.usuarioId(),
                AccionAuditoria.READ,
                "paciente",
                pacienteId,
                ResultadoAuditoria.EXITOSO,
                "Lectura de paciente");

        return paciente;
    }
}
