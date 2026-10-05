package co.edu.uniquindio.meditrack.usuario.infrastructure.persistence;

import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA de la tabla {@code rol} (migracion V2).
 *
 * <p>Los roles son fijos (los siembra V3) y solo se leen.</p>
 */
@Entity
@Table(name = "rol")
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private RolNombre nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    protected RolEntity() {
        // Requerido por JPA.
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
