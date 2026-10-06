package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
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
 * Entidad JPA de la tabla {@code paciente} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, documento, nombres, apellidos,
 * fecha_nacimiento, sexo, habitacion, situacion_clinica, fecha_ingreso, activo,
 * creado_en, actualizado_en. {@code creado_en} y {@code actualizado_en} los
 * mantiene la aplicacion (decision D7).</p>
 */
@Entity
@Table(name = "paciente")
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "documento", nullable = false, unique = true, length = 30)
    private String documento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 20)
    private Sexo sexo;

    @Column(name = "habitacion", length = 20)
    private String habitacion;

    @Column(name = "situacion_clinica", columnDefinition = "text")
    private String situacionClinica;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected PacienteEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public String getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(String habitacion) {
        this.habitacion = habitacion;
    }

    public String getSituacionClinica() {
        return situacionClinica;
    }

    public void setSituacionClinica(String situacionClinica) {
        this.situacionClinica = situacionClinica;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
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
