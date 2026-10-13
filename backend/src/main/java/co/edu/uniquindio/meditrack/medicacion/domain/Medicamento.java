package co.edu.uniquindio.meditrack.medicacion.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

/**
 * Medicamento del catalogo (modelo de dominio, sin Spring ni JPA).
 *
 * <p>El nombre es unico sin distinguir mayusculas (lo garantiza un indice unico
 * sobre {@code lower(nombre)} en V2). El borrado es logico: nunca se elimina
 * fisicamente, solo se marca {@code activo = false}.</p>
 */
public class Medicamento {

    private final Long id;
    private String nombre;
    private String presentacion;
    private boolean activo;

    public Medicamento(Long id, String nombre, String presentacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.activo = activo;
    }

    /** Crea un medicamento nuevo (sin id aun) listo para persistir. */
    public static Medicamento nuevo(String nombre, String presentacion) {
        validarNombre(nombre);
        return new Medicamento(null, nombre.trim(), presentacion, true);
    }

    /** Actualiza los datos editables del medicamento. */
    public void actualizar(String nombre, String presentacion) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
        this.presentacion = presentacion;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ConflictoDeNegocioException("El nombre del medicamento es obligatorio");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public boolean isActivo() {
        return activo;
    }
}
