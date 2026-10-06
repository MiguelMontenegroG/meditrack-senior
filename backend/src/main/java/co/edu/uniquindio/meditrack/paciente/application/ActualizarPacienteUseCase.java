package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Caso de uso: actualizar los datos de un paciente (solo administrador).
 */
public class ActualizarPacienteUseCase {

    private final PacienteRepositoryPort pacientes;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public ActualizarPacienteUseCase(PacienteRepositoryPort pacientes, AuditoriaPort auditoria, Clock reloj) {
        this.pacientes = pacientes;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Paciente ejecutar(Long pacienteId, ComandoCrearPaciente comando, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        if (comando.documento() != null
                && pacientes.existePorDocumentoEnOtro(comando.documento().trim(), pacienteId)) {
            throw new ConflictoDeNegocioException("Ya existe otro paciente con ese documento");
        }

        LocalDate hoy = LocalDate.now(reloj);
        paciente.actualizar(
                comando.documento(),
                comando.nombres(),
                comando.apellidos(),
                comando.fechaNacimiento(),
                comando.sexo(),
                comando.habitacion(),
                comando.situacionClinica(),
                comando.fechaIngreso(),
                hoy);

        Paciente persistido = pacientes.guardar(paciente);

        auditoria.registrar(
                autorId,
                AccionAuditoria.UPDATE,
                "paciente",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                "Actualizacion de paciente");

        return persistido;
    }
}
