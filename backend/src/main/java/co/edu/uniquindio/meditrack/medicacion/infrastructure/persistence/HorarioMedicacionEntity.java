package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

/**
 * Entidad JPA de la tabla {@code horario_medicacion} (migracion V2).
 *
 * <p>Mapea id, esquema_id y hora. El indice unico {@code uq_horario_esquema_hora}
 * garantiza que no se repita la misma hora en un esquema.</p>
 */
@Entity
@Table(name = "horario_medicacion")
public class HorarioMedicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "esquema_id", nullable = false)
    private Long esquemaId;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    protected HorarioMedicacionEntity() {
        // Requerido por JPA.
    }

    public HorarioMedicacionEntity(Long esquemaId, LocalTime hora) {
        this.esquemaId = esquemaId;
        this.hora = hora;
    }

    public Long getId() {
        return id;
    }

    public Long getEsquemaId() {
        return esquemaId;
    }

    public void setEsquemaId(Long esquemaId) {
        this.esquemaId = esquemaId;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
}
