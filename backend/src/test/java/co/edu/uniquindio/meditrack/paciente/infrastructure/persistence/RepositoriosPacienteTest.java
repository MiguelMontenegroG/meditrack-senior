package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.support.BasePruebaPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integracion de la capa de persistencia del modulo paciente contra
 * PostgreSQL real (Testcontainers).
 *
 * <p>Cubre las combinaciones de filtros opcionales ({@code texto} y
 * {@code activo}) que antes solo se resolvian con {@code cast(... as string)} /
 * {@code cast(... as boolean)} y fallaban en PostgreSQL cuando el parametro
 * llegaba nulo ({@code function lower(bytea) does not exist},
 * {@code cannot cast type bytea to boolean}). Tambien cubre el filtrado por
 * alcance (administrador, cuidador con asignacion vigente y familiar
 * autorizado) y la coherencia entre el listado y su conteo. Cada prueba se
 * revierte al terminar ({@code @Transactional}).</p>
 */
@Transactional
class RepositoriosPacienteTest extends BasePruebaPostgres {

    private static final PageRequest PRIMERA_PAGINA = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id"));

    @Autowired
    private PacienteJpaRepository pacientes;
    @Autowired
    private JdbcTemplate jdbc;

    private Long activoA;
    private Long activoB;
    private Long inactivoC;

    private Long cuidador;
    private Long familiar;

    @BeforeEach
    void prepararDatos() {
        activoA = crearPaciente("DOC-A", "Maria", "Gomez", true);
        activoB = crearPaciente("DOC-B", "Juan", "Ramirez", true);
        inactivoC = crearPaciente("DOC-C", "Marta", "Gomez", false);

        cuidador = crearUsuario("CUIDADOR_ENFERMERO", "cuidador@prueba.co");
        familiar = crearUsuario("FAMILIAR_AUTORIZADO", "familiar@prueba.co");

        // El cuidador solo tiene asignacion vigente sobre A.
        asignar(cuidador, activoA, LocalDate.now().minusDays(1), null);
        // El cuidador tiene una asignacion vencida sobre B: no debe verla por ese motivo.
        asignar(cuidador, activoB, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
        // El familiar solo tiene vinculacion autorizada sobre A.
        vincular(familiar, activoA, true);
        vincular(familiar, activoB, false);
    }

    // ------------------------------------------------------------------
    // ADMINISTRADOR: las cuatro combinaciones de filtros (antes rompian por cast).
    // ------------------------------------------------------------------

    @Test
    void admin_listarCubreTodasLasCombinacionesDeFiltros() {
        // Sin filtros.
        assertThat(pacientes.listarTodosSinFiltros(PRIMERA_PAGINA).getContent()).hasSize(3);
        assertThat(pacientes.contarTodosSinFiltros()).isEqualTo(3);

        // Solo activo.
        assertThat(pacientes.listarTodosPorActivo(true, PRIMERA_PAGINA).getContent()).hasSize(2);
        assertThat(pacientes.contarTodosPorActivo(false)).isEqualTo(1);

        // Solo texto (busca en nombres, apellidos y documento).
        assertThat(pacientes.listarTodosPorTexto("gomez", PRIMERA_PAGINA).getContent()).hasSize(2);
        assertThat(pacientes.contarTodosPorTexto("DOC-B")).isEqualTo(1);

        // Texto y activo.
        assertThat(pacientes.listarTodosPorTextoYActivo("gomez", true, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarTodosPorTextoYActivo("gomez", false)).isEqualTo(1);
    }

    @Test
    void admin_listadoYConteoSonCoherentesConPaginacion() {
        // Primera pagina de 2 elementos: el total sigue siendo 3.
        var primera = pacientes.listarTodosSinFiltros(PageRequest.of(0, 2));
        assertThat(primera.getContent()).hasSize(2);
        assertThat(primera.getTotalElements()).isEqualTo(3);
        assertThat(pacientes.contarTodosSinFiltros()).isEqualTo(3);
    }

    // ------------------------------------------------------------------
    // CUIDADOR_ENFERMERO: solo asignaciones vigentes.
    // ------------------------------------------------------------------

    @Test
    void cuidador_cubreTodasLasCombinacionesDeFiltrosYRespetaElAlcance() {
        assertThat(pacientes.listarCuidadorSinFiltros(cuidador, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarCuidadorSinFiltros(cuidador)).isEqualTo(1);

        assertThat(pacientes.listarCuidadorPorActivo(true, cuidador, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarCuidadorPorActivo(true, cuidador)).isEqualTo(1);

        assertThat(pacientes.listarCuidadorPorTexto("gomez", cuidador, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarCuidadorPorTexto("x", cuidador)).isZero();

        assertThat(pacientes.listarCuidadorPorTextoYActivo("maria", true, cuidador, PRIMERA_PAGINA).getContent())
                .hasSize(1);
        assertThat(pacientes.contarCuidadorPorTextoYActivo("juan", true, cuidador)).isZero();

        // La asignacion vencida sobre B no da acceso a B.
        assertThat(pacientes.esVisibleParaCuidador(activoA, cuidador)).isTrue();
        assertThat(pacientes.esVisibleParaCuidador(activoB, cuidador)).isFalse();
    }

    // ------------------------------------------------------------------
    // FAMILIAR_AUTORIZADO: solo vinculaciones autorizadas.
    // ------------------------------------------------------------------

    @Test
    void familiar_cubreTodasLasCombinacionesDeFiltrosYRespetaElAlcance() {
        assertThat(pacientes.listarFamiliarSinFiltros(familiar, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarFamiliarSinFiltros(familiar)).isEqualTo(1);

        assertThat(pacientes.listarFamiliarPorActivo(true, familiar, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarFamiliarPorActivo(true, familiar)).isEqualTo(1);

        assertThat(pacientes.listarFamiliarPorTexto("gomez", familiar, PRIMERA_PAGINA).getContent()).hasSize(1);
        assertThat(pacientes.contarFamiliarPorTexto("ramirez", familiar)).isZero();

        assertThat(pacientes.listarFamiliarPorTextoYActivo("maria", true, familiar, PRIMERA_PAGINA).getContent())
                .hasSize(1);
        assertThat(pacientes.contarFamiliarPorTextoYActivo("maria", false, familiar)).isZero();

        // La vinculacion no autorizada sobre B no da acceso a B.
        assertThat(pacientes.esVisibleParaFamiliar(activoA, familiar)).isTrue();
        assertThat(pacientes.esVisibleParaFamiliar(activoB, familiar)).isFalse();
    }

    // ------------------------------------------------------------------
    // Utilidades de creacion de datos (SQL directo para tablas ajenas).
    // ------------------------------------------------------------------

    private Long crearPaciente(String documento, String nombres, String apellidos, boolean activo) {
        jdbc.update("""
                insert into paciente (documento, nombres, apellidos, fecha_nacimiento, sexo,
                                      fecha_ingreso, activo, creado_en, actualizado_en)
                values (?, ?, ?, ?, 'MASCULINO', ?, ?, now(), now())
                """, documento, nombres, apellidos, LocalDate.of(1940, 1, 1), LocalDate.now(), activo);
        return jdbc.queryForObject("select id from paciente where documento = ?", Long.class, documento);
    }

    private Long crearUsuario(String rolNombre, String correo) {
        Long rolId = jdbc.queryForObject("select id from rol where nombre = ?", Long.class, rolNombre);
        jdbc.update("""
                insert into usuario (rol_id, nombre_completo, correo, hash_contrasena, activo,
                                     creado_en, actualizado_en)
                values (?, ?, ?, ?, true, now(), now())
                """, rolId, "Usuario " + correo, correo,
                "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRS");
        return jdbc.queryForObject("select id from usuario where correo = ?", Long.class, correo);
    }

    private void asignar(Long usuarioId, Long pacienteId, LocalDate desde, LocalDate hasta) {
        jdbc.update("""
                insert into asignacion_cuidador (usuario_id, paciente_id, desde, hasta, creado_en)
                values (?, ?, ?, ?, now())
                """, usuarioId, pacienteId, desde, hasta);
    }

    private void vincular(Long usuarioId, Long pacienteId, boolean autorizado) {
        jdbc.update("""
                insert into vinculacion_familiar (usuario_id, paciente_id, parentesco, autorizado, creado_en)
                values (?, ?, 'Hijo', ?, now())
                """, usuarioId, pacienteId, autorizado);
    }
}