package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data de esquemas de medicacion.
 *
 * <p>Los filtros opcionales ({@code activo} y {@code vigente}) se resuelven con
 * <b>una consulta por combinacion de filtros presentes</b> en lugar de envolver
 * los parametros en {@code cast(... as string)} y compararlos contra nulo. Esa
 * otra tecnica fallaba en PostgreSQL con {@code cannot cast type bytea to
 * boolean} cuando el parametro llegaba nulo. Con consultas separadas nunca se
 * envia un parametro nulo. La vigencia se calcula con la fecha {@code :hoy}
 * recibida (nunca {@code current_date}), de modo que sea determinista y
 * testeable.</p>
 */
public interface EsquemaMedicacionJpaRepository extends JpaRepository<EsquemaMedicacionEntity, Long> {

    // --- Listado por paciente: una consulta por combinacion de filtros. ---

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.activo = :activo
                       and e.fechaInicio <= :hoy
              and (e.fechaFin is null or e.fechaFin >= :hoy)
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> listarActivoVigente(
            @Param("pacienteId") Long pacienteId, @Param("activo") boolean activo, @Param("hoy") LocalDate hoy);
    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.activo = :activo
              and (e.fechaInicio > :hoy or (e.fechaFin is not null and e.fechaFin < :hoy))
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> listarActivoNoVigente(
            @Param("pacienteId") Long pacienteId, @Param("activo") boolean activo, @Param("hoy") LocalDate hoy);

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.activo = :activo
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> listarPorActivo(
            @Param("pacienteId") Long pacienteId, @Param("activo") boolean activo);

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.fechaInicio <= :hoy
              and (e.fechaFin is null or e.fechaFin >= :hoy)
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> listarVigente(
            @Param("pacienteId") Long pacienteId, @Param("hoy") LocalDate hoy);

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and (e.fechaInicio > :hoy or (e.fechaFin is not null and e.fechaFin < :hoy))
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> listarNoVigente(
            @Param("pacienteId") Long pacienteId, @Param("hoy") LocalDate hoy);

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> listarTodos(@Param("pacienteId") Long pacienteId);

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.activo = true
              and e.fechaInicio <= :hoy
              and (e.fechaFin is null or e.fechaFin >= :hoy)
            order by e.id
            """)
    List<EsquemaMedicacionEntity> listarUtilizables(
            @Param("pacienteId") Long pacienteId, @Param("hoy") LocalDate hoy);

    /**
     * Cuenta esquemas activos del mismo medicamento y residente cuya vigencia se
     * solapa con el rango {@code [fechaInicio, fechaFin]} (con {@code fechaFin}
     * nula = vigencia abierta, representada por una fecha maxima), excluyendo un
     * id dado ({@code 0} cuando no aplica). Solapamiento:
     * {@code inicioNuevo <= finExistente AND inicioExistente <= finNuevo}.
     */
    @Query("""
            select count(e) from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.medicamentoId = :medicamentoId
              and e.activo = true
              and e.id <> :idExcluido
              and (e.fechaFin is null or :fechaInicio <= e.fechaFin)
              and e.fechaInicio <= :fechaFin
            """)
    long contarSolapamientos(
            @Param("pacienteId") Long pacienteId,
            @Param("medicamentoId") Long medicamentoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idExcluido") Long idExcluido);
}
