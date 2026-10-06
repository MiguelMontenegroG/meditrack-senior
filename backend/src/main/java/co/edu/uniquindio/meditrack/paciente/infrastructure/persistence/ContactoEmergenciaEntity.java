package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entidad JPA de la tabla {@code contacto_emergencia} (migracion V2).
 *
 * <p>Guarda {@code paciente_id} como columna simple (sin asociacion JPA) para
 * evitar cargas en cascada y mantener el adaptador explicito.</p>
 */
@Entity
@Table(name = "contacto_emergencia")
public class ContactoEmergenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "parentesco", length = 60)
    private String parentesco;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected ContactoEmergenciaEntity() {
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
