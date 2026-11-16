package co.edu.uniquindio.meditrack.reporte.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Duration;
import java.time.Instant;

import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;

/**
 * Metadatos de un reporte clinico generado (modelo de dominio, sin Spring ni JPA).
 *
 * <p>Mapea la tabla {@code reporte_generado} de la migracion V2: residente,
 * autor ({@code generadoPor}), tipo, rango de fechas, ruta de almacenamiento y
 * fecha de generacion. <b>El PDF no se guarda como binario en la base de datos</b>:
 * se regenera en el momento de la descarga a partir de los datos vigentes del
 * residente acotados al rango solicitado. La tabla solo guarda metadatos y una
 * referencia de almacenamiento, tal como define V2.</p>
 *
 * <p>Reglas de negocio:</p>
 * <ul>
 *   <li>El rango de fechas es obligatorio y {@code desde} no puede ser posterior
 *   a {@code hasta} (400).</li>
 *   <li>El rango no puede superar {@link #DIAS_MAXIMOS}, para evitar reportes de
 *   historia completa que exceden lo necesario (Ley 1581).</li>
 *   <li>El autor ({@code generadoPor}) sale siempre del usuario autenticado, nunca
 *   del cuerpo de la peticion.</li>
 * </ul>
 */
public class ReporteGenerado {

    /**
     * Antiguedad maxima del rango de un reporte: un anio. Un reporte sobre toda la
     * vida del residente no aporta valor clinico al cuidado diario y si expone mas
     * datos de los necesarios.
     */
    public static final int DIAS_MAXIMOS = 366;

    private final Long id;
    private final Long pacienteId;
    private final Long generadoPor;
    private final TipoReporte tipo;
    private final Instant rangoDesde;
    private final Instant rangoHasta;
    private final String rutaAlmacenamiento;
    private final Instant creadoEn;

    public ReporteGenerado(
            Long id,
            Long pacienteId,
            Long generadoPor,
            TipoReporte tipo,
            Instant rangoDesde,
            Instant rangoHasta,
            String rutaAlmacenamiento,
            Instant creadoEn) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.generadoPor = generadoPor;
        this.tipo = tipo;
        this.rangoDesde = rangoDesde;
        this.rangoHasta = rangoHasta;
        this.rutaAlmacenamiento = rutaAlmacenamiento;
        this.creadoEn = creadoEn;
    }

    /**
     * Crea los metadatos de un reporte nuevo (sin id aun) validando su rango.
     *
     * @param pacienteId residente del reporte (ya validado su alcance por el caso
     *                   de uso).
     * @param generadoPor autor; sale del usuario autenticado.
     * @param tipo       tipo del reporte.
     * @param desde      inicio del rango (incluido).
     * @param hasta      fin del rango (incluido).
     * @param ahora      instante de generacion obtenido del bean {@code Clock}.
     * @throws ValidacionDeNegocioException si el rango es nulo, invertido o
     *                                  demasiado amplio.
     */
    public static ReporteGenerado nuevo(
            Long pacienteId,
            Long generadoPor,
            TipoReporte tipo,
            Instant desde,
            Instant hasta,
            Instant ahora) {
        if (pacienteId == null) {
            throw new ValidacionDeNegocioException("El residente es obligatorio");
        }
        if (generadoPor == null) {
            throw new ValidacionDeNegocioException("El autor del reporte es obligatorio");
        }
        if (tipo == null) {
            throw new ValidacionDeNegocioException("El tipo de reporte es obligatorio");
        }
        validarRango(desde, hasta);

        return new ReporteGenerado(
                null, pacienteId, generadoPor, tipo, desde, hasta,
                rutaSugerida(pacienteId, tipo, ahora), ahora);
    }

    /**
     * Valida que el rango este completo, ordenado y no exceda el maximo de dias.
     * Es publico para que el caso de uso pueda rechazar un rango invalido antes
     * de resolver el alcance del residente.
     */
    public static void validarRango(Instant desde, Instant hasta) {
        if (desde == null || hasta == null) {
            throw new ValidacionDeNegocioException("El rango del reporte exige 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new ValidacionDeNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        long dias = Duration.between(desde, hasta).toDays();
        if (dias > DIAS_MAXIMOS) {
            throw new ConflictoDeNegocioException(
                    "El rango del reporte no puede superar los " + DIAS_MAXIMOS + " dias");
        }
    }

    /**
     * Ruta de almacenamiento referencial del reporte. No es donde se guarda el
     * PDF (que no se almacena), sino una clave estable para trazabilidad y para
     * nombrar el archivo en la descarga.
     */
    private static String rutaSugerida(Long pacienteId, TipoReporte tipo, Instant generado) {
        return "reportes/" + pacienteId + "/" + tipo.name().toLowerCase() + "-" + generado.getEpochSecond();
    }

    /** Nombre de archivo sugerido para la descarga. */
    public String nombreArchivo() {
        return tipo.name().toLowerCase() + "-" + creadoEn.getEpochSecond() + ".pdf";
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getGeneradoPor() {
        return generadoPor;
    }

    public TipoReporte getTipo() {
        return tipo;
    }

    public Instant getRangoDesde() {
        return rangoDesde;
    }

    public Instant getRangoHasta() {
        return rangoHasta;
    }

    public String getRutaAlmacenamiento() {
        return rutaAlmacenamiento;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
