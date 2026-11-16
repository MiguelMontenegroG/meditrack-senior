package co.edu.uniquindio.meditrack.reporte.infrastructure.persistence;

import co.edu.uniquindio.meditrack.reporte.application.port.DatosReportePort;
import co.edu.uniquindio.meditrack.reporte.application.port.ReporteGeneradoRepositoryPort;
import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;
import co.edu.uniquindio.meditrack.reporte.domain.PaginaReportesDominio;
import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;
import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;
import co.edu.uniquindio.meditrack.support.BasePruebaPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integracion del modulo de reportes contra PostgreSQL real
 * (Testcontainers), con el esquema exacto de produccion (Flyway aplica V2).
 *
 * <p>Cubre la persistencia de metadatos (append-only), el historial paginado y
 * las consultas de solo lectura que alimentan el contenido del reporte, de modo
 * que las JPQL se ejecutan de verdad (no contra mocks). Cada prueba se revierte
 * al terminar ({@code @Transactional}).</p>
 */
@Transactional
class RepositoriosReporteTest extends BasePruebaPostgres {

    @Autowired
    private ReporteGeneradoRepositoryPort reportes;
    @Autowired
    private DatosReportePort datos;
    @Autowired
    private JdbcTemplate jdbc;

    private Long paciente;
    private Long usuarioAdmin;

    @BeforeEach
    void prepararDatos() {
        paciente = crearPaciente("DOC-REP-1");
        usuarioAdmin = crearUsuario("ADMINISTRADOR", "admin.rep@prueba.co");
    }

    @Test
    void guardarConservaMetadatosYListaHistorial() {
        Instant desde = Instant.parse("2026-02-01T00:00:00Z");
        Instant hasta = Instant.parse("2026-02-28T23:59:59Z");
        ReporteGenerado guardado = reportes.guardar(ReporteGenerado.nuevo(
                paciente, usuarioAdmin, TipoReporte.HISTORIA_CLINICA, desde, hasta, Instant.now()));

        assertThat(guardado.getId()).isNotNull();
        assertThat(reportes.buscarPorId(guardado.getId())).isPresent();

        reportes.guardar(ReporteGenerado.nuevo(
                paciente, usuarioAdmin, TipoReporte.BITACORA, desde, hasta, Instant.now()));

        PaginaReportesDominio pagina = reportes.listarPorPaciente(paciente, 0, 10);
        assertThat(pagina.total()).isEqualTo(2);
        assertThat(pagina.contenido()).hasSize(2);
        assertThat(pagina.contenido()).extracting(ReporteGenerado::getGeneradoPor)
                .containsOnly(usuarioAdmin);
        assertThat(reportes.contarPorPaciente(paciente)).isEqualTo(2);
    }

    @Test
    void recolectarHistoriaIncluyeTodasLasSeccionesSinFallar() {
        insertarSignos(paciente, usuarioAdmin, Instant.parse("2026-02-05T08:00:00Z"), 120, 80);

        ContenidoReporte contenido = datos.recolectar(
                paciente, TipoReporte.HISTORIA_CLINICA,
                Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-02-28T23:59:59Z"),
                Instant.now(), "Administrador");

        assertThat(contenido.cabecera().documento()).isEqualTo("DOC-REP-1");
        assertThat(contenido.bitacora()).hasSize(1);
        assertThat(contenido.bitacora().get(0).presionFormateada()).isEqualTo("120/80");
    }

    @Test
    void recolectarMedicacionYGeneralNoFallaConSeccionesVacias() {
        Instant desde = Instant.parse("2026-02-01T00:00:00Z");
        Instant hasta = Instant.parse("2026-02-28T23:59:59Z");

        ContenidoReporte medicacion = datos.recolectar(
                paciente, TipoReporte.MEDICACION, desde, hasta, Instant.now(), "Administrador");
        ContenidoReporte general = datos.recolectar(
                paciente, TipoReporte.GENERAL, desde, hasta, Instant.now(), "Administrador");

        assertThat(medicacion.medicacion()).isEmpty();
        assertThat(medicacion.citas()).isEmpty();
        assertThat(general.alertas()).isEmpty();
    }

    // ------------------------------------------------------------------
    // Utilidades (SQL directo, mismo patron que las demas integraciones)
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

    private void insertarSignos(Long pacienteId, Long autorId, Instant fechaHora,
                                Integer sistolica, Integer diastolica) {
        jdbc.update("""
                insert into registro_signos_vitales (paciente_id, registrado_por, fecha_hora,
                                                     presion_sistolica, presion_diastolica, creado_en)
                values (?, ?, ?, ?, ?, now())
                """, pacienteId, autorId, Timestamp.from(fechaHora), sistolica, diastolica);
    }
}