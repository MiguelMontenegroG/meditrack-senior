package co.edu.uniquindio.meditrack.reporte.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data de metadatos de reportes generados.
 *
 * <p>El historial de un residente se pagina con una consulta ordenada por
 * creacion descendente; el total lo calcula Spring Data con el mismo predicado,
 * de modo que contenido y total son coherentes. No hay actualizaciones ni
 * borrados: la tabla es append-only.</p>
 */
public interface ReporteGeneradoJpaRepository extends JpaRepository<ReporteGeneradoEntity, Long> {

    @Query("""
            select r from ReporteGeneradoEntity r
            where r.pacienteId = :pacienteId
            order by r.creadoEn desc, r.id desc
            """)
    Page<ReporteGeneradoEntity> listarPorPaciente(
            @Param("pacienteId") Long pacienteId, Pageable pageable);

    @Query("select count(r) from ReporteGeneradoEntity r where r.pacienteId = :pacienteId")
    long contarPorPaciente(@Param("pacienteId") Long pacienteId);
}