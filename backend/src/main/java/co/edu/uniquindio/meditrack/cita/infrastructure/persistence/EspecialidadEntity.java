package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA de la tabla {@code catalogo_especialidad} (migracion V2/V3), en
 * modo solo lectura para el modulo de citas.
 *
 * <p>El esquema solo define {@code id} y {@code nombre}: no hay columna de
 * estado, por lo que no existe el concepto de "especialidad activa".</p>
 */
@Entity
@Table(name = "catalogo_especialidad")
public class EspecialidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    protected EspecialidadEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
