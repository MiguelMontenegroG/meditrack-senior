package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Caso de uso: crear un paciente (solo administrador).
 *
 * <p>Regla: el documento es unico.</p>
 */
public class CrearPacienteUseCase {

    private final PacienteRepositoryPort pacientes;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public CrearPacienteUseCase(PacienteRepositoryPort pacientes, AuditoriaPort auditoria, Clock reloj) {
        this.pacientes = pacientes;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Paciente ejecutar(ComandoCrearPaciente comando, Long autorId) {
        if (comando.documento() == null || comando.documento().isBlank()) {
            throw new ConflictoDeNegocioException("El documento es obligatorio");
        }
        if (pacientes.existePorDocumento(comando.documento().trim())) {
            throw new ConflictoDeNegocioException("Ya existe un paciente con ese documento");
        }

        LocalDate hoy = LocalDate.now(reloj);
        Paciente paciente = Paciente.nuevo(
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
                AccionAuditoria.CREATE,
                "paciente",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                "Creacion de paciente");

        return persistido;
    }
}
