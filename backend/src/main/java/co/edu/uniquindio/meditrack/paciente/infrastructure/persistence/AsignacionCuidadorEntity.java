package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad JPA de la tabla {@code asignacion_cuidador} (migracion V2).
 *
 * <p>{@code hasta == null} indica una asignacion vigente.</p>
 */
@Entity
@Table(name = "asignacion_cuidador")
public class AsignacionCuidadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "desde", nullable = false)
    private LocalDate desde;

    @Column(name = "hasta")
    private LocalDate hasta;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected AsignacionCuidadorEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
