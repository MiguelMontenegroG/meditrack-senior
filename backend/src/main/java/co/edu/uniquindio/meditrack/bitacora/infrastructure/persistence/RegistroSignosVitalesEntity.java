package co.edu.uniquindio.meditrack.bitacora.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA de la tabla {@code registro_signos_vitales} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, paciente_id, registrado_por,
 * fecha_hora (TIMESTAMPTZ), presion_sistolica (SMALLINT), presion_diastolica
 * (SMALLINT), glucosa_mg_dl (SMALLINT), temperatura_c (NUMERIC(4,1)),
 * observaciones (TEXT) y creado_en (TIMESTAMPTZ). Todas las columnas clinicas son
 * nullable en el esquema. La entidad solo se inserta: no hay edicion ni borrado,
 * por eso {@code creado_en} es {@code updatable = false}.</p>
 */
@Entity
@Table(name = "registro_signos_vitales")
public class RegistroSignosVitalesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "registrado_por", nullable = false)
    private Long registradoPor;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "presion_sistolica")
    private Integer presionSistolica;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "presion_diastolica")
    private Integer presionDiastolica;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "glucosa_mg_dl")
    private Integer glucosaMgDl;

    @Column(name = "temperatura_c", precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected RegistroSignosVitalesEntity() {
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

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Long registradoPor) {
        this.registradoPor = registradoPor;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getPresionSistolica() {
        return presionSistolica;
    }

    public void setPresionSistolica(Integer presionSistolica) {
        this.presionSistolica = presionSistolica;
    }

    public Integer getPresionDiastolica() {
        return presionDiastolica;
    }

    public void setPresionDiastolica(Integer presionDiastolica) {
        this.presionDiastolica = presionDiastolica;
    }

    public Integer getGlucosaMgDl() {
        return glucosaMgDl;
    }

    public void setGlucosaMgDl(Integer glucosaMgDl) {
        this.glucosaMgDl = glucosaMgDl;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(BigDecimal temperaturaC) {
        this.temperaturaC = temperaturaC;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
