package co.edu.uniquindio.meditrack.alerta.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Instant;

/**
 * Alerta de seguimiento de un residente (modelo de dominio, sin Spring ni JPA).
 *
 * <p>Mapea la tabla {@code alerta} de la migracion V2: paciente, tipo, severidad,
 * mensaje, origen, referencia opcional a la fuente ({@code toma_id},
 * {@code cita_id} o {@code signos_id}), estado, {@code creado_en} y
 * {@code revisada_en}/{@code revisada_por}.</p>
 *
 * <p>Decisiones de diseno:</p>
 * <ul>
 *   <li>El mensaje es <b>generico</b> y nunca incluye valores clinicos: el detalle
 *   se consulta en la bitacora (los datos de salud no se replican ni se envían por
 *   notificaciones).</li>
 *   <li>Exactamente una fuente por alerta (lo exige
 *   {@code chk_alerta_fuente_unica} en V2).</li>
 *   <li>Ciclo de vida ACTIVA -> ATENDIDA. ATENDIDA se persiste como
 *   {@code REVISADA} con {@code revisada_en}/{@code revisada_por} porque V2 no
 *   tiene columnas de "atencion". Una alerta ya atendida responde 409.</li>
 *   <li>Las alertas no se borran (trazabilidad).</li>
 * </ul>
 */
public class Alerta {

    private final Long id;
    private final Long pacienteId;
    private final TipoAlerta tipo;
    private final SeveridadAlerta severidad;
    private final String mensaje;
    private final OrigenAlerta origen;
    private final Long signosId;
    private final Long tomaId;
    private final Long citaId;
    private EstadoAlerta estado;
    private final Instant creadoEn;
    private Instant revisadaEn;
    private Long revisadaPor;

    public Alerta(
            Long id,
            Long pacienteId,
            TipoAlerta tipo,
            SeveridadAlerta severidad,
            String mensaje,
            OrigenAlerta origen,
            Long signosId,
            Long tomaId,
            Long citaId,
            EstadoAlerta estado,
            Instant creadoEn,
            Instant revisadaEn,
            Long revisadaPor) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tipo = tipo;
        this.severidad = severidad;
        this.mensaje = mensaje;
        this.origen = origen;
        this.signosId = signosId;
        this.tomaId = tomaId;
        this.citaId = citaId;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.revisadaEn = revisadaEn;
        this.revisadaPor = revisadaPor;
    }

    /**
     * Crea una alerta nueva del sistema (sin id) en estado ACTIVA.
     *
     * @throws ValidacionDeNegocioException si no hay paciente, mensaje o fuente, o
     *                                      si se informa mas de una fuente.
     */
    public static Alerta delSistema(
            Long pacienteId,
            TipoAlerta tipo,
            SeveridadAlerta severidad,
            String mensaje,
            Long signosId,
            Long tomaId,
            Long citaId,
            Instant creadoEn) {
        if (pacienteId == null) {
            throw new ValidacionDeNegocioException("El residente de la alerta es obligatorio");
        }
        if (tipo == null || severidad == null) {
            throw new ValidacionDeNegocioException("El tipo y la severidad de la alerta son obligatorios");
        }
        if (mensaje == null || mensaje.isBlank()) {
            throw new ValidacionDeNegocioException("El mensaje de la alerta es obligatorio");
        }
        exigirFuenteUnica(signosId, tomaId, citaId);
        return new Alerta(
                null, pacienteId, tipo, severidad, mensaje, OrigenAlerta.SISTEMA,
                signosId, tomaId, citaId, EstadoAlerta.ACTIVA, creadoEn, null, null);
    }

    /**
     * Marca la alerta como atendida (ACTIVA -> ATENDIDA/REVISADA) registrando
     * quien y cuando.
     *
     * @throws ConflictoDeNegocioException si ya estaba atendida.
     */
    public void atender(Long usuarioId, Instant ahora) {
        if (usuarioId == null) {
            throw new ValidacionDeNegocioException("El usuario que atiende es obligatorio");
        }
        // Se valida antes de mutar: si se rechaza, la instancia sigue ACTIVA y el
        // cambio espurio no puede persistirse por el auto-flush del ORM.
        if (estado != EstadoAlerta.ACTIVA) {
            throw new ConflictoDeNegocioException("La alerta ya fue atendida");
        }
        estado = EstadoAlerta.REVISADA;
        revisadaEn = ahora;
        revisadaPor = usuarioId;
    }

    /** Indica si la alerta esta pendiente de atencion. */
    public boolean estaActiva() {
        return estado == EstadoAlerta.ACTIVA;
    }

    private static void exigirFuenteUnica(Long signosId, Long tomaId, Long citaId) {
        int fuentes = (signosId != null ? 1 : 0) + (tomaId != null ? 1 : 0) + (citaId != null ? 1 : 0);
        if (fuentes != 1) {
            throw new ValidacionDeNegocioException("La alerta debe referenciar exactamente una fuente");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public SeveridadAlerta getSeveridad() {
        return severidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public OrigenAlerta getOrigen() {
        return origen;
    }

    public Long getSignosId() {
        return signosId;
    }

    public Long getTomaId() {
        return tomaId;
    }

    public Long getCitaId() {
        return citaId;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getRevisadaEn() {
        return revisadaEn;
    }

    public Long getRevisadaPor() {
        return revisadaPor;
    }
}
