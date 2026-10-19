package co.edu.uniquindio.meditrack.bitacora.infrastructure.persistence;

import co.edu.uniquindio.meditrack.bitacora.application.PaginaSignosVitalesDominio;
import co.edu.uniquindio.meditrack.bitacora.application.port.SignosVitalesRepositoryPort;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.support.BasePruebaPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integracion del modulo de bitacora contra PostgreSQL real
 * (Testcontainers), con el esquema exacto de produccion (Flyway aplica V2).
 *
 * <p>Cubre: insercion y recuperacion del registro, historial paginado con y sin
 * filtros de rango (metodos separados para no enviar parametros nulos), orden por
 * fecha descendente, ultimo registro, y el alcance de los tres roles resuelto con
 * el {@link PacienteAlcancePort} (admin ve todo, cuidador solo con asignacion
 * vigente, familiar solo con vinculacion autorizada).</p>
 *
 * <p>Cada prueba se revierte al terminar ({@code @Transactional}).</p>
 */
@Transactional
class RepositoriosBitacoraTest extends BasePruebaPostgres {

    @Autowired
    private SignosVitalesRepositoryPort signos;
    @Autowired
    private PacienteAlcancePort alcance;
    @Autowired
    private JdbcTemplate jdbc;

    private Long pacienteA;
    private Long pacienteInactivo;
    private Long usuarioAdmin;
    private Long usuarioCuidador;
    private Long usuarioFamiliar;

    @BeforeEach
    void prepararDatos() {
        pacienteA = crearPaciente("DOC-BIT-A", true);
        pacienteInactivo = crearPaciente("DOC-BIT-INACT", false);
        usuarioAdmin = crearUsuario("ADMINISTRADOR", "admin.bit@prueba.co");
        usuarioCuidador = crearUsuario("CUIDADOR_ENFERMERO", "cuidador.bit@prueba.co");
        usuarioFamiliar = crearUsuario("FAMILIAR_AUTORIZADO", "familiar.bit@prueba.co");
    }

    // ------------------------------------------------------------------
    // Persistencia e historial paginado
    // ------------------------------------------------------------------

    @Test
    void guardarYRecuperarConservaTodosLosCampos() {
        Instant fecha = Instant.parse("2026-02-10T08:00:00Z");
        RegistroSignosVitales guardado = signos.guardar(new RegistroSignosVitales(
                null, pacienteA, usuarioCuidador, fecha, 120, 80, 95,
                new BigDecimal("36.7"), "Sin novedad"));

        assertThat(guardado.getId()).isNotNull();
        RegistroSignosVitales leido = signos.buscarUltimoPorPaciente(pacienteA).orElseThrow();
        assertThat(leido.getId()).isEqualTo(guardado.getId());
        assertThat(leido.getPacienteId()).isEqualTo(pacienteA);
        assertThat(leido.getRegistradoPor()).isEqualTo(usuarioCuidador);
        assertThat(leido.getPresionSistolica()).isEqualTo(120);
        assertThat(leido.getPresionDiastolica()).isEqualTo(80);
        assertThat(leido.getGlucosaMgDl()).isEqualTo(95);
        assertThat(leido.getTemperaturaC()).isEqualByComparingTo("36.7");
        assertThat(leido.getObservaciones()).isEqualTo("Sin novedad");
    }

    @Test
    void historialPaginadoOrdenDescendenteYTotalCoherente() {
        Instant antes = Instant.parse("2026-02-01T08:00:00Z");
        Instant medio = Instant.parse("2026-02-05T08:00:00Z");
        Instant despues = Instant.parse("2026-02-09T08:00:00Z");
        insertarSignos(pacienteA, usuarioCuidador, antes, 110, 70);
        insertarSignos(pacienteA, usuarioCuidador, medio, 115, 75);
        insertarSignos(pacienteA, usuarioCuidador, despues, 120, 80);
        // Registro de otro residente: no debe mezclarse.
        insertarSignos(pacienteInactivo, usuarioCuidador, medio, 130, 85);

        PaginaSignosVitalesDominio primera = signos.listarPorPaciente(pacienteA, 0, 2);
        assertThat(primera.total()).isEqualTo(3);
        assertThat(primera.contenido()).hasSize(2);
        // Orden descendente: el mas reciente primero.
        assertThat(primera.contenido().get(0).getFechaHora()).isEqualTo(despues);
        assertThat(primera.contenido().get(1).getFechaHora()).isEqualTo(medio);

        PaginaSignosVitalesDominio segunda = signos.listarPorPaciente(pacienteA, 1, 2);
        assertThat(segunda.total()).isEqualTo(3);
        assertThat(segunda.contenido()).hasSize(1);
        assertThat(segunda.contenido().get(0).getFechaHora()).isEqualTo(antes);
    }

    @Test
    void historialFiltradoPorRangoYPorCadaExtremo() {
        Instant antes = Instant.parse("2026-01-20T08:00:00Z");
        Instant dentro = Instant.parse("2026-02-05T08:00:00Z");
        Instant despues = Instant.parse("2026-02-20T08:00:00Z");
        insertarSignos(pacienteA, usuarioCuidador, antes, 110, 70);
        insertarSignos(pacienteA, usuarioCuidador, dentro, 115, 75);
        insertarSignos(pacienteA, usuarioCuidador, despues, 120, 80);

        Instant desde = Instant.parse("2026-02-01T00:00:00Z");
        Instant hasta = Instant.parse("2026-02-10T23:59:59Z");

        assertThat(signos.listarPorPacienteYRango(pacienteA, desde, hasta, 0, 10).total())
                .isEqualTo(1);
        assertThat(signos.listarPorPacienteDesde(pacienteA, desde, 0, 10).total())
                .isEqualTo(2);
        assertThat(signos.listarPorPacienteHasta(pacienteA, hasta, 0, 10).total())
                .isEqualTo(2);
    }

    @Test
    void ultimoEsElMasRecienteYVacioSinRegistros() {
        insertarSignos(pacienteA, usuarioCuidador, Instant.parse("2026-02-01T08:00:00Z"), 110, 70);
        insertarSignos(pacienteA, usuarioCuidador, Instant.parse("2026-02-09T08:00:00Z"), 120, 80);
        insertarSignos(pacienteA, usuarioCuidador, Instant.parse("2026-02-05T08:00:00Z"), 115, 75);

        Optional<RegistroSignosVitales> ultimo = signos.buscarUltimoPorPaciente(pacienteA);
        assertThat(ultimo).isPresent();
        assertThat(ultimo.get().getFechaHora()).isEqualTo(Instant.parse("2026-02-09T08:00:00Z"));

        Long sinRegistros = crearPaciente("DOC-BIT-VACIO", true);
        assertThat(signos.buscarUltimoPorPaciente(sinRegistros)).isEmpty();
    }

    // ------------------------------------------------------------------
    // Alcance de los tres roles (reutiliza PacienteAlcancePort -> modulo paciente)
    // ------------------------------------------------------------------

    @Test
    void alcanceAdminVeCualquierResidenteExistente() {
        assertThat(alcance.visible(pacienteA, usuarioAdmin, RolUsuario.ADMINISTRADOR)).isTrue();
        assertThat(alcance.visible(pacienteInactivo, usuarioAdmin, RolUsuario.ADMINISTRADOR)).isTrue();
    }

    @Test
    void alcanceCuidadorSoloConAsignacionVigente() {
        // Sin asignacion: no ve el residente.
        assertThat(alcance.visible(pacienteA, usuarioCuidador, RolUsuario.CUIDADOR_ENFERMERO)).isFalse();

        asignarCuidador(pacienteA, usuarioCuidador, LocalDate.now().minusDays(1), null);
        assertThat(alcance.visible(pacienteA, usuarioCuidador, RolUsuario.CUIDADOR_ENFERMERO)).isTrue();
    }

    @Test
    void alcanceFamiliarSoloConVinculacionAutorizada() {
        assertThat(alcance.visible(pacienteA, usuarioFamiliar, RolUsuario.FAMILIAR_AUTORIZADO)).isFalse();

        vincularFamiliar(pacienteA, usuarioFamiliar, true);
        assertThat(alcance.visible(pacienteA, usuarioFamiliar, RolUsuario.FAMILIAR_AUTORIZADO)).isTrue();
    }

    @Test
    void residenteInactivoNoAdmiteRegistroSegunEstaActivo() {
        assertThat(alcance.estaActivo(pacienteA)).isTrue();
        assertThat(alcance.estaActivo(pacienteInactivo)).isFalse();
    }

    // ------------------------------------------------------------------
    // Utilidades (SQL directo reutilizando el patron de citas)
    // ------------------------------------------------------------------

    private Long crearPaciente(String documento, boolean activo) {
        jdbc.update("""
                insert into paciente (documento, nombres, apellidos, fecha_nacimiento, sexo,
                                      fecha_ingreso, activo, creado_en, actualizado_en)
                values (?, ?, ?, ?, 'MASCULINO', ?, ?, now(), now())
                """, documento, "Nombre " + documento, "Apellido " + documento,
                LocalDate.of(1940, 1, 1), LocalDate.now(), activo);
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

    private void insertarSignos(Long pacienteId, Long autorId, Instant fechaHora,
                                Integer sistolica, Integer diastolica) {
        jdbc.update("""
                insert into registro_signos_vitales (paciente_id, registrado_por, fecha_hora,
                                                     presion_sistolica, presion_diastolica, creado_en)
                values (?, ?, ?, ?, ?, now())
                """, pacienteId, autorId, Timestamp.from(fechaHora), sistolica, diastolica);
    }

    private void asignarCuidador(Long pacienteId, Long cuidadorId, LocalDate desde, LocalDate hasta) {
        jdbc.update("""
                insert into asignacion_cuidador (paciente_id, usuario_id, desde, hasta, creado_en)
                values (?, ?, ?, ?, now())
                """, pacienteId, cuidadorId, desde, hasta);
    }

    private void vincularFamiliar(Long pacienteId, Long familiarId, boolean autorizado) {
        jdbc.update("""
                insert into vinculacion_familiar (paciente_id, usuario_id, parentesco, autorizado, creado_en)
                values (?, ?, ?, ?, now())
                """, pacienteId, familiarId, "HIJO", autorizado);
    }
}