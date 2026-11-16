package co.edu.uniquindio.meditrack.alerta.application;

import co.edu.uniquindio.meditrack.alerta.application.port.AlertaRepositoryPort;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort.FuenteCita;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort.FuenteSignos;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort.FuenteToma;
import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.alerta.domain.SeveridadAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Caso de uso: generacion automatica de alertas de seguimiento (Proceso 3).
 *
 * <p>Es un POJO sin anotaciones de Spring; lo invoca un componente
 * {@code @Scheduled} fino en {@code alerta/infrastructure/scheduling}. Evalua tres
 * fuentes, solo de residentes activos (el filtro lo aplica la consulta), usando el
 * bean {@code Clock} compartido (America/Bogota):</p>
 * <ol>
 *   <li>toma atrasada: programada hace mas de {@code tolerancia-toma-min} minutos y
 *   sin registro;</li>
 *   <li>cita PROGRAMADA vencida hace mas de {@code tolerancia-cita-min} minutos
 *   (sin cambiar su estado);</li>
 *   <li>signos vitales de las ultimas 24 h fuera de los umbrales de referencia.</li>
 * </ol>
 *
 * <p><b>Orden de operaciones:</b> primero se leen las tres fuentes (consultas) y al
 * final se escriben las alertas. Como PostgreSQL aborta la transaccion completa
 * despues de cualquier error (por ejemplo, la violacion de un indice unico por una
 * ejecucion concurrente), no se puede dejar una fuente sin leer despues de la
 * primera escritura: se lee todo antes.</p>
 *
 * <p><b>Idempotencia:</b> cada alerta referencia su fuente ({@code toma_id},
 * {@code cita_id}, {@code signos_id}) y la tabla tiene indices unicos parciales por
 * fuente (V2). Una segunda ejecucion no duplica porque las consultas ya excluyen
 * fuentes con alerta; si dos ejecuciones concurren, el indice unico lo impide y la
 * transaccion se revierte entera (no queda ni la alerta ni la toma OMITIDA creada
 * para esa fuente).</p>
 *
 * <p><b>Mensajes genericos:</b> ninguna alerta incluye valores clinicos; el detalle
 * se consulta en la bitacora.</p>
 */
public class GenerarAlertasUseCase {

    /** Mensajes genericos: nunca llevan valores clinicos ni datos del residente. */
    public static final String MENSAJE_DOSIS_ATRASADA = "Toma de medicacion programada sin registro";
    public static final String MENSAJE_CITA_PROXIMA = "Cita programada sin registrar cumplimiento";
    public static final String MENSAJE_SIGNO_FUERA_DE_RANGO = "Signos vitales fuera del rango de referencia";

    /** Zona horaria del centro: la misma del bean Clock compartido. */
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final FuentesAlertaPort fuentes;
    private final AlertaRepositoryPort alertas;
    private final ConfiguracionAlertas configuracion;
    private final Clock clock;

    public GenerarAlertasUseCase(
            FuentesAlertaPort fuentes,
            AlertaRepositoryPort alertas,
            ConfiguracionAlertas configuracion,
            Clock clock) {
        this.fuentes = fuentes;
        this.alertas = alertas;
        this.configuracion = configuracion;
        this.clock = clock;
    }

    /** Ejecuta una pasada de generacion y resume el resultado. */
    public ResultadoGeneracion ejecutar() {
        Instant ahora = clock.instant();
        LocalDate fechaCentro = ahora.atZone(ZONA).toLocalDate();

        // 1) Lectura: las tres fuentes antes de cualquier escritura.
        List<FuenteToma> tomas = fuentes.tomasAtrasadas(
                ahora.minus(Duration.ofMinutes(configuracion.toleranciaTomaMin())),
                fechaCentro.toString(), ZONA.getId());
        List<FuenteCita> citas = fuentes.citasProgramadasVencidas(
                ahora.minus(Duration.ofMinutes(configuracion.toleranciaCitaMin())));
        List<FuenteSignos> signos = fuentes.signosFueraDeRango(
                ahora.minus(Duration.ofHours(configuracion.ventanaSignosHoras())),
                aConsulta(configuracion.umbrales()));

        // 2) Escritura: una alerta por candidato; las fuentes ya no se vuelven a leer.
        int creadas = 0;
        int duplicadas = 0;

        for (FuenteToma toma : tomas) {
            Long registroToma = toma.registroTomaId();
            if (registroToma == null) {
                // V2 exige que alerta.toma_id exista en registro_toma y no guarda las
                // tomas futuras: la toma atrasada se materializa como OMITIDA.
                registroToma = alertas.localizarOMaterializarTomaOmitida(
                        toma.horarioId(), toma.pacienteId(),
                        fechaCentro.toString(), ZONA.getId(), ahora).id();
            }
            if (registrar(Alerta.delSistema(
                    toma.pacienteId(), TipoAlerta.DOSIS_ATRASADA, SeveridadAlerta.MEDIA,
                    MENSAJE_DOSIS_ATRASADA, null, registroToma, null, ahora))) {
                creadas++;
            } else {
                duplicadas++;
            }
        }
        for (FuenteCita cita : citas) {
            if (registrar(Alerta.delSistema(
                    cita.pacienteId(), TipoAlerta.CITA_PROXIMA, SeveridadAlerta.MEDIA,
                    MENSAJE_CITA_PROXIMA, null, null, cita.citaId(), ahora))) {
                creadas++;
            } else {
                duplicadas++;
            }
        }
        for (FuenteSignos registro : signos) {
            if (registrar(Alerta.delSistema(
                    registro.pacienteId(), TipoAlerta.SIGNO_FUERA_DE_RANGO, SeveridadAlerta.ALTA,
                    MENSAJE_SIGNO_FUERA_DE_RANGO, registro.signosId(), null, null, ahora))) {
                creadas++;
            } else {
                duplicadas++;
            }
        }

        return new ResultadoGeneracion(
                tomas.size() + citas.size() + signos.size(), creadas, duplicadas);
    }

    /** Devuelve true si la alerta se creo; false si ya existia (indice unico). */
    private boolean registrar(Alerta alerta) {
        return alertas.guardar(alerta).isPresent();
    }

    private static FuentesAlertaPort.UmbralesConsulta aConsulta(ConfiguracionAlertas.Umbrales umbrales) {
        return new FuentesAlertaPort.UmbralesConsulta(
                umbrales.sistolicaMin(), umbrales.sistolicaMax(),
                umbrales.diastolicaMin(), umbrales.diastolicaMax(),
                umbrales.glucosaMin(), umbrales.glucosaMax(),
                umbrales.temperaturaMin(), umbrales.temperaturaMax());
    }
}