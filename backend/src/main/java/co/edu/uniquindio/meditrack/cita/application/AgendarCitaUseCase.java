package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

import java.time.Clock;
import java.time.Instant;

/**
 * Caso de uso: agendar una cita de un residente (solo administrador).
 *
 * <p>Reglas: el residente debe ser visible y estar activo; si se indica
 * especialidad, debe existir en el catalogo; la fecha/hora no puede estar en el
 * pasado. El autor de la cita ({@code registradaPor}) sale del usuario
 * autenticado. Reutiliza el alcance del modulo paciente a traves de
 * {@link VerificadorAlcanceResidente} (no duplica la regla de visibilidad).</p>
 */
public class AgendarCitaUseCase {

    private final CitaRepositoryPort citas;
    private final EspecialidadRepositoryPort especialidades;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public AgendarCitaUseCase(
            CitaRepositoryPort citas,
            EspecialidadRepositoryPort especialidades,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock reloj) {
        this.citas = citas;
        this.especialidades = especialidades;
        this.alcance = alcance;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Cita ejecutar(Long pacienteId, ComandoCita comando, AlcanceCita autor) {
        alcance.exigirVisibleYActivo(pacienteId, aAlcanceMedicacion(autor));

        if (comando.especialidadId() != null && !especialidades.existe(comando.especialidadId())) {
            throw new RecursoNoEncontradoException("Especialidad no encontrada");
        }

        Instant ahora = reloj.instant();
        Cita cita = Cita.nuevo(
                pacienteId,
                autor.usuarioId(),
                comando.especialidadId(),
                comando.profesional(),
                comando.fechaHora(),
                comando.lugar(),
                comando.motivo(),
                ahora);

        Cita persistida = citas.guardar(cita);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.CREATE, "cita",
                persistida.getId(), ResultadoAuditoria.EXITOSO, "Agendamiento de cita");

        return persistida;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceCita alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
