package co.edu.uniquindio.meditrack.bitacora.application;

import co.edu.uniquindio.meditrack.bitacora.application.port.SignosVitalesRepositoryPort;
import co.edu.uniquindio.meditrack.bitacora.domain.AlcanceBitacora;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: ultimo registro de signos vitales (el mas reciente por fecha) de un
 * residente.
 *
 * <p>Los tres roles pueden leer, segun su alcance. Si el residente esta fuera de
 * alcance o es inexistente, el verificador responde 404 y audita READ DENEGADO. Si
 * el residente es visible pero no tiene ningun registro, responde 404 sin
 * auditar un acceso denegado (el residente si era accesible, simplemente no hay
 * bitacora).</p>
 */
public class ObtenerUltimoSignosVitalesUseCase {

    private final SignosVitalesRepositoryPort signos;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ObtenerUltimoSignosVitalesUseCase(
            SignosVitalesRepositoryPort signos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.signos = signos;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public RegistroSignosVitales ejecutar(Long pacienteId, AlcanceBitacora autor) {
        alcance.exigirVisible(pacienteId, aAlcanceMedicacion(autor));

        RegistroSignosVitales ultimo = signos.buscarUltimoPorPaciente(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El residente no tiene registros de signos vitales"));

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "signos_vitales",
                ultimo.getId(), ResultadoAuditoria.EXITOSO, "Lectura del ultimo registro de signos vitales");

        return ultimo;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceBitacora alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
