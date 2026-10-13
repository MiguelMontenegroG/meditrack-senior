package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio Spring Data de tomas (historial clinico).
 *
 * <p>El historial de un residente se obtiene uniendo {@code registro_toma} con
 * {@code esquema_medicacion} (por {@code esquema_id}) para filtrar por
 * residente.</p>
 *
 * <p>Los filtros opcionales (esquema y rango de fechas) se resuelven con
 * <b>una consulta por combinacion de filtros presentes</b>, en lugar de
 * sustituir los extremos ausentes por instantes centinela ({@code EPOCH} /
 * {@code 9999-12-31}) o comparar parametros nulos en la consulta. Ambos trucos
 * fallaban en PostgreSQL (comparar contra {@code EPOCH} filtra mal las fechas
 * reales; los parametros nulos daban {@code cannot cast type bytea to
 * timestamp}). El adaptador elige el metodo segun que filtros llegan.</p>
 */
public interface RegistroTomaJpaRepository extends JpaRepository<RegistroTomaEntity, Long> {

    boolean existsByEsquemaIdAndFechaHoraProgramada(Long esquemaId, Instant fechaHoraProgramada);

    List<RegistroTomaEntity> findByEsquemaIdAndFechaHoraProgramadaBetween(
            Long esquemaId, Instant desde, Instant hasta);

    List<RegistroTomaEntity> findByEsquemaIdInAndFechaHoraProgramadaBetween(
            List<Long> esquemaIds, Instant desde, Instant hasta);

    // ---------------------------------------------------------------------
    // Historial: una consulta por combinacion de filtros. El predicado de
    // residente (subconsulta sobre esquema_medicacion) es comun a todos.
    // ---------------------------------------------------------------------

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialSinFiltros(
            @Param("pacienteId") Long pacienteId, Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
            """)
    long contarHistorialSinFiltros(@Param("pacienteId") Long pacienteId);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialPorEsquema(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
            """)
    long contarHistorialPorEsquema(
            @Param("pacienteId") Long pacienteId, @Param("esquemaId") Long esquemaId);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada >= :desde
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialDesde(
            @Param("pacienteId") Long pacienteId, @Param("desde") Instant desde, Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada >= :desde
            """)
    long contarHistorialDesde(@Param("pacienteId") Long pacienteId, @Param("desde") Instant desde);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada <= :hasta
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialHasta(
            @Param("pacienteId") Long pacienteId, @Param("hasta") Instant hasta, Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada <= :hasta
            """)
    long contarHistorialHasta(@Param("pacienteId") Long pacienteId, @Param("hasta") Instant hasta);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada >= :desde
              and t.fechaHoraProgramada <= :hasta
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialPorRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada >= :desde
              and t.fechaHoraProgramada <= :hasta
            """)
    long contarHistorialPorRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
              and t.fechaHoraProgramada >= :desde
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialPorEsquemaDesde(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            @Param("desde") Instant desde,
            Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
              and t.fechaHoraProgramada >= :desde
            """)
    long contarHistorialPorEsquemaDesde(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            @Param("desde") Instant desde);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
              and t.fechaHoraProgramada <= :hasta
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialPorEsquemaHasta(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            @Param("hasta") Instant hasta,
            Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
              and t.fechaHoraProgramada <= :hasta
            """)
    long contarHistorialPorEsquemaHasta(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            @Param("hasta") Instant hasta);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
              and t.fechaHoraProgramada >= :desde
              and t.fechaHoraProgramada <= :hasta
            order by t.fechaHoraProgramada desc
            """)
    Page<RegistroTomaEntity> historialPorEsquemaYRango(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            Pageable pageable);

    @Query("""
            select count(t) from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.esquemaId = :esquemaId
              and t.fechaHoraProgramada >= :desde
              and t.fechaHoraProgramada <= :hasta
            """)
    long contarHistorialPorEsquemaYRango(
            @Param("pacienteId") Long pacienteId,
            @Param("esquemaId") Long esquemaId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);
}
