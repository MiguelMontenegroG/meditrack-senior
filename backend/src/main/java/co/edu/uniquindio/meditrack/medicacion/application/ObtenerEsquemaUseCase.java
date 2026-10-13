package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: obtener el detalle de un esquema aplicando el alcance del usuario.
 * El esquema debe pertenecer al residente indicado.
 */
public class ObtenerEsquemaUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ObtenerEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.esquemas = esquemas;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public EsquemaMedicacion ejecutar(Long pacienteId, Long esquemaId, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);

        EsquemaMedicacion esquema = esquemas.buscarPorId(esquemaId)
                .filter(e -> e.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Esquema no encontrado"));

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "esquema_medicacion",
                esquemaId, ResultadoAuditoria.EXITOSO, "Lectura de esquema");

        return esquema;
    }
}
