package co.edu.uniquindio.meditrack.alerta.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * Repositorio Spring Data de alertas.
 *
 * <p>Los filtros opcionales (estado y tipo) y el alcance (residente concreto o
 * conjunto de residentes visibles) se exponen como <b>una consulta por
 * combinacion</b> para no enviar nunca a PostgreSQL un parametro nulo. Cada listado
 * devuelve un {@link Page} para que el total se calcule con el mismo predicado que
 * el contenido, ordenado por {@code creadoEn} descendente (lo mas reciente primero).
 * No se usa JPQL con {@code IN :lista} vacia: el caso de uso responde pagina vacia
 * sin invocar el repositorio.</p>
 */
public interface AlertaJpaRepository extends JpaRepository<AlertaEntity, Long> {

    // ------------------------------------------------------------------
    // Conjunto de residentes visibles.
    // ------------------------------------------------------------------

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId in :pacienteIds
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacientes(
            @Param("pacienteIds") Collection<Long> pacienteIds, Pageable pageable);

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId in :pacienteIds
              and a.estado = :estado
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacientesYEstado(
            @Param("pacienteIds") Collection<Long> pacienteIds,
            @Param("estado") EstadoAlerta estado, Pageable pageable);

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId in :pacienteIds
              and a.tipo = :tipo
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacientesYTipo(
            @Param("pacienteIds") Collection<Long> pacienteIds,
            @Param("tipo") TipoAlerta tipo, Pageable pageable);

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId in :pacienteIds
              and a.estado = :estado
              and a.tipo = :tipo
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacientesEstadoYTipo(
            @Param("pacienteIds") Collection<Long> pacienteIds,
            @Param("estado") EstadoAlerta estado, @Param("tipo") TipoAlerta tipo, Pageable pageable);

    // ------------------------------------------------------------------
    // Residente concreto.
    // ------------------------------------------------------------------

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId = :pacienteId
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPaciente(@Param("pacienteId") Long pacienteId, Pageable pageable);

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId = :pacienteId
              and a.estado = :estado
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacienteYEstado(
            @Param("pacienteId") Long pacienteId,
            @Param("estado") EstadoAlerta estado, Pageable pageable);

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId = :pacienteId
              and a.tipo = :tipo
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacienteYTipo(
            @Param("pacienteId") Long pacienteId,
            @Param("tipo") TipoAlerta tipo, Pageable pageable);

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId = :pacienteId
              and a.estado = :estado
              and a.tipo = :tipo
            order by a.creadoEn desc, a.id desc
            """)
    Page<AlertaEntity> listarPorPacienteEstadoYTipo(
            @Param("pacienteId") Long pacienteId,
            @Param("estado") EstadoAlerta estado, @Param("tipo") TipoAlerta tipo, Pageable pageable);
}
