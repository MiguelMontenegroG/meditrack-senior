package co.edu.uniquindio.meditrack.alerta.application;

import co.edu.uniquindio.meditrack.alerta.application.port.AlertaRepositoryPort;
import co.edu.uniquindio.meditrack.alerta.domain.AlcanceAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

import java.time.Clock;

/**
 * Caso de uso: atender una alerta (ACTIVA -> ATENDIDA/REVISADA).
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>El familiar nunca atiende (403).</li>
 *   <li>La alerta inexistente responde 404.</li>
 *   <li>Si el residente de la alerta esta fuera del alcance del usuario responde
 *   404 (nunca 403) y se audita {@code READ DENEGADO}, sin revelar su existencia.</li>
 *   <li>Una alerta ya atendida responde 409.</li>
 *   <li>Al atender se registra quien y cuando (columnas {@code revisada_por} y
 *   {@code revisada_en} de V2) y se audita {@code UPDATE EXITOSO}.</li>
 * </ul>
 */
public class AtenderAlertaUseCase {

    private final AlertaRepositoryPort alertas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock clock;

    public AtenderAlertaUseCase(
            AlertaRepositoryPort alertas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        this.alertas = alertas;
        this.alcance = alcance;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    public Alerta ejecutar(Long alertaId, AlcanceAlerta autor) {
        if (!autor.puedeAtender()) {
            auditoria.registrar(autor.usuarioId(), AccionAuditoria.UPDATE, "alerta",
                    alertaId, ResultadoAuditoria.DENEGADO, "Intento de atender una alerta sin permiso");
            throw new AccesoDenegadoException("Solo el administrador o el cuidador pueden atender alertas");
        }

        Alerta alerta = alertas.buscarPorId(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada"));

        // 404 + READ DENEGADO si el residente de la alerta no es visible.
        alcance.exigirVisible(alerta.getPacienteId(),
                AlcanceMedicacion.de(autor.usuarioId(), RolUsuario.valueOf(autor.rol().name())));

        alerta.atender(autor.usuarioId(), clock.instant());
        Alerta atendida = alertas.actualizar(alerta);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.UPDATE, "alerta",
                alertaId, ResultadoAuditoria.EXITOSO, "Atencion de alerta");
        return atendida;
    }
}
