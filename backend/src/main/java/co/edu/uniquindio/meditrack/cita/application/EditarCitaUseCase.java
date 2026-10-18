package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

import java.time.Clock;
import java.time.Instant;

/**
 * Caso de uso: editar una cita (solo administrador).
 *
 * <p>La cita debe pertenecer al residente indicado, el residente debe estar
 * visible y activo y la cita debe seguir en estado inicial (PROGRAMADA); en caso
 * contrario el dominio responde 409. Si se indica especialidad, debe existir.</p>
 */
public class EditarCitaUseCase {

    private final CitaRepositoryPort citas;
    private final EspecialidadRepositoryPort especialidades;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public EditarCitaUseCase(
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

    public Cita ejecutar(Long pacienteId, Long citaId, ComandoCita comando, AlcanceCita autor) {
        alcance.exigirVisibleYActivo(pacienteId, aAlcanceMedicacion(autor));

        Cita cita = citas.buscarPorId(citaId)
                .filter(c -> c.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        if (comando.especialidadId() != null && !especialidades.existe(comando.especialidadId())) {
            throw new RecursoNoEncontradoException("Especialidad no encontrada");
        }

        Instant ahora = reloj.instant();
        cita.editar(
                comando.especialidadId(),
                comando.profesional(),
                comando.fechaHora(),
                comando.lugar(),
                comando.motivo(),
                ahora);

        Cita persistida = citas.guardar(cita);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.UPDATE, "cita",
                citaId, ResultadoAuditoria.EXITOSO, "Edicion de cita");

        return persistida;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceCita alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
