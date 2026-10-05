package co.edu.uniquindio.meditrack.usuario.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entidad JPA de la tabla {@code usuario} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, rol_id, nombre_completo, correo,
 * hash_contrasena, telefono, activo, ultimo_acceso_en, creado_en, actualizado_en.</p>
 *
 * <p>{@code creado_en} y {@code actualizado_en} los mantiene la aplicacion:
 * {@code @CreationTimestamp} en el alta (equivalente al DEFAULT now()) y
 * {@code @UpdateTimestamp} en cada actualizacion (decision D7 del modelo).</p>
 */
@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private RolEntity rol;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "hash_contrasena", nullable = false, length = 255)
    private String hashContrasena;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "ultimo_acceso_en")
    private Instant ultimoAccesoEn;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected UsuarioEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public RolEntity getRol() {
        return rol;
    }

    public void setRol(RolEntity rol) {
        this.rol = rol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public void setHashContrasena(String hashContrasena) {
        this.hashContrasena = hashContrasena;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getUltimoAccesoEn() {
        return ultimoAccesoEn;
    }

    public void setUltimoAccesoEn(Instant ultimoAccesoEn) {
        this.ultimoAccesoEn = ultimoAccesoEn;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
