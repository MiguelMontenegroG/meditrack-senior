package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA de la tabla {@code catalogo_medicamento} (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, nombre, presentacion, activo.
 * El nombre es unico sin distinguir mayusculas (indice unico sobre
 * {@code lower(nombre)}).</p>
 */
@Entity
@Table(name = "catalogo_medicamento")
public class MedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected MedicamentoEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
