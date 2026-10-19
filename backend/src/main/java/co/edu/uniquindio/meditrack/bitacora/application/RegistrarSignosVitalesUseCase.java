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

import java.time.Clock;

/**
 * Caso de uso: registrar signos vitales de un residente (ADMINISTRADOR o
 * CUIDADOR_ENFERMERO).
 *
 * <p>Reglas: el residente debe existir, ser visible y estar activo (para
 * registrar). Se admite un subconjunto de signos, pero al menos uno. Los rangos de
 * plausibilidad y la fecha no futura los valida el dominio
 * ({@link RegistroSignosVitales}). El autor ({@code registradoPor}) sale del
 * usuario autenticado. Reutiliza el alcance del modulo paciente a traves de
 * {@link VerificadorAlcanceResidente} (no duplica la regla de visibilidad).</p>
 *
 * <p>El registro es inmutable: este caso de uso solo crea; no hay edicion ni
 * borrado.</p>
 */
public class RegistrarSignosVitalesUseCase {

    private final SignosVitalesRepositoryPort signos;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public RegistrarSignosVitalesUseCase(
            SignosVitalesRepositoryPort signos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock reloj) {
        this.signos = signos;
        this.alcance = alcance;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public RegistroSignosVitales ejecutar(
            Long pacienteId, ComandoSignosVitales comando, AlcanceBitacora autor) {
        alcance.exigirVisibleYActivo(pacienteId, aAlcanceMedicacion(autor));

        RegistroSignosVitales registro = RegistroSignosVitales.nuevo(
                pacienteId,
                autor.usuarioId(),
                comando.fechaHora(),
                comando.presionSistolica(),
                comando.presionDiastolica(),
                comando.glucosaMgDl(),
                comando.temperaturaC(),
                comando.observaciones(),
                reloj.instant());

        RegistroSignosVitales persistido = signos.guardar(registro);

        // Auditoria CREATE: nunca se incluyen valores clinicos en el detalle.
        auditoria.registrar(autor.usuarioId(), AccionAuditoria.CREATE, "signos_vitales",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Registro de signos vitales");

        return persistido;
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceBitacora alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
