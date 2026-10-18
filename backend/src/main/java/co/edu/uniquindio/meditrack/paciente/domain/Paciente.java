package co.edu.uniquindio.meditrack.paciente.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Perfil del adulto mayor residente (modelo de dominio, sin Spring ni JPA).
 *
 * <p>Concentra las reglas de negocio del perfil: validacion de datos basicos, la
 * fecha de nacimiento no futura (validada en la aplicacion, no hay CHECK con
 * {@code current_date}), la fecha de ingreso no futura, la coherencia del
 * perfil clinico (un residente activo exige situacion clinica; la habitacion
 * es opcional porque el producto tambien cubre centros de dia) y el borrado
 * logico mediante {@code activo}. Nunca se borra fisicamente un paciente.</p>
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
     * Crea un paciente nuevo (sin id aun) listo para persistir. El residente
     * nace activo, por lo que exige la situacion clinica (la habitacion es
     * opcional: el producto incluye centros de dia sin habitaciones).
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
        validarFechaIngreso(fechaIngreso, hoy);
        validarPerfilClinico(situacionClinica);
        return new Paciente(
                null,
                documento.trim(),
                nombres.trim(),
                apellidos.trim(),
                fechaNacimiento,
                sexo,
                normalizar(habitacion),
                normalizar(situacionClinica),
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
        validarFechaIngreso(fechaIngreso, hoy);
        validarPerfilClinico(situacionClinica);
        this.documento = documento.trim();
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.habitacion = normalizar(habitacion);
        this.situacionClinica = normalizar(situacionClinica);
        if (fechaIngreso != null) {
            this.fechaIngreso = fechaIngreso;
        }
    }

    /** Activa el residente (solo si el perfil clinico esta completo). */
    public void activar() {
        validarPerfilClinico(situacionClinica);
        this.activo = true;
    }

    /**
     * Desactiva el residente (borrado logico). El motivo es obligatorio: se
     * registra en la auditoria de acceso (Ley 1581) y permite explicar por que
     * se retiro al residente del cuidado activo.
     */
    public void desactivar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ValidacionDeNegocioException("El motivo de la desactivacion es obligatorio");
        }
        this.activo = false;
    }

    /**
     * Datos clinicos que faltan para que el perfil este completo. Se usa para
     * explicar en la respuesta por que un residente no puede activarse. La
     * habitacion no cuenta: es opcional (centros de dia).
     */
    public List<String> datosClinicosPendientes() {
        List<String> faltantes = new ArrayList<>();
        if (situacionClinica == null || situacionClinica.isBlank()) {
            faltantes.add("situacionClinica");
        }
        return faltantes;
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

    /**
     * La fecha de ingreso es opcional (por defecto hoy), pero si se envia no
     * puede ser futura: un residente no puede ingresar manana.
     */
    private static void validarFechaIngreso(LocalDate fechaIngreso, LocalDate hoy) {
        if (fechaIngreso != null && fechaIngreso.isAfter(hoy)) {
            throw new ValidacionDeNegocioException("La fecha de ingreso no puede ser futura");
        }
    }

    /**
     * Un residente que va a quedar activo debe tener la situacion clinica. La
     * regla se evalua tanto al crear como al editar; la habitacion no se exige
     * porque el producto cubre tambien centros de dia sin habitaciones.
     */
    private static void validarPerfilClinico(String situacionClinica) {
        if (situacionClinica == null || situacionClinica.isBlank()) {
            throw new ValidacionDeNegocioException(
                    "Un residente activo debe tener la situacion clinica");
        }
    }

    /** Recorta y convierte a nulo los textos vacios. */
    private static String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
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
