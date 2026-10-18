package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: obtener el detalle de una cita aplicando el alcance del usuario.
 * La cita debe pertenecer al residente indicado. La lectura se audita como
 * EXITOSA (la lectura denegada la audita el verificador de alcance).
 */
public class ObtenerCitaUseCase {

    private final CitaRepositoryPort citas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ObtenerCitaUseCase(
            CitaRepositoryPort citas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.citas = citas;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public Cita ejecutar(Long pacienteId, Long citaId, AlcanceCita autor) {
        alcance.exigirVisible(pacienteId, aAlcanceMedicacion(autor));

        Cita cita = citas.buscarPorId(citaId)
                .filter(c -> c.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "cita",
                citaId, ResultadoAuditoria.EXITOSO, "Lectura de cita");

        return cita;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceCita alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
