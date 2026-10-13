package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio Spring Data de horarios de medicacion.
 */
public interface HorarioMedicacionJpaRepository extends JpaRepository<HorarioMedicacionEntity, Long> {

    List<HorarioMedicacionEntity> findByEsquemaIdOrderByHora(Long esquemaId);

    /**
     * Elimina los horarios de un esquema. Es una consulta de borrado masivo, por
     * lo que requiere {@code @Modifying}; el {@code flush} se fuerza desde el
     * adaptador antes de insertar los nuevos horarios para respetar la
     * restriccion unica {@code (esquema_id, hora)}.
     */
    @Modifying
    @Query("delete from HorarioMedicacionEntity h where h.esquemaId = :esquemaId")
    void deleteByEsquemaId(@Param("esquemaId") Long esquemaId);

    List<HorarioMedicacionEntity> findByEsquemaIdIn(List<Long> esquemaIds);
}
