package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.port.RegistroTomaRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.CalendarioTomas;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: consultar la agenda de medicacion de un residente para un dia,
 * calculando que tomas estan pendientes, atrasadas, administradas u omitidas.
 *
 * <p>Lo pendiente/atrasado se deriva ({@link CalendarioTomas}), nunca se
 * persiste. Se consideran los esquemas activos y vigentes en la fecha, y las
 * tomas ya registradas de esos esquemas en ese dia.</p>
 */
public class ConsultarAgendaDiaUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final RegistroTomaRepositoryPort tomas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public ConsultarAgendaDiaUseCase(
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

    public List<AgendaEsquema> ejecutar(Long pacienteId, LocalDate fecha, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);

        ZoneId zona = reloj.getZone();
        Instant ahora = reloj.instant();
        List<EsquemaMedicacion> vigentes = esquemas.listarUtilizables(pacienteId, fecha);

        List<Long> ids = vigentes.stream().map(EsquemaMedicacion::getId).toList();
        Instant desde = fecha.atStartOfDay(zona).toInstant();
        Instant hasta = fecha.plusDays(1).atStartOfDay(zona).toInstant();
        List<RegistroToma> registradas = ids.isEmpty()
                ? List.of()
                : tomas.listarPorEsquemasYRango(ids, desde, hasta);

        List<AgendaEsquema> agenda = new ArrayList<>();
        for (EsquemaMedicacion esquema : vigentes) {
            List<RegistroToma> delEsquema = registradas.stream()
                    .filter(t -> t.getEsquemaId().equals(esquema.getId()))
                    .toList();
            agenda.add(new AgendaEsquema(
                    esquema,
                    CalendarioTomas.calcularDelDia(esquema, delEsquema, fecha, zona, ahora)));
        }

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "agenda_medicacion",
                pacienteId, ResultadoAuditoria.EXITOSO, "Consulta de agenda de medicacion del dia");

        return agenda;
    }
}
