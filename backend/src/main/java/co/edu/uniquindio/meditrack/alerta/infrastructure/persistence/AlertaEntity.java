package co.edu.uniquindio.meditrack.alerta.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.OrigenAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.SeveridadAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entidad JPA de la tabla {@code alerta} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, paciente_id, tipo, severidad,
 * mensaje, origen, signos_id, toma_id, cita_id, estado, creado_en, revisada_en y
 * revisada_por. La unica actualizacion permitida es la atencion (estado y datos de
 * revision); no hay borrado. Las columnas de fuente son nullable y a lo sumo una
 * viene informada ({@code chk_alerta_fuente_unica}).</p>
 */
@Entity
@Table(name = "alerta")
public class AlertaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "severidad", nullable = false, length = 10)
    private SeveridadAlerta severidad;

    @Column(name = "mensaje", nullable = false, columnDefinition = "text")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 20)
    private OrigenAlerta origen;

    @Column(name = "signos_id")
    private Long signosId;

    @Column(name = "toma_id")
    private Long tomaId;

    @Column(name = "cita_id")
    private Long citaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAlerta estado;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "revisada_en")
    private Instant revisadaEn;

    @Column(name = "revisada_por")
    private Long revisadaPor;

    protected AlertaEntity() {
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

    public TipoAlerta getTipo() {
        return tipo;
    }

    public void setTipo(TipoAlerta tipo) {
        this.tipo = tipo;
    }

    public SeveridadAlerta getSeveridad() {
        return severidad;
    }

    public void setSeveridad(SeveridadAlerta severidad) {
        this.severidad = severidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public OrigenAlerta getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenAlerta origen) {
        this.origen = origen;
    }

    public Long getSignosId() {
        return signosId;
    }

    public void setSignosId(Long signosId) {
        this.signosId = signosId;
    }

    public Long getTomaId() {
        return tomaId;
    }

    public void setTomaId(Long tomaId) {
        this.tomaId = tomaId;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlerta estado) {
        this.estado = estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getRevisadaEn() {
        return revisadaEn;
    }

    public void setRevisadaEn(Instant revisadaEn) {
        this.revisadaEn = revisadaEn;
    }

    public Long getRevisadaPor() {
        return revisadaPor;
    }

    public void setRevisadaPor(Long revisadaPor) {
        this.revisadaPor = revisadaPor;
    }
}
