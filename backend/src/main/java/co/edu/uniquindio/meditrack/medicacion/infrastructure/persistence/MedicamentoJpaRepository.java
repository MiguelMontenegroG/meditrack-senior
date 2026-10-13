package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data del catalogo de medicamentos.
 *
 * <p>La unicidad del nombre es sin distinguir mayusculas: se comprueba con
 * {@code lower(...)}.</p>
 *
 * <p>Los filtros opcionales ({@code texto} y {@code activo}) se resuelven con
 * <b>una consulta por combinacion de filtros presentes</b> en lugar de envolver
 * los parametros en {@code cast(... as string)} y compararlos contra nulo. Esa
 * otra tecnica fallaba en PostgreSQL con {@code cannot cast type bytea to
 * boolean} cuando el parametro llegaba nulo, porque el driver enviaba el nulo
 * sin tipo. Con consultas separadas nunca se envia un parametro nulo. El
 * adaptador elige el metodo segun que filtros llegan informados.</p>
 */
public interface MedicamentoJpaRepository extends JpaRepository<MedicamentoEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    // --- Listado: una consulta por combinacion de filtros. ---

    @Query("""
            select m from MedicamentoEntity m
            where lower(m.nombre) like lower(concat('%', :texto, '%'))
              and m.activo = :activo
            """)
    Page<MedicamentoEntity> listarPorTextoYActivo(
            @Param("texto") String texto, @Param("activo") boolean activo, Pageable pageable);
    @Query("""
            select m from MedicamentoEntity m
            where lower(m.nombre) like lower(concat('%', :texto, '%'))
            """)
    Page<MedicamentoEntity> listarPorTexto(@Param("texto") String texto, Pageable pageable);

    @Query("""
            select m from MedicamentoEntity m
            where m.activo = :activo
            """)
    Page<MedicamentoEntity> listarPorActivo(@Param("activo") boolean activo, Pageable pageable);

    @Query("select m from MedicamentoEntity m")
    Page<MedicamentoEntity> listarTodos(Pageable pageable);

    // --- Conteo: las mismas cuatro combinaciones, mismo predicado. ---

    @Query("""
            select count(m) from MedicamentoEntity m
            where lower(m.nombre) like lower(concat('%', :texto, '%'))
              and m.activo = :activo
            """)
    long contarPorTextoYActivo(@Param("texto") String texto, @Param("activo") boolean activo);

    @Query("""
            select count(m) from MedicamentoEntity m
            where lower(m.nombre) like lower(concat('%', :texto, '%'))
            """)
    long contarPorTexto(@Param("texto") String texto);

    @Query("select count(m) from MedicamentoEntity m where m.activo = :activo")
    long contarPorActivo(@Param("activo") boolean activo);

    @Query("select count(m) from MedicamentoEntity m")
    long contarTodos();
}
