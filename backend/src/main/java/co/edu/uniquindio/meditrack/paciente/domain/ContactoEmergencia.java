package co.edu.uniquindio.meditrack.paciente.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

import java.time.Instant;

/**
 * Contacto de emergencia de un paciente (modelo de dominio, sin Spring ni JPA).
 *
 * <p>Es una entidad dependiente del paciente: se elimina en cascada con el
 * paciente y puede borrarse por completo (a diferencia del paciente, que solo se
 * desactiva). Solo puede haber un contacto principal por paciente, regla
 * reforzada por el indice unico parcial {@code uq_contacto_principal}.</p>
 */
public class ContactoEmergencia {

    private final Long id;
    private final Long pacienteId;
    private String nombre;
    private String parentesco;
    private String telefono;
    private boolean principal;
    private Instant creadoEn;

    public ContactoEmergencia(
            Long id,
            Long pacienteId,
            String nombre,
            String parentesco,
            String telefono,
            boolean principal,
            Instant creadoEn) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.nombre = nombre;
        this.parentesco = parentesco;
        this.telefono = telefono;
        this.principal = principal;
        this.creadoEn = creadoEn;
    }

    /**
     * Crea un contacto nuevo (sin id aun) listo para persistir.
     */
    public static ContactoEmergencia nuevo(
            Long pacienteId,
            String nombre,
            String parentesco,
            String telefono,
            boolean principal) {
        validar(nombre, telefono);
        return new ContactoEmergencia(null, pacienteId, nombre.trim(), parentesco, telefono.trim(), principal, null);
    }

    /**
     * Actualiza los datos editables del contacto.
     */
    public void actualizar(String nombre, String parentesco, String telefono) {
        validar(nombre, telefono);
        this.nombre = nombre.trim();
        this.parentesco = parentesco;
        this.telefono = telefono.trim();
    }

    /**
     * Marca este contacto como principal.
     */
    public void marcarPrincipal() {
        this.principal = true;
    }

    /**
     * Quita la marca de principal (al reemplazarlo por otro contacto principal).
     */
    public void quitarPrincipal() {
        this.principal = false;
    }

    private static void validar(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new ConflictoDeNegocioException("El nombre del contacto es obligatorio");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ConflictoDeNegocioException("El telefono del contacto es obligatorio");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getParentesco() {
        return parentesco;
    }

    public String getTelefono() {
        return telefono;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
