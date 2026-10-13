package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.port.RegistroTomaRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Caso de uso: registrar la toma de un esquema (ADMINISTRADOR o
 * CUIDADOR_ENFERMERO, solo residentes con asignacion vigente).
 *
 * <p>Reglas: el esquema debe pertenecer a un residente visible y estar
 * utilizable (activo y vigente); la hora programada debe corresponder a uno de
 * los horarios del esquema (una hora no programada se rechaza con 400); la hora
 * programada no puede ser futura (no se registran tomas de horas que aun no
 * llegan) y solo puede existir una toma por esquema y hora programada (409). El
 * autor sale del usuario autenticado.</p>
 */
public class RegistrarTomaUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final RegistroTomaRepositoryPort tomas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public RegistrarTomaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            RegistroTomaRepositoryPort tomas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock reloj) {
        this.esquemas = esquemas;
        this.tomas = tomas;
        this.alcance = alcance;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public RegistroToma ejecutar(
            Long pacienteId, Long esquemaId, ComandoRegistrarToma comando, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);

        EsquemaMedicacion esquema = esquemas.buscarPorId(esquemaId)
                .filter(e -> e.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Esquema no encontrado"));

        Instant ahora = reloj.instant();
        if (!esquema.utilizableEn(ahora.atZone(reloj.getZone()).toLocalDate())) {
            throw new ConflictoDeNegocioException("El esquema no esta activo o no esta vigente");
        }
        if (comando.fechaHoraProgramada().isAfter(ahora)) {
            throw new ValidacionDeNegocioException("No se puede registrar una toma de una hora futura");
        }
        if (!correspondeAUnHorario(esquema, comando.fechaHoraProgramada(), reloj.getZone())) {
            throw new ValidacionDeNegocioException(
                    "La hora programada no corresponde a ningun horario del esquema");
        }
        if (tomas.existeParaEsquemaYProgramada(esquemaId, comando.fechaHoraProgramada())) {
            throw new ConflictoDeNegocioException("Ya existe una toma registrada para esa hora programada");
        }

        RegistroToma registro = RegistroToma.registrar(
                esquemaId,
                autor.usuarioId(),
                comando.fechaHoraProgramada(),
                comando.estado(),
                comando.observaciones(),
                ahora);

        RegistroToma persistido = tomas.guardar(registro);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.CREATE, "registro_toma",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Registro de toma");

        return persistido;
    }

    /**
     * Comprueba que el instante programado corresponda a la hora (HH:mm) de
     * alguno de los horarios del esquema. Se compara la hora local en la zona
     * del centro, ya que los horarios se guardan como hora del dia.
     */
    private static boolean correspondeAUnHorario(EsquemaMedicacion esquema, Instant programada, ZoneId zona) {
        LocalTime horaProgramada = programada.atZone(zona).toLocalTime();
        return esquema.horas().stream().anyMatch(hora -> hora.equals(horaProgramada));
    }
}
