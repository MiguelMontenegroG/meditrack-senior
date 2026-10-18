package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
/**
 * Repositorio Spring Data de citas.
 *
 * <p>Todos los filtros (estado, rango de fechas y conjunto de residentes
 * visibles) son opcionales salvo el rango, que el caso de uso exige siempre. Se
 * expone <b>una consulta por combinacion de filtros presentes</b> para no enviar
 * nunca a PostgreSQL un parametro nulo: ese era el origen del error
 * {@code cannot cast type bytea to boolean} del resto del proyecto. El adaptador
 * elige el metodo segun que filtros llegan informados.</p>
 *
 * <p>Cada listado devuelve un {@link Page} para que el {@code total} se calcule
 * con el mismo predicado que el contenido (una sola consulta de conteo, sin un
 * metodo de conteo separado que podria divergir). El orden es por
 * {@code fechaHora} ascendente y se recibe el {@link Pageable} acotado por el
 * caso de uso.</p>
 *
 * <p>Las columnas de fecha son {@code TIMESTAMPTZ}: se comparan como
 * {@code Instant}, sin conversion explicita, para no provocar errores de tipado
 * ({@code timestamp without time zone} vs {@code timestamp with time zone}).</p>
 *
 * <p>No se usa JPQL con {@code IN :lista} vacia: el caso de uso responde pagina
 * vacia cuando no hay residentes visibles y no invoca el repositorio.</p>
 */
public interface CitaJpaRepository extends JpaRepository<CitaEntity, Long> {

    // ---------------------------------------------------------------------
    // Por residente concreto (listado por residente): combinaciones de
    // estado x presencia. El rango es obligatorio en el caso de uso, por eso se
    // separa solo por la presencia de estado.
    // ---------------------------------------------------------------------

    @Query("""
            select c from CitaEntity c
            where c.pacienteId = :pacienteId
              and c.fechaHora >= :desde and c.fechaHora <= :hasta
            order by c.fechaHora asc
            """)
    Page<CitaEntity> listarPorPacienteYRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);

    @Query("""
            select c from CitaEntity c
            where c.pacienteId = :pacienteId
              and c.estado = :estado
              and c.fechaHora >= :desde and c.fechaHora <= :hasta
            order by c.fechaHora asc
            """)
    Page<CitaEntity> listarPorPacienteEstadoYRango(
            @Param("pacienteId") Long pacienteId, @Param("estado") EstadoCita estado,
            @Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);

    // ---------------------------------------------------------------------
    // Calendario por conjunto de residentes visibles: mismo juego de
    // combinaciones (el rango es obligatorio en el caso de uso).
    // ---------------------------------------------------------------------

    @Query("""
            select c from CitaEntity c
            where c.pacienteId in :pacienteIds
              and c.fechaHora >= :desde and c.fechaHora <= :hasta
            order by c.fechaHora asc
            """)
    Page<CitaEntity> listarPorPacientesYRango(
            @Param("pacienteIds") Collection<Long> pacienteIds,
            @Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);

    @Query("""
            select c from CitaEntity c
            where c.pacienteId in :pacienteIds
              and c.estado = :estado
              and c.fechaHora >= :desde and c.fechaHora <= :hasta
            order by c.fechaHora asc
            """)
    Page<CitaEntity> listarPorPacientesEstadoYRango(
            @Param("pacienteIds") Collection<Long> pacienteIds, @Param("estado") EstadoCita estado,
            @Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);
}
