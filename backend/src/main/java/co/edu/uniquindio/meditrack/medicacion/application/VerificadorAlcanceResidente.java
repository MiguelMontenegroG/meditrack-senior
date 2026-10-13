package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Resuelve el acceso de un usuario a un residente para el modulo de medicacion,
 * centralizando la regla comun:
 *
 * <ul>
 *   <li>residente inexistente o fuera de alcance -> 404 (nunca 403) + READ
 *   DENEGADO en auditoria, sin revelar su existencia;</li>
 *   <li>residente inactivo -> 409 para operaciones de escritura (no admite
 *   nuevos esquemas ni tomas).</li>
 * </ul>
 *
 * <p>Evita duplicar esta logica en cada caso de uso y mantiene el filtrado por
 * rol en el backend.</p>
 */
public class VerificadorAlcanceResidente {

    private final PacienteAlcancePort pacientes;
    private final AuditoriaPort auditoria;

    public VerificadorAlcanceResidente(PacienteAlcancePort pacientes, AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.auditoria = auditoria;
    }

    /**
     * Verifica que el residente sea visible para el usuario. Si no lo es lanza
     * 404 y audita READ DENEGADO. Devuelve true siempre que no lance (residente
     * visible y existente).
     */
    public void exigirVisible(Long pacienteId, AlcanceMedicacion alcance) {
        if (!pacientes.visible(pacienteId, alcance.usuarioId(), alcance.rol())) {
            auditoria.registrar(
                    alcance.usuarioId(),
                    AccionAuditoria.READ,
                    "paciente",
                    pacienteId,
                    ResultadoAuditoria.DENEGADO,
                    "Acceso a medicacion de residente fuera de alcance o inexistente");
            throw new RecursoNoEncontradoException("Residente no encontrado");
        }
    }

    /**
     * Verifica que el residente sea visible y ademas este activo (para operaciones
     * de escritura: nuevo esquema o nueva toma).
     */
    public void exigirVisibleYActivo(Long pacienteId, AlcanceMedicacion alcance) {
        exigirVisible(pacienteId, alcance);
        if (!pacientes.estaActivo(pacienteId)) {
            throw new ConflictoDeNegocioException("El residente esta inactivo y no admite cambios de medicacion");
        }
    }
}
