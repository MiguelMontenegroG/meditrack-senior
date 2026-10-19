package co.edu.uniquindio.meditrack.bitacora.domain;

import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro de signos vitales de un residente (modelo de dominio, sin Spring ni
 * JPA). Es un registro <b>inmutable</b>: una vez creado no se edita ni se borra
 * (ver {@code docs/seguridad.md}, seccion de bitacora).
 *
 * <p>Mapea la tabla {@code registro_signos_vitales} de la migracion V2:
 * paciente, autor ({@code registradoPor}), {@code fechaHora} (momento de la
 * medicion), presion sistolica y diastolica, glucosa, temperatura y
 * observaciones. Las columnas clinicas son opcionales en el esquema: se puede
 * registrar un subconjunto de signos (por ejemplo solo temperatura), pero al
 * menos uno es obligatorio.</p>
 *
 * <p><b>Rangos de plausibilidad (no clinicos).</b> El proposito es rechazar
 * errores de digitacion (un dedo de mas, un decimal corrido) con un 400 claro,
 * no clasificar el estado de salud del residente: la bitacora nunca marca un
 * valor como "anormal" ni genera alertas (eso corresponde al modulo alerta). Los
 * limites replican exactamente los CHECK de la tabla en V2, de modo que lo que el
 * dominio acepta siempre cabe en la base y viceversa:</p>
 * <ul>
 *   <li>Sistolica: 50-300 mmHg ({@code chk_signos_sistolica BETWEEN 50 AND 300}).</li>
 *   <li>Diastolica: 30-200 mmHg ({@code chk_signos_diastolica BETWEEN 30 AND 200})
 *   y estrictamente menor que la sistolica ({@code chk_signos_presion_coherente}),
 *   cuando ambas vienen informadas.</li>
 *   <li>Glucosa: 20-800 mg/dL ({@code chk_signos_glucosa BETWEEN 20 AND 800}).</li>
 *   <li>Temperatura: 30.0-45.0 grados C ({@code chk_signos_temp BETWEEN 30.0 AND 45.0}),
 *   con un decimal ({@code NUMERIC(4,1)}).</li>
 * </ul>
 *
 * <p><b>Fecha/hora.</b> El esquema guarda {@code fecha_hora TIMESTAMPTZ NOT NULL}.
 * El servidor la toma del bean {@code Clock} por defecto; el cliente puede enviar
 * una fecha de medicion opcional (por ejemplo si registra un dato de hace una
 * hora), pero nunca en el futuro (400 en ese caso).</p>
 *
 * <p><b>Autor.</b> {@code registradoPor} sale siempre del usuario autenticado,
 * nunca del cuerpo de la peticion.</p>
 */
public class RegistroSignosVitales {

    /** Limites de plausibilidad; ver la justificacion en el javadoc de la clase. */
    static final int SISTOLICA_MIN = 50;
    static final int SISTOLICA_MAX = 300;
    static final int DIASTOLICA_MIN = 30;
    static final int DIASTOLICA_MAX = 200;
    static final int GLUCOSA_MIN = 20;
    static final int GLUCOSA_MAX = 800;
    static final BigDecimal TEMPERATURA_MIN = new BigDecimal("30.0");
    static final BigDecimal TEMPERATURA_MAX = new BigDecimal("45.0");

    private final Long id;
    private final Long pacienteId;
    private final Long registradoPor;
    private final Instant fechaHora;
    private final Integer presionSistolica;
    private final Integer presionDiastolica;
    private final Integer glucosaMgDl;
    private final BigDecimal temperaturaC;
    private final String observaciones;

    public RegistroSignosVitales(
            Long id,
            Long pacienteId,
            Long registradoPor,
            Instant fechaHora,
            Integer presionSistolica,
            Integer presionDiastolica,
            Integer glucosaMgDl,
            BigDecimal temperaturaC,
            String observaciones) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.registradoPor = registradoPor;
        this.fechaHora = fechaHora;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
        this.glucosaMgDl = glucosaMgDl;
        this.temperaturaC = temperaturaC;
        this.observaciones = observaciones;
    }

    /**
     * Crea un registro nuevo (sin id aun). Valida que el residente este indicado,
     * que haya al menos un signo informado, que cada signo caiga dentro de su
     * rango de plausibilidad, que la presion sea coherente y que la fecha de
     * medicion no sea futura.
     *
     * @param pacienteId        residente de la bitacora.
     * @param registradoPor     autor; sale del usuario autenticado.
     * @param fechaHora         momento de la medicion; si es nulo se usa {@code ahora}.
     * @param ahora             instante actual obtenido del bean {@code Clock}.
     * @throws ValidacionDeNegocioException si alguna regla no se cumple (400).
     */
    public static RegistroSignosVitales nuevo(
            Long pacienteId,
            Long registradoPor,
            Instant fechaHora,
            Integer presionSistolica,
            Integer presionDiastolica,
            Integer glucosaMgDl,
            BigDecimal temperaturaC,
            String observaciones,
            Instant ahora) {
        if (pacienteId == null) {
            throw new ValidacionDeNegocioException("El residente es obligatorio");
        }
        if (registradoPor == null) {
            throw new ValidacionDeNegocioException("El autor del registro es obligatorio");
        }
        exigirAlMenosUnSigno(presionSistolica, presionDiastolica, glucosaMgDl, temperaturaC);
        validarSistolica(presionSistolica);
        validarDiastolica(presionDiastolica);
        validarCoherenciaPresion(presionSistolica, presionDiastolica);
        validarGlucosa(glucosaMgDl);
        validarTemperatura(temperaturaC);
        Instant fecha = resolverFechaHora(fechaHora, ahora);

        return new RegistroSignosVitales(
                null,
                pacienteId,
                registradoPor,
                fecha,
                presionSistolica,
                presionDiastolica,
                glucosaMgDl,
                temperaturaC,
                normalizar(observaciones));
    }

    private static void exigirAlMenosUnSigno(
            Integer sistolica, Integer diastolica, Integer glucosa, BigDecimal temperatura) {
        if (sistolica == null && diastolica == null && glucosa == null && temperatura == null) {
            throw new ValidacionDeNegocioException(
                    "Debe registrar al menos un signo vital (presion, glucosa o temperatura)");
        }
    }

    private static void validarSistolica(Integer sistolica) {
        if (sistolica != null && (sistolica < SISTOLICA_MIN || sistolica > SISTOLICA_MAX)) {
            throw new ValidacionDeNegocioException(
                    "La presion sistolica debe estar entre " + SISTOLICA_MIN + " y "
                            + SISTOLICA_MAX + " mmHg");
        }
    }

    private static void validarDiastolica(Integer diastolica) {
        if (diastolica != null && (diastolica < DIASTOLICA_MIN || diastolica > DIASTOLICA_MAX)) {
            throw new ValidacionDeNegocioException(
                    "La presion diastolica debe estar entre " + DIASTOLICA_MIN + " y "
                            + DIASTOLICA_MAX + " mmHg");
        }
    }

    private static void validarCoherenciaPresion(Integer sistolica, Integer diastolica) {
        if (sistolica != null && diastolica != null && diastolica >= sistolica) {
            throw new ValidacionDeNegocioException(
                    "La presion diastolica debe ser menor que la sistolica");
        }
    }

    private static void validarGlucosa(Integer glucosa) {
        if (glucosa != null && (glucosa < GLUCOSA_MIN || glucosa > GLUCOSA_MAX)) {
            throw new ValidacionDeNegocioException(
                    "La glucosa debe estar entre " + GLUCOSA_MIN + " y " + GLUCOSA_MAX + " mg/dL");
        }
    }

    private static void validarTemperatura(BigDecimal temperatura) {
        if (temperatura != null
                && (temperatura.compareTo(TEMPERATURA_MIN) < 0
                || temperatura.compareTo(TEMPERATURA_MAX) > 0)) {
            throw new ValidacionDeNegocioException(
                    "La temperatura debe estar entre " + TEMPERATURA_MIN.toPlainString()
                            + " y " + TEMPERATURA_MAX.toPlainString() + " grados C");
        }
    }

    /**
     * Resuelve la fecha/hora del registro: si el cliente no envia una fecha de
     * medicion se usa el instante del servidor; si la envia, no puede ser futura.
     */
    private static Instant resolverFechaHora(Instant fechaHora, Instant ahora) {
        if (fechaHora == null) {
            return ahora;
        }
        if (fechaHora.isAfter(ahora)) {
            throw new ValidacionDeNegocioException(
                    "La fecha y hora de la medicion no puede estar en el futuro");
        }
        return fechaHora;
    }

    private static String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public Integer getPresionSistolica() {
        return presionSistolica;
    }

    public Integer getPresionDiastolica() {
        return presionDiastolica;
    }

    public Integer getGlucosaMgDl() {
        return glucosaMgDl;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
