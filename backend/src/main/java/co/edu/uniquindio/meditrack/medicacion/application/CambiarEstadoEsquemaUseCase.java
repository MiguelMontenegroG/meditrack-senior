package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: suspender o reactivar un esquema (solo administrador).
 *
 * <p>Borrado logico: el esquema y su historial de tomas se conservan siempre.</p>
 */
public class CambiarEstadoEsquemaUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public CambiarEstadoEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.esquemas = esquemas;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public EsquemaMedicacion ejecutar(Long pacienteId, Long esquemaId, boolean activo, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);

        EsquemaMedicacion esquema = esquemas.buscarPorId(esquemaId)
                .filter(e -> e.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Esquema no encontrado"));

        if (activo) {
            esquema.activar();
        } else {
            esquema.suspender();
        }
        EsquemaMedicacion persistido = esquemas.guardar(esquema);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.UPDATE, "esquema_medicacion",
                esquemaId, ResultadoAuditoria.EXITOSO, "Cambio de estado de esquema");

        return persistido;
    }
}
