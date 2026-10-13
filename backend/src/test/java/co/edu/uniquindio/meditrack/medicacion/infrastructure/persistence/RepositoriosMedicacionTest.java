package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma;
import co.edu.uniquindio.meditrack.medicacion.domain.ViaAdministracion;
import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.PacienteJpaRepository;
import co.edu.uniquindio.meditrack.support.BasePruebaPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integracion de la capa de persistencia del modulo medicacion
 * contra PostgreSQL real (Testcontainers).
 *
 * <p>Cubre todas las consultas declaradas, incluidas las combinaciones de
 * filtros opcionales (donde antes aparecian fallos de tipado de parametros),
 * el mapeo de {@code frecuencia_horas} como SMALLINT, la unicidad de horarios,
 * la deteccion de solapamiento y las consultas de alcance de paciente. Cada
 * prueba se revierte al terminar ({@code @Transactional}).</p>
 *
 * <p>Los datos de paciente, usuario, asignacion y vinculacion se insertan con
 * SQL directo ({@link JdbcTemplate}) porque sus entidades tienen constructor
 * protegido fuera de este paquete; el significado probado (las consultas de
 * medicacion y de alcance) es el mismo.</p>
 */
@Transactional
class RepositoriosMedicacionTest extends BasePruebaPostgres {

    @Autowired
    private MedicamentoJpaRepository medicamentos;
    @Autowired
    private EsquemaMedicacionJpaRepository esquemas;
    @Autowired
    private HorarioMedicacionJpaRepository horarios;
    @Autowired
    private RegistroTomaJpaRepository tomas;
    @Autowired
    private PacienteJpaRepository pacientes;
    @Autowired
    private JdbcTemplate jdbc;

    private Long pacienteA;
    private Long pacienteB;
    private Long usuarioCuidador;
    private Long usuarioFamiliar;
    private Long medicamentoId;

    @BeforeEach
    void prepararDatos() {
        pacienteA = crearPaciente("DOC-A");
        pacienteB = crearPaciente("DOC-B");
        usuarioCuidador = crearUsuario("CUIDADOR_ENFERMERO", "cuidador@prueba.co");
        usuarioFamiliar = crearUsuario("FAMILIAR_AUTORIZADO", "familiar@prueba.co");
        medicamentoId = crearMedicamento("Losartan", "50 mg", true);
    }

    // ------------------------------------------------------------------
    // Catalogo de medicamentos: las cuatro combinaciones de filtros.
    // ------------------------------------------------------------------

    @Test
    void medicamentos_listar_cubreTodasLasCombinacionesDeFiltros() {
        crearMedicamento("Metformina", "850 mg", true);
        crearMedicamento("Ibuprofeno", "400 mg", false);
        PageRequest page = PageRequest.of(0, 50);

        // Sin filtros (parametros nulos antes rompian en PostgreSQL).
        assertThat(medicamentos.listarTodos(page).getContent()).hasSize(3);
        assertThat(medicamentos.contarTodos()).isEqualTo(3);

        // Solo activo.
        assertThat(medicamentos.listarPorActivo(true, page).getContent()).hasSize(2);
        assertThat(medicamentos.contarPorActivo(false)).isEqualTo(1);

        // Solo texto.
        assertThat(medicamentos.listarPorTexto("met", page).getContent()).hasSize(1);
        assertThat(medicamentos.contarPorTexto("form")).isEqualTo(1);

        // Texto y activo.
        assertThat(medicamentos.listarPorTextoYActivo("o", true, page).getContent()).hasSize(2);
        assertThat(medicamentos.contarPorTextoYActivo("o", false)).isEqualTo(1);
    }

    @Test
    void medicamentos_existePorNombre_ignoraMayusculas() {
        assertThat(medicamentos.existsByNombreIgnoreCase("LOSARTAN")).isTrue();
        assertThat(medicamentos.existsByNombreIgnoreCaseAndIdNot("losartan", medicamentoId)).isFalse();
        assertThat(medicamentos.existsByNombreIgnoreCaseAndIdNot("losartan", -1L)).isTrue();
    }

    // ------------------------------------------------------------------
    // Esquemas: frecuencia SMALLINT, filtros activo/vigente y solapamiento.
    // ------------------------------------------------------------------

    @Test
    void esquema_mapeaFrecuenciaSmallintYListaPorCombinacionesDeFiltro() {
        Long vigenteActivo = crearEsquema(pacienteA, medicamentoId, 8, LocalDate.now().minusDays(1), null, true);
        Long noVigenteActivo = crearEsquema(pacienteA, medicamentoId, 12,
                LocalDate.now().plusDays(10), null, true);
        Long suspendido = crearEsquema(pacienteA, medicamentoId, 24,
                LocalDate.now().minusDays(1), null, false);

        LocalDate hoy = LocalDate.now();

        assertThat(esquemas.findById(vigenteActivo).orElseThrow().getFrecuenciaHoras()).isEqualTo(8);

        assertThat(esquemas.listarTodos(pacienteA)).hasSize(3);
        assertThat(esquemas.listarPorActivo(pacienteA, true)).hasSize(2);
        assertThat(esquemas.listarVigente(pacienteA, hoy)).hasSize(2); // vigenteActivo + suspendido
        assertThat(esquemas.listarNoVigente(pacienteA, hoy)).hasSize(1);
        assertThat(esquemas.listarActivoVigente(pacienteA, true, hoy)).hasSize(1);
        assertThat(esquemas.listarActivoNoVigente(pacienteA, true, hoy))
                .extracting(EsquemaMedicacionEntity::getId).containsExactly(noVigenteActivo);
        assertThat(esquemas.listarActivoVigente(pacienteA, false, hoy))
                .extracting(EsquemaMedicacionEntity::getId).containsExactly(suspendido);
        assertThat(esquemas.listarActivoNoVigente(pacienteA, false, hoy)).isEmpty();

        // Los datos de otro paciente no se mezclan.
        assertThat(esquemas.listarTodos(pacienteB)).isEmpty();
    }

    @Test
    void esquema_detectaSolapamientoDeVigencia() {
        // Esquema existente activo que cubre [hoy-5, hoy+5].
        Long existente = crearEsquema(pacienteA, medicamentoId, 8,
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), true);

        LocalDate hoy = LocalDate.now();
        // Uno nuevo que empieza dentro de la vigencia existente: solapa.
        assertThat(esquemas.contarSolapamientos(pacienteA, medicamentoId, hoy, hoy.plusDays(30), 0L))
                .isEqualTo(1);
        // Excluyendo el propio esquema, no hay solapamiento consigo mismo.
        assertThat(esquemas.contarSolapamientos(pacienteA, medicamentoId, hoy, hoy.plusDays(30), existente))
                .isZero();
        // Un esquema que empieza despues de que el existente termina: no solapa.
        assertThat(esquemas.contarSolapamientos(pacienteA, medicamentoId,
                hoy.plusDays(10), hoy.plusDays(20), 0L)).isZero();
        // Medicamento distinto: no cuenta.
        Long otroMedicamento = crearMedicamento("Otro", null, true);
        assertThat(esquemas.contarSolapamientos(pacienteA, otroMedicamento, hoy, hoy.plusDays(30), 0L))
                .isZero();
    }

    @Test
    void esquema_listarUtilizables_soloActivosVigentes() {
        Long utilizable = crearEsquema(pacienteA, medicamentoId, 24, LocalDate.now().minusDays(1), null, true);
        crearEsquema(pacienteA, medicamentoId, 24, LocalDate.now().minusDays(2), null, false);
        crearEsquema(pacienteA, medicamentoId, 24, LocalDate.now().plusDays(1), null, true);

        assertThat(esquemas.listarUtilizables(pacienteA, LocalDate.now()))
                .extracting(EsquemaMedicacionEntity::getId).containsExactly(utilizable);
    }

    // ------------------------------------------------------------------
    // Horarios: unicidad y borrado masivo.
    // ------------------------------------------------------------------

    @Test
    void horarios_seInsertanYSeBorraPorEsquema() {
        Long esquemaId = crearEsquema(pacienteA, medicamentoId, 8, LocalDate.now(), null, true);
        horarios.save(new HorarioMedicacionEntity(esquemaId, LocalTime.of(7, 0)));
        horarios.save(new HorarioMedicacionEntity(esquemaId, LocalTime.of(15, 0)));
        horarios.save(new HorarioMedicacionEntity(esquemaId, LocalTime.of(23, 0)));
        horarios.flush();

        assertThat(horarios.findByEsquemaIdOrderByHora(esquemaId))
                .extracting(HorarioMedicacionEntity::getHora)
                .containsExactly(LocalTime.of(7, 0), LocalTime.of(15, 0), LocalTime.of(23, 0));
        assertThat(horarios.findByEsquemaIdIn(List.of(esquemaId))).hasSize(3);

        horarios.deleteByEsquemaId(esquemaId);
        horarios.flush();
        assertThat(horarios.findByEsquemaIdOrderByHora(esquemaId)).isEmpty();
    }

    // ------------------------------------------------------------------
    // Tomas: historial con todas las combinaciones de filtros.
    // ------------------------------------------------------------------

    @Test
    void tomas_historialCubreTodasLasCombinacionesDeFiltros() {
        Long esquema = crearEsquema(pacienteA, medicamentoId, 8, LocalDate.now().minusDays(30), null, true);
        Long otroEsquema = crearEsquema(pacienteB, medicamentoId, 8, LocalDate.now().minusDays(30), null, true);

        Instant d10 = Instant.parse("2024-06-10T08:00:00Z");
        Instant d20 = Instant.parse("2024-06-20T08:00:00Z");
        Instant d30 = Instant.parse("2024-06-30T08:00:00Z");
        crearToma(esquema, d10, EstadoToma.ADMINISTRADA);
        crearToma(esquema, d20, EstadoToma.OMITIDA);
        crearToma(esquema, d30, EstadoToma.ADMINISTRADA);
        crearToma(otroEsquema, d20, EstadoToma.ADMINISTRADA);

        PageRequest page = PageRequest.of(0, 50);

        // Sin filtros: solo las del paciente A.
        assertThat(tomas.historialSinFiltros(pacienteA, page).getContent()).hasSize(3);
        assertThat(tomas.contarHistorialSinFiltros(pacienteA)).isEqualTo(3);

        // Por esquema.
        assertThat(tomas.contarHistorialPorEsquema(pacienteA, esquema)).isEqualTo(3);

        // Solo desde / solo hasta / rango.
        assertThat(tomas.contarHistorialDesde(pacienteA, d20)).isEqualTo(2);
        assertThat(tomas.contarHistorialHasta(pacienteA, d20)).isEqualTo(2);
        assertThat(tomas.contarHistorialPorRango(pacienteA, d10, d20)).isEqualTo(2);

        // Esquema + desde / hasta / rango.
        assertThat(tomas.contarHistorialPorEsquemaDesde(pacienteA, esquema, d20)).isEqualTo(2);
        assertThat(tomas.contarHistorialPorEsquemaHasta(pacienteA, esquema, d20)).isEqualTo(2);
        assertThat(tomas.contarHistorialPorEsquemaYRango(pacienteA, esquema, d20, d30)).isEqualTo(2);

        assertThat(tomas.historialPorEsquema(pacienteA, esquema, page).getContent()).hasSize(3);
        assertThat(tomas.historialDesde(pacienteA, d20, page).getContent()).hasSize(2);
        assertThat(tomas.historialHasta(pacienteA, d20, page).getContent()).hasSize(2);
        assertThat(tomas.historialPorRango(pacienteA, d10, d30, page).getContent()).hasSize(3);
        assertThat(tomas.historialPorEsquemaDesde(pacienteA, esquema, d20, page).getContent()).hasSize(2);
        assertThat(tomas.historialPorEsquemaHasta(pacienteA, esquema, d20, page).getContent()).hasSize(2);
        assertThat(tomas.historialPorEsquemaYRango(pacienteA, esquema, d20, d30, page).getContent()).hasSize(2);
    }

    @Test
    void tomas_existeParaEsquemaYProgramada() {
        Long esquema = crearEsquema(pacienteA, medicamentoId, 8, LocalDate.now().minusDays(5), null, true);
        Instant programada = Instant.parse("2024-06-10T08:00:00Z");
        crearToma(esquema, programada, EstadoToma.ADMINISTRADA);

        assertThat(tomas.existsByEsquemaIdAndFechaHoraProgramada(esquema, programada)).isTrue();
        assertThat(tomas.existsByEsquemaIdAndFechaHoraProgramada(esquema,
                Instant.parse("2024-06-11T08:00:00Z"))).isFalse();
    }

    // ------------------------------------------------------------------
    // Alcance de paciente (consultas usadas por las pruebas de alcance).
    // ------------------------------------------------------------------

    @Test
    void paciente_alcance_cuidadorYFamiliar() {
        crearAsignacion(usuarioCuidador, pacienteA, LocalDate.now().minusDays(1), null);
        crearAsignacion(usuarioCuidador, pacienteB, LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(1)); // vencida: no visible
        crearVinculacion(usuarioFamiliar, pacienteA, true);
        crearVinculacion(usuarioFamiliar, pacienteB, false); // no autorizada

        assertThat(pacientes.esVisibleParaCuidador(pacienteA, usuarioCuidador)).isTrue();
        assertThat(pacientes.esVisibleParaCuidador(pacienteB, usuarioCuidador)).isFalse();
        assertThat(pacientes.esVisibleParaFamiliar(pacienteA, usuarioFamiliar)).isTrue();
        assertThat(pacientes.esVisibleParaFamiliar(pacienteB, usuarioFamiliar)).isFalse();
    }

    // ------------------------------------------------------------------
    // Utilidades de creacion de datos (SQL directo para las tablas ajenas).
    // ------------------------------------------------------------------

    private Long crearPaciente(String documento) {
        jdbc.update("""
                insert into paciente (documento, nombres, apellidos, fecha_nacimiento, sexo,
                                      fecha_ingreso, activo, creado_en, actualizado_en)
                values (?, ?, ?, ?, 'MASCULINO', ?, true, now(), now())
                """, documento, "Nombre " + documento, "Apellido " + documento,
                LocalDate.of(1940, 1, 1), LocalDate.now());
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

    private void crearAsignacion(Long usuarioId, Long pacienteId, LocalDate desde, LocalDate hasta) {
        jdbc.update("""
                insert into asignacion_cuidador (usuario_id, paciente_id, desde, hasta, creado_en)
                values (?, ?, ?, ?, now())
                """, usuarioId, pacienteId, desde, hasta);
    }

    private void crearVinculacion(Long usuarioId, Long pacienteId, boolean autorizado) {
        jdbc.update("""
                insert into vinculacion_familiar (usuario_id, paciente_id, parentesco, autorizado, creado_en)
                values (?, ?, 'Hijo', ?, now())
                """, usuarioId, pacienteId, autorizado);
    }

    private Long crearMedicamento(String nombre, String presentacion, boolean activo) {
        MedicamentoEntity m = new MedicamentoEntity();
        m.setNombre(nombre);
        m.setPresentacion(presentacion);
        m.setActivo(activo);
        return medicamentos.save(m).getId();
    }

    private Long crearEsquema(Long pacienteId, Long medicamentoId, int frecuencia,
                              LocalDate inicio, LocalDate fin, boolean activo) {
        EsquemaMedicacionEntity e = new EsquemaMedicacionEntity();
        e.setPacienteId(pacienteId);
        e.setMedicamentoId(medicamentoId);
        e.setDosis("1 tableta");
        e.setFrecuenciaHoras(frecuencia);
        e.setVia(ViaAdministracion.ORAL);
        e.setFechaInicio(inicio);
        e.setFechaFin(fin);
        e.setActivo(activo);
        return esquemas.save(e).getId();
    }

    private void crearToma(Long esquemaId, Instant programada, EstadoToma estado) {
        RegistroTomaEntity t = new RegistroTomaEntity();
        t.setEsquemaId(esquemaId);
        t.setRegistradoPor(usuarioCuidador);
        t.setFechaHoraProgramada(programada);
        t.setFechaHoraRegistro(programada);
        t.setEstado(estado);
        t.setObservaciones(estado == EstadoToma.OMITIDA ? "No disponible" : null);
        tomas.save(t);
    }
}
