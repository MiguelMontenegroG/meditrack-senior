package co.edu.uniquindio.meditrack.paciente.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

/**
 * Perfil del adulto mayor residente (modelo de dominio, sin Spring ni JPA).
 *
 * <p>Concentra las reglas de negocio del perfil: validacion de datos basicos, la
 * fecha de nacimiento no futura (validada en la aplicacion, no hay CHECK con
 * {@code current_date}) y el borrado logico mediante {@code activo}. Nunca se
 * borra fisicamente un paciente.</p>
 */
public class Paciente {

    private final Long id;
    private String documento;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String habitacion;
    private String situacionClinica;
    private LocalDate fechaIngreso;
    private boolean activo;
    private Instant creadoEn;
    private Instant actualizadoEn;

    public Paciente(
            Long id,
            String documento,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            Sexo sexo,
            String habitacion,
            String situacionClinica,
            LocalDate fechaIngreso,
            boolean activo,
            Instant creadoEn,
            Instant actualizadoEn) {
        this.id = id;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.habitacion = habitacion;
        this.situacionClinica = situacionClinica;
        this.fechaIngreso = fechaIngreso;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    /**
     * Crea un paciente nuevo (sin id aun) listo para persistir.
     */
    public static Paciente nuevo(
            String documento,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            Sexo sexo,
            String habitacion,
            String situacionClinica,
            LocalDate fechaIngreso,
            LocalDate hoy) {
        validarDatos(documento, nombres, apellidos, fechaNacimiento, sexo, hoy);
        return new Paciente(
                null,
                documento.trim(),
                nombres.trim(),
                apellidos.trim(),
                fechaNacimiento,
                sexo,
                habitacion,
                situacionClinica,
                fechaIngreso != null ? fechaIngreso : hoy,
                true,
                null,
                null);
    }

    /**
     * Actualiza los datos editables del perfil.
     */
    public void actualizar(
            String documento,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            Sexo sexo,
            String habitacion,
            String situacionClinica,
            LocalDate fechaIngreso,
            LocalDate hoy) {
        validarDatos(documento, nombres, apellidos, fechaNacimiento, sexo, hoy);
        this.documento = documento.trim();
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.habitacion = habitacion;
        this.situacionClinica = situacionClinica;
        if (fechaIngreso != null) {
            this.fechaIngreso = fechaIngreso;
        }
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    /**
     * Edad calculada en anios cumplidos a la fecha indicada.
     */
    public int edad(LocalDate hoy) {
        return Period.between(fechaNacimiento, hoy).getYears();
    }

    /**
     * Nombre completo para listados (apellidos, nombres).
     */
    public String nombreCompleto() {
        return apellidos + ", " + nombres;
    }

    private static void validarDatos(
            String documento,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            Sexo sexo,
            LocalDate hoy) {
        if (documento == null || documento.isBlank()) {
            throw new ConflictoDeNegocioException("El documento es obligatorio");
        }
        if (nombres == null || nombres.isBlank()) {
            throw new ConflictoDeNegocioException("Los nombres son obligatorios");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new ConflictoDeNegocioException("Los apellidos son obligatorios");
        }
        if (fechaNacimiento == null) {
            throw new ConflictoDeNegocioException("La fecha de nacimiento es obligatoria");
        }
        if (fechaNacimiento.isAfter(hoy)) {
            throw new ConflictoDeNegocioException("La fecha de nacimiento no puede ser futura");
        }
        if (sexo == null) {
            throw new ConflictoDeNegocioException("El sexo es obligatorio");
        }
    }

    public Long getId() {
        return id;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public String getHabitacion() {
        return habitacion;
    }

    public String getSituacionClinica() {
        return situacionClinica;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
