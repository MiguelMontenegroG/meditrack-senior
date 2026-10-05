package co.edu.uniquindio.meditrack.shared.infrastructure.persistence;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
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
 * Entidad JPA de la tabla {@code auditoria_acceso} (migracion V2).
 *
 * <p>Tabla de solo insercion: no se actualiza ni se borra. El campo detalle
 * nunca debe contener contrasenas ni tokens.</p>
 */
@Entity
@Table(name = "auditoria_acceso")
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 20)
    private AccionAuditoria accion;

    @Column(name = "entidad", nullable = false, length = 60)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private Instant fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private ResultadoAuditoria resultado;

    @Column(name = "detalle")
    private String detalle;

    protected AuditoriaEntity() {
        // Requerido por JPA.
    }

    public AuditoriaEntity(
            Long usuarioId,
            AccionAuditoria accion,
            String entidad,
            Long entidadId,
            ResultadoAuditoria resultado,
            String detalle) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.detalle = detalle;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public AccionAuditoria getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public ResultadoAuditoria getResultado() {
        return resultado;
    }

    public String getDetalle() {
        return detalle;
    }
}
