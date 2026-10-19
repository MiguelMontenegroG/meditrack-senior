package co.edu.uniquindio.meditrack.bitacora.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Repositorio Spring Data de la bitacora de signos vitales.
 *
 * <p>Los filtros opcionales del rango de fechas se exponen como <b>una consulta
 * por combinacion</b> (sin fechas, solo desde, solo hasta, ambos), para no enviar
 * nunca a PostgreSQL un parametro nulo: ese era el origen del error
 * {@code cannot cast type bytea to boolean} del resto del proyecto. El caso de uso
 * elige el metodo segun que filtros llegan informados.</p>
 *
 * <p>Cada listado devuelve un {@link Page} para que el {@code total} se calcule
 * con el mismo predicado que el contenido (una sola consulta de conteo, sin un
 * metodo de conteo separado que podria divergir). El orden es por
 * {@code fechaHora} descendente (registro mas reciente primero). Las columnas de
 * fecha son {@code TIMESTAMPTZ}: se comparan como {@code Instant}, sin conversion
 * explicita, para no provocar errores de tipado.</p>
 *
 * <p>Solo hay insercion: no hay edicion ni borrado.</p>
 */
public interface RegistroSignosVitalesJpaRepository
        extends JpaRepository<RegistroSignosVitalesEntity, Long> {

    @Query("""
            select r from RegistroSignosVitalesEntity r
            where r.pacienteId = :pacienteId
            order by r.fechaHora desc
            """)
    Page<RegistroSignosVitalesEntity> listarPorPaciente(
            @Param("pacienteId") Long pacienteId, Pageable pageable);

    @Query("""
            select r from RegistroSignosVitalesEntity r
            where r.pacienteId = :pacienteId
              and r.fechaHora >= :desde
            order by r.fechaHora desc
            """)
    Page<RegistroSignosVitalesEntity> listarPorPacienteDesde(
            @Param("pacienteId") Long pacienteId, @Param("desde") Instant desde, Pageable pageable);

    @Query("""
            select r from RegistroSignosVitalesEntity r
            where r.pacienteId = :pacienteId
              and r.fechaHora <= :hasta
            order by r.fechaHora desc
            """)
    Page<RegistroSignosVitalesEntity> listarPorPacienteHasta(
            @Param("pacienteId") Long pacienteId, @Param("hasta") Instant hasta, Pageable pageable);

    @Query("""
            select r from RegistroSignosVitalesEntity r
            where r.pacienteId = :pacienteId
              and r.fechaHora >= :desde and r.fechaHora <= :hasta
            order by r.fechaHora desc
            """)
    Page<RegistroSignosVitalesEntity> listarPorPacienteYRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);

    /**
     * Registro mas reciente de un residente. Se limita a 1 resultado en la base
     * (sin traer toda la bitacora) y se desempata por id para un resultado estable
     * ante fechas iguales.
     */
    @Query("""
            select r from RegistroSignosVitalesEntity r
            where r.pacienteId = :pacienteId
            order by r.fechaHora desc, r.id desc
            """)
    Page<RegistroSignosVitalesEntity> listarUltimo(
            @Param("pacienteId") Long pacienteId, Pageable pageable);

    /** Atajo tipado del ultimo registro; el adaptador usa {@link #listarUltimo}. */
    default Optional<RegistroSignosVitalesEntity> buscarUltimo(Long pacienteId) {
        return listarUltimo(pacienteId, Pageable.ofSize(1)).stream().findFirst();
    }
}
