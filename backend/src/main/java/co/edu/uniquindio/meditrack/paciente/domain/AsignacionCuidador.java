package co.edu.uniquindio.meditrack.paciente.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Asignacion de un usuario cuidador a un paciente (modelo de dominio, sin Spring
 * ni JPA). Corresponde a la tabla {@code asignacion_cuidador}.
 *
 * <p>Una asignacion vigente tiene {@code hasta == null}. Desasignar cierra la
 * vigencia poniendo {@code hasta = hoy}, de modo que solo puede existir una
 * asignacion vigente por par usuario/paciente (indice unico parcial
 * {@code uq_asicuid_vigente} con {@code WHERE hasta IS NULL}).</p>
 */
public class AsignacionCuidador {

    private final Long id;
    private final Long usuarioId;
    private final Long pacienteId;
    private LocalDate desde;
    private LocalDate hasta;
    private Instant creadoEn;

    public AsignacionCuidador(
            Long id,
            Long usuarioId,
            Long pacienteId,
            LocalDate desde,
            LocalDate hasta,
            Instant creadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.pacienteId = pacienteId;
        this.desde = desde;
        this.hasta = hasta;
        this.creadoEn = creadoEn;
    }

    /**
     * Crea una asignacion vigente (sin id aun) con fecha de inicio hoy.
     */
    public static AsignacionCuidador nueva(Long usuarioId, Long pacienteId, LocalDate hoy) {
        return new AsignacionCuidador(null, usuarioId, pacienteId, hoy, null, null);
    }

    /**
     * Cierra la asignacion dejandola no vigente desde la fecha indicada.
     */
    public void cerrar(LocalDate fechaCierre) {
        if (desde != null && fechaCierre.isBefore(desde)) {
            throw new ConflictoDeNegocioException(
                    "La fecha de cierre no puede ser anterior al inicio de la asignacion");
        }
        this.hasta = fechaCierre;
    }

    /**
     * Indica si la asignacion estaba vigente antes de cerrarse.
     */
    public boolean estaVigente() {
        return hasta == null;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
