package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.PacienteJpaRepository;
import co.edu.uniquindio.meditrack.support.BasePruebaPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integracion de la capa de persistencia del modulo de citas contra
 * PostgreSQL real (Testcontainers).
 *
 * <p>Cubre las consultas paginadas de {@link CitaJpaRepository} por residente y
 * por conjunto de residentes, con y sin estado, que se separan por metodo para
 * no enviar parametros nulos a PostgreSQL (origen del error {@code cannot cast
 * type bytea to boolean}). Verifica ademas que el {@code total} de la pagina es
 * coherente con el contenido y que {@code TIMESTAMPTZ} se compara correctamente
 * como {@code Instant}.</p>
 *
 * <p>Las citas se insertan con SQL directo ({@link JdbcTemplate}) porque
 * {@code CitaEntity} tiene constructor protegido fuera de este paquete; el
 * significado probado (las consultas JPQL) es el mismo. Cada prueba se revierte
 * al terminar ({@code @Transactional}).</p>
 */
@Transactional
class RepositoriosCitaTest extends BasePruebaPostgres {

    @Autowired
    private CitaJpaRepository citas;
    @Autowired
    private EspecialidadJpaRepository especialidades;
    @Autowired
    private PacienteJpaRepository pacientes;
    @Autowired
    private JdbcTemplate jdbc;

    private Long pacienteA;
    private Long pacienteB;
    private Long usuario;
    private Long especialidad;
    private Long otraEspecialidad;

    @BeforeEach
    void prepararDatos() {
        pacienteA = crearPaciente("DOC-CITA-A");
        pacienteB = crearPaciente("DOC-CITA-B");
        usuario = crearUsuario("ADMINISTRADOR", "admin.citas@prueba.co");
        // Las especialidades se siembran en V3: se reutilizan en lugar de insertarlas.
        especialidad = idEspecialidad("Cardiologia");
        otraEspecialidad = idEspecialidad("Geriatria");
    }

    // ------------------------------------------------------------------
    // Por residente: combinaciones por presencia de estado.
    // ------------------------------------------------------------------

    @Test
    void porResidente_paginaYTotalCoherentes() {
        Instant pasado = Instant.parse("2025-12-01T10:00:00Z");
        Instant medio = Instant.parse("2025-12-15T10:00:00Z");
        Instant futuro = Instant.parse("2026-02-01T10:00:00Z");

        crearCita(pacienteA, especialidad, pasado, EstadoCita.CUMPLIDA);
        crearCita(pacienteA, especialidad, medio, EstadoCita.PROGRAMADA);
        crearCita(pacienteA, especialidad, futuro, EstadoCita.PROGRAMADA);
        // Cita de otro residente: no debe mezclarse.
        crearCita(pacienteB, especialidad, medio, EstadoCita.PROGRAMADA);

        Instant desde = Instant.parse("2025-12-01T00:00:00Z");
        Instant hasta = Instant.parse("2026-12-31T23:59:59Z");
        Pageable primera = PageRequest.of(0, 2);

        // Sin estado: 3 citas de A. Primera pagina de 2, total 3, orden ascendente.
        Page<CitaEntity> sinEstado = citas.listarPorPacienteYRango(pacienteA, desde, hasta, primera);
        assertThat(sinEstado.getTotalElements()).isEqualTo(3);
        assertThat(sinEstado.getContent()).hasSize(2);
        assertThat(sinEstado.getContent()).extracting(CitaEntity::getFechaHora)
                .containsExactly(pasado, medio);

        // Segunda pagina: 1 cita, total sigue siendo 3.
        Page<CitaEntity> segunda = citas.listarPorPacienteYRango(
                pacienteA, desde, hasta, PageRequest.of(1, 2));
        assertThat(segunda.getTotalElements()).isEqualTo(3);
        assertThat(segunda.getContent()).hasSize(1);

        // Con estado PROGRAMADA: 2 citas.
        Page<CitaEntity> programadas = citas.listarPorPacienteEstadoYRango(
                pacienteA, EstadoCita.PROGRAMADA, desde, hasta, primera);
        assertThat(programadas.getTotalElements()).isEqualTo(2);
        assertThat(programadas.getContent()).hasSize(2);

        assertThat(citas.listarPorPacienteYRango(pacienteB, desde, hasta, primera).getTotalElements())
                .isEqualTo(1);
    }

    // ------------------------------------------------------------------
    // Por conjunto de residentes (calendario).
    // ------------------------------------------------------------------

    @Test
    void calendario_porConjuntoDeResidentesPaginado() {
        Instant antes = Instant.parse("2025-11-15T10:00:00Z");
        Instant dentro = Instant.parse("2025-12-20T10:00:00Z");
        Instant despues = Instant.parse("2026-03-01T10:00:00Z");

        crearCita(pacienteA, especialidad, dentro, EstadoCita.PROGRAMADA);
        crearCita(pacienteB, especialidad, dentro, EstadoCita.CUMPLIDA);
        crearCita(pacienteA, especialidad, despues, EstadoCita.PROGRAMADA);
        crearCita(pacienteB, especialidad, antes, EstadoCita.PROGRAMADA);

        Instant desde = Instant.parse("2025-12-01T00:00:00Z");
        Instant hasta = Instant.parse("2025-12-31T23:59:59Z");
        Pageable primera = PageRequest.of(0, 10);

        // Dentro del rango: una cita de cada residente.
        Page<CitaEntity> rango = citas.listarPorPacientesYRango(
                List.of(pacienteA, pacienteB), desde, hasta, primera);
        assertThat(rango.getTotalElements()).isEqualTo(2);
        assertThat(rango.getContent()).hasSize(2);

        Page<CitaEntity> programadas = citas.listarPorPacientesEstadoYRango(
                List.of(pacienteA, pacienteB), EstadoCita.PROGRAMADA, desde, hasta, primera);
        assertThat(programadas.getTotalElements()).isEqualTo(1);

        // Solo un residente del conjunto: solo su cita dentro del rango.
        assertThat(citas.listarPorPacientesYRango(List.of(pacienteB), desde, hasta, primera)
                .getTotalElements()).isEqualTo(1);
    }

    // ------------------------------------------------------------------
    // Catalogo de especialidades (solo lectura).
    // ------------------------------------------------------------------

    @Test
    void especialidades_catalogoSeLeeYSeBuscaPorNombre() {
        assertThat(especialidades.existsById(especialidad)).isTrue();
        assertThat(especialidades.findById(especialidad).orElseThrow().getNombre()).isEqualTo("Cardiologia");
        assertThat(especialidades.findByNombreIgnoreCase("cardiologia")).isPresent();
        assertThat(especialidades.findAll())
                .extracting(EspecialidadEntity::getNombre)
                .contains("Cardiologia", "Geriatria");
        assertThat(otraEspecialidad).isNotEqualTo(especialidad);
    }

    // ------------------------------------------------------------------
    // Utilidades de creacion de datos (SQL directo).
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

    private Long idEspecialidad(String nombre) {
        return jdbc.queryForObject(
                "select id from catalogo_especialidad where nombre = ?", Long.class, nombre);
    }

    private void crearCita(Long pacienteId, Long especialidadId, Instant fechaHora, EstadoCita estado) {
        jdbc.update("""
                insert into cita (paciente_id, especialidad_id, registrada_por, profesional,
                                  fecha_hora, lugar, motivo, estado, creado_en, actualizado_en)
                values (?, ?, ?, ?, ?, ?, ?, ?, now(), now())
                """, pacienteId, especialidadId, usuario, "Dra. Perez",
                Timestamp.from(fechaHora), "Consultorio 2", "Control", estado.name());
    }
}