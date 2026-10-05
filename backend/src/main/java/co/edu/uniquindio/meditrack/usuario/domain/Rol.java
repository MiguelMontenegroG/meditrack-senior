package co.edu.uniquindio.meditrack.usuario.domain;

/**
 * Rol asignable a un usuario. Modelo de dominio minimo: identifica el rol por
 * su nombre; la descripcion es informativa.
 */
public class Rol {

    private final Long id;
    private final RolNombre nombre;
    private final String descripcion;

    public Rol(Long id, RolNombre nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public RolNombre getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
