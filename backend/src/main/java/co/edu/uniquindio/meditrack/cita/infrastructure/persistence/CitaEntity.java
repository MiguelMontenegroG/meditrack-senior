package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
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

/**
 * Entidad JPA de la tabla {@code cita} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, paciente_id, especialidad_id,
 * registrada_por, profesional, fecha_hora, lugar, motivo, estado, creado_en,
 * actualizado_en. {@code especialidad_id} es nullable en el esquema (una cita
 * puede registrarse sin especialidad, por ejemplo una visita general).</p>
 */
@Entity
@Table(name = "cita")
public class CitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "especialidad_id")
    private Long especialidadId;

    @Column(name = "registrada_por", nullable = false)
    private Long registradaPor;

    @Column(name = "profesional", length = 150)
    private String profesional;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "lugar", length = 150)
    private String lugar;

    @Column(name = "motivo", columnDefinition = "text")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected CitaEntity() {
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

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public Long getRegistradaPor() {
        return registradaPor;
    }

    public void setRegistradaPor(Long registradaPor) {
        this.registradaPor = registradaPor;
    }

    public String getProfesional() {
        return profesional;
    }

    public void setProfesional(String profesional) {
        this.profesional = profesional;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
