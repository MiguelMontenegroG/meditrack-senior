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
 * <p>Los filtros opcionales ({@code texto} y {@code activo}) se resuelven con
 * <b>una consulta por combinacion de filtros presentes</b>, en lugar de
 * envolver los parametros en {@code cast(... as string)} / {@code cast(... as
 * boolean)} y compararlos contra nulo. Ese truco fallaba en PostgreSQL: cuando
 * el parametro llegaba nulo, el driver enviaba el nulo sin tipo y la consulta
 * moria con {@code function lower(bytea) does not exist} / {@code cannot cast
 * type bytea to boolean}. Con consultas separadas nunca se envia un parametro
 * nulo. El adaptador elige el metodo segun que filtros llegan informados.</p>
 *
 * <p>Dentro de cada alcance, el predicado de busqueda y de estado es unico, de
 * modo que el listado y su conteo comparten exactamente el mismo filtro y son
 * coherentes.</p>
 */
public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    // ---------------------------------------------------------------------
    // Alcance ADMINISTRADOR: ve todos los pacientes.
    // ---------------------------------------------------------------------

    @Query("""
            select p from PacienteEntity p
            where lower(p.nombres)   like lower(concat('%', :texto, '%'))
               or lower(p.apellidos) like lower(concat('%', :texto, '%'))
               or lower(p.documento) like lower(concat('%', :texto, '%'))
            """)
    Page<PacienteEntity> listarTodosPorTexto(@Param("texto") String texto, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where lower(p.nombres)   like lower(concat('%', :texto, '%'))
               or lower(p.apellidos) like lower(concat('%', :texto, '%'))
               or lower(p.documento) like lower(concat('%', :texto, '%'))
            """)
    long contarTodosPorTexto(@Param("texto") String texto);

    @Query("select p from PacienteEntity p where p.activo = :activo")
    Page<PacienteEntity> listarTodosPorActivo(@Param("activo") boolean activo, Pageable pageable);

    @Query("select count(p) from PacienteEntity p where p.activo = :activo")
    long contarTodosPorActivo(@Param("activo") boolean activo);

    @Query("""
            select p from PacienteEntity p
            where p.activo = :activo
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    Page<PacienteEntity> listarTodosPorTextoYActivo(
            @Param("texto") String texto, @Param("activo") boolean activo, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where p.activo = :activo
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    long contarTodosPorTextoYActivo(@Param("texto") String texto, @Param("activo") boolean activo);

    @Query("select p from PacienteEntity p")
    Page<PacienteEntity> listarTodosSinFiltros(Pageable pageable);

    @Query("select count(p) from PacienteEntity p")
    long contarTodosSinFiltros();

    // ---------------------------------------------------------------------
    // Alcance CUIDADOR_ENFERMERO: solo pacientes con asignacion vigente.
    // ---------------------------------------------------------------------

    @Query("""
            select p from PacienteEntity p
            where exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
            """)
    Page<PacienteEntity> listarCuidadorSinFiltros(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
            """)
    long contarCuidadorSinFiltros(@Param("usuarioId") Long usuarioId);

    @Query("""
            select p from PacienteEntity p
            where exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    Page<PacienteEntity> listarCuidadorPorTexto(
            @Param("texto") String texto, @Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    long contarCuidadorPorTexto(@Param("texto") String texto, @Param("usuarioId") Long usuarioId);

    @Query("""
            select p from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
            """)
    Page<PacienteEntity> listarCuidadorPorActivo(
            @Param("activo") boolean activo, @Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
            """)
    long contarCuidadorPorActivo(@Param("activo") boolean activo, @Param("usuarioId") Long usuarioId);

    @Query("""
            select p from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    Page<PacienteEntity> listarCuidadorPorTextoYActivo(
            @Param("texto") String texto, @Param("activo") boolean activo,
            @Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from AsignacionCuidadorEntity a
                          where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    long contarCuidadorPorTextoYActivo(
            @Param("texto") String texto, @Param("activo") boolean activo, @Param("usuarioId") Long usuarioId);

    // ---------------------------------------------------------------------
    // Alcance FAMILIAR_AUTORIZADO: solo vinculaciones autorizadas.
    // ---------------------------------------------------------------------

    @Query("""
            select p from PacienteEntity p
            where exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
            """)
    Page<PacienteEntity> listarFamiliarSinFiltros(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
            """)
    long contarFamiliarSinFiltros(@Param("usuarioId") Long usuarioId);

    @Query("""
            select p from PacienteEntity p
            where exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    Page<PacienteEntity> listarFamiliarPorTexto(
            @Param("texto") String texto, @Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    long contarFamiliarPorTexto(@Param("texto") String texto, @Param("usuarioId") Long usuarioId);

    @Query("""
            select p from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
            """)
    Page<PacienteEntity> listarFamiliarPorActivo(
            @Param("activo") boolean activo, @Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
            """)
    long contarFamiliarPorActivo(@Param("activo") boolean activo, @Param("usuarioId") Long usuarioId);

    @Query("""
            select p from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    Page<PacienteEntity> listarFamiliarPorTextoYActivo(
            @Param("texto") String texto, @Param("activo") boolean activo,
            @Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where p.activo = :activo
              and exists (select 1 from VinculacionFamiliarEntity v
                          where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true)
              and (lower(p.nombres)   like lower(concat('%', :texto, '%'))
                or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                or lower(p.documento) like lower(concat('%', :texto, '%')))
            """)
    long contarFamiliarPorTextoYActivo(
            @Param("texto") String texto, @Param("activo") boolean activo, @Param("usuarioId") Long usuarioId);

    // ---------------------------------------------------------------------
    // Visibilidad de un paciente concreto, separada por alcance.
    // ---------------------------------------------------------------------

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