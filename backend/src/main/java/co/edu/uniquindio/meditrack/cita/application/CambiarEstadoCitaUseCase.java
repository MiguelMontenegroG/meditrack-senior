package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Clock;
import java.time.Instant;

/**
 * Caso de uso: cambiar el estado de una cita.
 *
 * <p>Destinos admitidos:</p>
 * <ul>
 *   <li>{@code CUMPLIDA} / {@code INCUMPLIDA}: administrador o cuidador
 *   (residente con asignacion vigente); solo desde el estado inicial y solo si
 *   la fecha/hora ya llego.</li>
 *   <li>{@code CANCELADA}: solo administrador; desde el estado inicial.</li>
 * </ul>
 *
 * <p>Registrar el regreso a {@code PROGRAMADA} o a {@code REPROGRAMADA} no es una
 * operacion del producto y se rechaza con validacion (400).</p>
 */
public class CambiarEstadoCitaUseCase {

    private final CitaRepositoryPort citas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public CambiarEstadoCitaUseCase(
            CitaRepositoryPort citas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock reloj) {
        this.citas = citas;
        this.alcance = alcance;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Cita ejecutar(Long pacienteId, Long citaId, EstadoCita nuevo, AlcanceCita autor) {
        alcance.exigirVisible(pacienteId, aAlcanceMedicacion(autor));

        if (nuevo == null) {
            throw new ValidacionDeNegocioException("El estado es obligatorio");
        }
        if (nuevo == EstadoCita.CANCELADA && !autor.esAdministrador()) {
            throw new AccesoDenegadoException("Solo el administrador puede cancelar una cita");
        }
        if (nuevo.esInicial() || nuevo == EstadoCita.REPROGRAMADA) {
            throw new ValidacionDeNegocioException(
                    "Estado no permitido: solo se puede marcar CUMPLIDA, INCUMPLIDA o CANCELADA");
        }

        Cita cita = citas.buscarPorId(citaId)
                .filter(c -> c.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        Instant ahora = reloj.instant();
        if (nuevo == EstadoCita.CANCELADA) {
            cita.cancelar();
        } else {
            cita.marcarCumplimiento(nuevo, ahora);
        }

        Cita persistida = citas.guardar(cita);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.UPDATE, "cita",
                citaId, ResultadoAuditoria.EXITOSO, "Cambio de estado de cita a " + nuevo.name());

        return persistida;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceCita alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
