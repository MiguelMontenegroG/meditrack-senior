package co.edu.uniquindio.meditrack.medicacion.domain;

import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Tratamiento de un residente con un medicamento: dosis, via, frecuencia y
 * vigencia, junto con sus horarios del dia.
 *
 * <p>Reglas de negocio del esquema (sin Spring ni JPA):</p>
 * <ul>
 *   <li>frecuencia entre 1 y 24 horas (igual que el CHECK de V2);</li>
 *   <li>{@code fechaFin} nulo (vigencia abierta) o posterior/igual a la fecha de
 *   inicio;</li>
 *   <li>coherencia entre horarios y frecuencia: cuando 24 es divisible por la
 *   frecuencia, la cantidad de horarios debe ser exactamente 24/frecuencia; en
 *   los demas casos se exige al menos un horario y ninguno repetido.</li>
 *   <li>borrado logico: suspender y reactivar sin borrar fisicamente.</li>
 * </ul>
 *
 * <p>La vigencia de una fecha se calcula con {@link #vigenteEn(LocalDate)} y no
 * se persiste.</p>
 */
public class EsquemaMedicacion {

    private final Long id;
    private final Long pacienteId;
    private final Long medicamentoId;
    private String dosis;
    private int frecuenciaHoras;
    private ViaAdministracion via;
    private String indicaciones;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;
    private List<HorarioMedicacion> horarios;

    public EsquemaMedicacion(
            Long id,
            Long pacienteId,
            Long medicamentoId,
            String dosis,
            int frecuenciaHoras,
            ViaAdministracion via,
            String indicaciones,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            boolean activo,
            List<HorarioMedicacion> horarios) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicamentoId = medicamentoId;
        this.dosis = dosis;
        this.frecuenciaHoras = frecuenciaHoras;
        this.via = via;
        this.indicaciones = indicaciones;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
        this.horarios = horarios;
    }

    /** Crea un esquema nuevo validando todas las reglas. */
    public static EsquemaMedicacion nuevo(
            Long pacienteId,
            Long medicamentoId,
            String dosis,
            int frecuenciaHoras,
            ViaAdministracion via,
            String indicaciones,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<HorarioMedicacion> horarios) {
        validar(dosis, frecuenciaHoras, via, fechaInicio, fechaFin, horarios);
        return new EsquemaMedicacion(
                null, pacienteId, medicamentoId, dosis.trim(), frecuenciaHoras, via,
                indicaciones, fechaInicio, fechaFin, true, List.copyOf(horarios));
    }

    /** Actualiza los datos editables y los horarios del esquema. */
    public void actualizar(
            String dosis,
            int frecuenciaHoras,
            ViaAdministracion via,
            String indicaciones,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<HorarioMedicacion> horarios) {
        validar(dosis, frecuenciaHoras, via, fechaInicio, fechaFin, horarios);
        this.dosis = dosis.trim();
        this.frecuenciaHoras = frecuenciaHoras;
        this.via = via;
        this.indicaciones = indicaciones;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horarios = List.copyOf(horarios);
    }

    public void activar() {
        this.activo = true;
    }

    public void suspender() {
        this.activo = false;
    }

    /** Indica si la fecha indicada cae dentro de la vigencia (inicio/fin). */
    public boolean vigenteEn(LocalDate fecha) {
        boolean desdeInicio = !fecha.isBefore(fechaInicio);
        boolean hastaFin = fechaFin == null || !fecha.isAfter(fechaFin);
        return desdeInicio && hastaFin;
    }

    /** Indica si el esquema esta activo y vigente en la fecha indicada. */
    public boolean utilizableEn(LocalDate fecha) {
        return activo && vigenteEn(fecha);
    }

    /**
     * Indica si las vigencias de dos esquemas del mismo residente se solapan
     * (ambos extremos nulos cuentan como abiertos).
     */
    public boolean vigenciaSolapaCon(EsquemaMedicacion otro) {
        LocalDate finEste = fechaFin == null ? LocalDate.MAX : fechaFin;
        LocalDate finOtro = otro.fechaFin == null ? LocalDate.MAX : otro.fechaFin;
        return !fechaInicio.isAfter(finOtro) && !otro.fechaInicio.isAfter(finEste);
    }

    /** Conjunto de horas del dia del esquema, sin duplicados. */
    public List<LocalTime> horas() {
        return horarios.stream().map(HorarioMedicacion::hora).toList();
    }

    private static void validar(
            String dosis,
            int frecuenciaHoras,
            ViaAdministracion via,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            List<HorarioMedicacion> horarios) {
        if (dosis == null || dosis.isBlank()) {
            throw new ValidacionDeNegocioException("La dosis es obligatoria");
        }
        if (frecuenciaHoras < 1 || frecuenciaHoras > 24) {
            throw new ValidacionDeNegocioException("La frecuencia debe estar entre 1 y 24 horas");
        }
        if (via == null) {
            throw new ValidacionDeNegocioException("La via de administracion es obligatoria");
        }
        if (fechaInicio == null) {
            throw new ValidacionDeNegocioException("La fecha de inicio es obligatoria");
        }
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ValidacionDeNegocioException("La fecha de fin no puede ser anterior a la de inicio");
        }
        validarHorarios(frecuenciaHoras, horarios);
    }

    /**
     * Valida la coherencia entre horarios y frecuencia.
     *
     * <p>Regla elegida: cuando 24 es divisible por la frecuencia, el numero de
     * horarios debe ser exactamente 24/frecuencia (un dia completo, tomas
     * equiespaciadas). En los demas casos la division no es entera y no hay un
     * numero determinista, por lo que solo se exige al menos un horario y que no
     * se repita ninguna hora.</p>
     */
    private static void validarHorarios(int frecuenciaHoras, List<HorarioMedicacion> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            throw new ValidacionDeNegocioException("El esquema debe tener al menos un horario");
        }
        long horasDistintas = horarios.stream().map(HorarioMedicacion::hora).distinct().count();
        if (horasDistintas != horarios.size()) {
            throw new ValidacionDeNegocioException("No puede haber horarios repetidos en un esquema");
        }
        if (24 % frecuenciaHoras == 0) {
            int esperados = 24 / frecuenciaHoras;
            if (horarios.size() != esperados) {
                throw new ValidacionDeNegocioException(
                        "Con frecuencia de " + frecuenciaHoras + "h se esperan " + esperados
                                + " horarios; se recibieron " + horarios.size());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public String getDosis() {
        return dosis;
    }

    public int getFrecuenciaHoras() {
        return frecuenciaHoras;
    }

    public ViaAdministracion getVia() {
        return via;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<HorarioMedicacion> getHorarios() {
        return horarios;
    }
}
