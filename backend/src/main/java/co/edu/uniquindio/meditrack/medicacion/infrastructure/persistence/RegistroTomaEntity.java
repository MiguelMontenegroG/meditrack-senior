package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Entidad JPA de la tabla {@code registro_toma} (migracion V2).
 *
 * <p>Mapea id, esquema_id, registrado_por, fecha_hora_programada,
 * fecha_hora_registro, estado y observaciones. Es historial clinico: solo se
 * inserta (append-only).</p>
 */
@Entity
@Table(name = "registro_toma")
public class RegistroTomaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "esquema_id", nullable = false)
    private Long esquemaId;

    @Column(name = "registrado_por", nullable = false)
    private Long registradoPor;

    @Column(name = "fecha_hora_programada", nullable = false)
    private Instant fechaHoraProgramada;

    @Column(name = "fecha_hora_registro")
    private Instant fechaHoraRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoToma estado;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    protected RegistroTomaEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public Long getEsquemaId() {
        return esquemaId;
    }

    public void setEsquemaId(Long esquemaId) {
        this.esquemaId = esquemaId;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Long registradoPor) {
        this.registradoPor = registradoPor;
    }

    public Instant getFechaHoraProgramada() {
        return fechaHoraProgramada;
    }

    public void setFechaHoraProgramada(Instant fechaHoraProgramada) {
        this.fechaHoraProgramada = fechaHoraProgramada;
    }

    public Instant getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public void setFechaHoraRegistro(Instant fechaHoraRegistro) {
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    public EstadoToma getEstado() {
        return estado;
    }

    public void setEstado(EstadoToma estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
