package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.domain.ViaAdministracion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad JPA de la tabla {@code esquema_medicacion} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, paciente_id, medicamento_id,
 * dosis, frecuencia_horas, via, indicaciones, fecha_inicio, fecha_fin, activo,
 * creado_en, actualizado_en. Los horarios viven en {@code horario_medicacion} y
 * se gestionan en su propia entidad.</p>
 */
@Entity
@Table(name = "esquema_medicacion")
public class EsquemaMedicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    @Column(name = "dosis", nullable = false, length = 50)
    private String dosis;

    @Column(name = "frecuencia_horas", nullable = false)
    private Short frecuenciaHoras;

    @Enumerated(EnumType.STRING)
    @Column(name = "via", nullable = false, length = 30)
    private ViaAdministracion via;

    @Column(name = "indicaciones", columnDefinition = "text")
    private String indicaciones;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected EsquemaMedicacionEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Long medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public int getFrecuenciaHoras() {
        return frecuenciaHoras == null ? 0 : frecuenciaHoras;
    }

    public void setFrecuenciaHoras(int frecuenciaHoras) {
        this.frecuenciaHoras = (short) frecuenciaHoras;
    }

    public ViaAdministracion getVia() {
        return via;
    }

    public void setVia(ViaAdministracion via) {
        this.via = via;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
