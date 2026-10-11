package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data de pacientes con consultas de alcance.
 *
 * <p>El filtrado por rol se resuelve con <b>consultas separadas por alcance</b>
 * en lugar de pasar el rol como parametro: comparar un parametro contra
 * literales de enum dentro de un {@code OR} con subconsultas {@code EXISTS}
 * deja a Hibernate 6 sin poder determinar el {@code ValueMapping} del parametro
 * y lanza {@code ConversionException} en tiempo de ejecucion. El caso de uso
 * elige el metodo segun el rol del usuario.</p>
 *
 * <p>Los parametros opcionales ({@code :texto} y {@code :activo}) se envuelven
 * en {@code cast(... as string)} / {@code cast(... as boolean)}: sin el cast,
 * cuando el valor llega nulo PostgreSQL no puede inferir el tipo y falla con
 * {@code function lower(bytea) does not exist} o equivalentes.</p>
 *
 * <p>Todas las consultas comparten el mismo predicado de busqueda y de estado,
 * de modo que el listado y su conteo son coherentes.</p>
 */
public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    // ---------------------------------------------------------------------
    // Alcance ADMINISTRADOR: ve todos los pacientes.
    // ---------------------------------------------------------------------

    @Query("""
            select p from PacienteEntity p
            where (cast(:texto as string) is null
                   or lower(p.nombres)   like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.apellidos) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.documento) like lower(concat('%', cast(:texto as string), '%')))
              and (cast(:activo as boolean) is null or p.activo = cast(:activo as boolean))
            """)
    Page<PacienteEntity> listarTodos(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where (cast(:texto as string) is null
                   or lower(p.nombres)   like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.apellidos) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.documento) like lower(concat('%', cast(:texto as string), '%')))
              and (cast(:activo as boolean) is null or p.activo = cast(:activo as boolean))
            """)
    long contarTodos(@Param("texto") String texto, @Param("activo") Boolean activo);

    // ---------------------------------------------------------------------
    // Alcance CUIDADOR_ENFERMERO: solo pacientes con asignacion vigente.
    // ---------------------------------------------------------------------

    @Query("""
            select p from PacienteEntity p
            where (cast(:texto as string) is null
                   or lower(p.nombres)   like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.apellidos) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.documento) like lower(concat('%', cast(:texto as string), '%')))
              and (cast(:activo as boolean) is null or p.activo = cast(:activo as boolean))
              and exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
            """)
    Page<PacienteEntity> listarPorCuidador(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where (cast(:texto as string) is null
                   or lower(p.nombres)   like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.apellidos) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.documento) like lower(concat('%', cast(:texto as string), '%')))
              and (cast(:activo as boolean) is null or p.activo = cast(:activo as boolean))
              and exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
            """)
    long contarPorCuidador(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("usuarioId") Long usuarioId);

    // ---------------------------------------------------------------------
    // Alcance FAMILIAR_AUTORIZADO: solo vinculaciones autorizadas.
    // ---------------------------------------------------------------------

    @Query("""
            select p from PacienteEntity p
            where (cast(:texto as string) is null
                   or lower(p.nombres)   like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.apellidos) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.documento) like lower(concat('%', cast(:texto as string), '%')))
              and (cast(:activo as boolean) is null or p.activo = cast(:activo as boolean))
              and exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
            """)
    Page<PacienteEntity> listarPorFamiliar(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where (cast(:texto as string) is null
                   or lower(p.nombres)   like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.apellidos) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(p.documento) like lower(concat('%', cast(:texto as string), '%')))
              and (cast(:activo as boolean) is null or p.activo = cast(:activo as boolean))
              and exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
            """)
    long contarPorFamiliar(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("usuarioId") Long usuarioId);

    // ---------------------------------------------------------------------
    // Visibilidad de un paciente concreto, separada por alcance.
    // ---------------------------------------------------------------------

    boolean existsById(Long pacienteId);

    @Query("""
            select count(a) > 0 from AsignacionCuidadorEntity a
            where a.pacienteId = :pacienteId and a.usuarioId = :usuarioId and a.hasta is null
            """)
    boolean esVisibleParaCuidador(@Param("pacienteId") Long pacienteId, @Param("usuarioId") Long usuarioId);

    @Query("""
            select count(v) > 0 from VinculacionFamiliarEntity v
            where v.pacienteId = :pacienteId and v.usuarioId = :usuarioId and v.autorizado = true
            """)
    boolean esVisibleParaFamiliar(@Param("pacienteId") Long pacienteId, @Param("usuarioId") Long usuarioId);
}