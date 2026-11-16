package co.edu.uniquindio.meditrack.alerta.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.application.AtenderAlertaUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ConfiguracionAlertas;
import co.edu.uniquindio.meditrack.alerta.application.GenerarAlertasUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ResultadoGeneracion;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort;
import co.edu.uniquindio.meditrack.alerta.domain.AlcanceAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio;
import co.edu.uniquindio.meditrack.alerta.domain.RolAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.SeveridadAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.PacienteJpaRepository;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.infrastructure.persistence.AuditoriaJpaRepository;
import co.edu.uniquindio.meditrack.support.BasePruebaPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de integracion del modulo de alertas contra PostgreSQL real
 * (Testcontainers).
 *
 * <p>Cubre lo que las pruebas unitarias no ven: las tres consultas SQL de
 * {@link FuentesAlertaAdapter} sobre el esquema de V2, la idempotencia real que
 * imponen los indices unicos parciales ({@code uq_alerta_toma},
 * {@code uq_alerta_cita}, {@code uq_alerta_signos}) y la atencion
 * (ACTIVA -> REVISADA) con su auditoria.</p>
 *
 * <p>Los datos se arman con SQL directo para no depender de constructores
 * protegidos de otros paquetes. Cada prueba se revierte al terminar.</p>
 */
@Transactional
class AlertasIntegracionTest extends BasePruebaPostgres {

    /** Reloj fijo en zona del centro: 10:00 del 2026-03-10 en America/Bogota. */
    private static final Instant AHORA = Instant.parse("2026-03-10T15:00:00Z");
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final String ZONA = "America/Bogota";

    private static final ConfiguracionAlertas CONFIGURACION = new ConfiguracionAlertas(
            30, 60, 24,
            new ConfiguracionAlertas.Umbrales(90, 180, 50, 110, 70, 250, 35.0, 38.0));

    @Autowired
    private AlertaJpaRepository alertasJpa;
    @Autowired
    private AlertaRepositoryAdapter alertas;
    @Autowired
    private FuentesAlertaPort fuentes;
    @Autowired
    private PacienteAlcancePort alcancePaciente;
    @Autowired
    private AuditoriaJpaRepository auditoriaJpa;
    @Autowired
    private PacienteJpaRepository pacientesJpa;
    @Autowired
    private JdbcTemplate jdbc;

    private Long usuario;
    private Long pacienteActivo;
    private Long pacienteInactivo;

    @BeforeEach
    void prepararDatos() {
        usuario = crearUsuario("ADMINISTRADOR", "admin.alertas@prueba.co");
        pacienteActivo = crearPaciente("DOC-ALERTA-A", true);
        pacienteInactivo = crearPaciente("DOC-ALERTA-B", false);
    }

    // ------------------------------------------------------------------
    // Fuente 1: toma atrasada.
    // ------------------------------------------------------------------

    @Test
    void tomaAtrasadaDeResidenteActivoGeneraUnaSolaAlerta() {
        Long medicamento = crearMedicamento("Medicamento toma atrasada");
        Long esquema = crearEsquema(pacienteActivo, medicamento, true);
        Long horario = crearHorario(esquema, "08:00:00");
        // Un esquema del residente inactivo: nunca debe alertar.
        Long esquemaInactivo = crearEsquema(pacienteInactivo, medicamento, true);
        crearHorario(esquemaInactivo, "08:00:00");

        List<FuentesAlertaPort.FuenteToma> candidatas = fuentes.tomasAtrasadas(
                AHORA.minusSeconds(30 * 60), "2026-03-10", ZONA);
        assertThat(candidatas).extracting(FuentesAlertaPort.FuenteToma::horarioId).containsExactly(horario);
        assertThat(candidatas.getFirst().registroTomaId()).isNull();
        GenerarAlertasUseCase caso = generar();
        assertThat(caso.ejecutar().creadas()).isEqualTo(1);

        // Segunda pasada: la fuente ya tiene alerta (la consulta la excluye). jdbc
        // directo no puede leer aqui (PostgreSQL aborta la transaccion tras el
        // intento duplicado del generador); el conteo se hace con el repositorio.
        assertThat(fuentes.tomasAtrasadas(AHORA.minusSeconds(30 * 60), "2026-03-10", ZONA)).isEmpty();
        assertThat(alertasJpa.count()).isEqualTo(1);
    }

    @Test
    void tomaConRegistroDelDiaNoAlerta() {
        Long medicamento = crearMedicamento("Medicamento con toma registrada");
        Long esquema = crearEsquema(pacienteActivo, medicamento, true);
        crearHorario(esquema, "08:00:00");
        Instant programada = instanteDelDia("08:00:00");
        jdbc.update("""
                insert into registro_toma (esquema_id, registrado_por, fecha_hora_programada,
                                           fecha_hora_registro, estado, creado_en)
                values (?, ?, ?, ?, 'ADMINISTRADA', now())
                """, esquema, usuario, Timestamp.from(programada), Timestamp.from(programada));

        assertThat(fuentes.tomasAtrasadas(AHORA.minusSeconds(30 * 60), "2026-03-10", ZONA)).isEmpty();
        assertThat(generar().ejecutar().creadas()).isZero();
    }

    @Test
    void tomaDeEsquemaInactivoOFueraDeVigenciaNoAlerta() {
        Long medicamento = crearMedicamento("Medicamento inactivo");
        Long inactivo = crearEsquema(pacienteActivo, medicamento, false);
        crearHorario(inactivo, "08:00:00");
        Long vencido = crearEsquemaConVigencia(
                pacienteActivo, medicamento, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));
        crearHorario(vencido, "08:00:00");

        assertThat(fuentes.tomasAtrasadas(AHORA.minusSeconds(30 * 60), "2026-03-10", ZONA)).isEmpty();
    }

    @Test
    void tomaDentroDeLaToleranciaNoAlerta() {
        Long medicamento = crearMedicamento("Medicamento tolerancia");
        Long esquema = crearEsquema(pacienteActivo, medicamento, true);
        // 09:45 es hace 15 minutos: dentro de la tolerancia de 30 minutos.
        crearHorario(esquema, "09:45:00");

        assertThat(fuentes.tomasAtrasadas(AHORA.minusSeconds(30 * 60), "2026-03-10", ZONA)).isEmpty();
    }

    // ------------------------------------------------------------------
    // Fuente 2: cita PROGRAMADA vencida.
    // ------------------------------------------------------------------

    @Test
    void citaProgramadaVencidaGeneraAlertaSinCambiarSuEstado() {
        Long especialidad = idEspecialidad("Cardiologia");
        Long cita = crearCita(pacienteActivo, especialidad,
                AHORA.minusSeconds(120 * 60), "PROGRAMADA");
        crearCita(pacienteInactivo, especialidad, AHORA.minusSeconds(120 * 60), "PROGRAMADA");
        crearCita(pacienteActivo, especialidad, AHORA.minusSeconds(120 * 60), "CUMPLIDA");

        assertThat(fuentes.citasProgramadasVencidas(AHORA.minusSeconds(60 * 60)))
                .extracting(FuentesAlertaPort.FuenteCita::citaId)
                .containsExactly(cita);

        assertThat(generar().ejecutar().creadas()).isEqualTo(1);
        assertThat(estadoCita(cita)).isEqualTo("PROGRAMADA");
        assertThat(generar().ejecutar().creadas()).isZero();
        assertThat(cuentaAlertas()).isEqualTo(1);
    }

    // ------------------------------------------------------------------
    // Fuente 3: signos fuera de rango.
    // ------------------------------------------------------------------

    @Test
    void signosFueraDeRangoGeneranAlertaPeroLosDelRangoNo() {
        Long fuera = crearSignos(pacienteActivo, AHORA.minusSeconds(3600), 190, 100, 90, 36.5);
        crearSignos(pacienteActivo, AHORA.minusSeconds(3600), 120, 80, 95, 36.5);
        crearSignos(pacienteInactivo, AHORA.minusSeconds(3600), 190, 100, 90, 36.5);
        // Fuera de la ventana de 24 h: no se revisa.
        crearSignos(pacienteActivo, AHORA.minusSeconds(30 * 60 * 60), 190, 100, 90, 36.5);

        assertThat(fuentes.signosFueraDeRango(
                AHORA.minusSeconds(24 * 3600), umbralesConsulta()))
                .extracting(FuentesAlertaPort.FuenteSignos::signosId)
                .containsExactly(fuera);

        assertThat(generar().ejecutar().creadas()).isEqualTo(1);
        assertThat(generar().ejecutar().creadas()).isZero();
        assertThat(cuentaAlertas()).isEqualTo(1);
    }

    @Test
    void indiceUnicoImpideDosAlertasParaLaMismaFuente() {
        Long signos = crearSignos(pacienteActivo, AHORA.minusSeconds(60), 200, 100, 90, 36.5);

        Alerta alerta = Alerta.delSistema(pacienteActivo, TipoAlerta.SIGNO_FUERA_DE_RANGO,
                SeveridadAlerta.ALTA, GenerarAlertasUseCase.MENSAJE_SIGNO_FUERA_DE_RANGO,
                signos, null, null, AHORA);

        assertThat(alertas.guardar(alerta)).isPresent();
        // La segunda alta choca con uq_alerta_signos. No se puede simular "solo es
        // duplicado, sigue": PostgreSQL aborta la transaccion, por eso el adaptador
        // propaga la violacion y la pasada completa se revierte (la siguiente
        // ejecucion vuelve a leer las fuentes). Aqui se comprueba el indice.
        assertThatThrownBy(() -> alertas.guardar(alerta))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    // ------------------------------------------------------------------
    // Atencion y alcance.
    // ------------------------------------------------------------------

    @Test
    void atenderPasaAActivaARevisadaConAutorYFecha() {
        Long cita = crearCita(pacienteActivo, idEspecialidad("Cardiologia"),
                AHORA.minusSeconds(120 * 60), "PROGRAMADA");
        Long alertaId = crearAlertaCita(pacienteActivo, cita);
        AtenderAlertaUseCase caso = new AtenderAlertaUseCase(alertas,
                new VerificadorAlcanceResidente(alcancePaciente, auditoriaAdapter()),
                auditoriaAdapter(), reloj());
        long auditoriaAntes = auditoriaJpa.count();

        Alerta atendida = caso.ejecutar(alertaId,
                AlcanceAlerta.de(usuario, RolAlerta.ADMINISTRADOR));

        assertThat(atendida.getEstado()).isEqualTo(EstadoAlerta.REVISADA);
        assertThat(atendida.getRevisadaPor()).isEqualTo(usuario);
        assertThat(atendida.getRevisadaEn()).isEqualTo(AHORA);
        assertThat(auditoriaJpa.count()).isEqualTo(auditoriaAntes + 1);

        // Una segunda atencion responde 409.
        assertThatThrownBy(() -> caso.ejecutar(alertaId,
                AlcanceAlerta.de(usuario, RolAlerta.ADMINISTRADOR)))
                .isInstanceOf(ConflictoDeNegocioException.class);
    }

    @Test
    void atenderAlertaDeResidenteFueraDeAlcanceResponde404() {
        Long cita = crearCita(pacienteActivo, idEspecialidad("Cardiologia"),
                AHORA.minusSeconds(120 * 60), "PROGRAMADA");
        Long alertaId = crearAlertaCita(pacienteActivo, cita);
        // Cuidador sin asignacion vigente sobre el residente.
        Long cuidador = crearUsuario("CUIDADOR_ENFERMERO", "cuidador.alertas@prueba.co");
        AtenderAlertaUseCase caso = new AtenderAlertaUseCase(alertas,
                new VerificadorAlcanceResidente(alcancePaciente, auditoriaAdapter()),
                auditoriaAdapter(), reloj());

        assertThatThrownBy(() -> caso.ejecutar(alertaId,
                AlcanceAlerta.de(cuidador, RolAlerta.CUIDADOR_ENFERMERO)))
                .isInstanceOf(co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException.class);
    }

    @Test
    void listadoPorResidenteYEstadoFiltraYOrdenaDescendente() {
        Long especialidad = idEspecialidad("Cardiologia");
        Long citaActivo = crearCita(pacienteActivo, especialidad,
                AHORA.minusSeconds(120 * 60), "PROGRAMADA");
        crearAlertaCita(pacienteActivo, citaActivo);
        Long esquema = crearEsquema(pacienteActivo, crearMedicamento("Medicamento listado"), true);
        Long toma = crearRegistroToma(esquema, "20:00:00");
        crearAlertaToma(pacienteActivo, toma);
        Long citaInactivo = crearCita(pacienteInactivo, especialidad,
                AHORA.minusSeconds(120 * 60), "PROGRAMADA");
        crearAlertaCita(pacienteInactivo, citaInactivo);

        PaginaAlertasDominio todas = alertas.listarPorPaciente(pacienteActivo, 0, 20);
        assertThat(todas.total()).isEqualTo(2);

        PaginaAlertasDominio activas = alertas.listarPorPacienteYEstado(
                pacienteActivo, EstadoAlerta.ACTIVA, 0, 20);
        assertThat(activas.total()).isEqualTo(2);

        PaginaAlertasDominio dosis = alertas.listarPorPacienteYTipo(
                pacienteActivo, TipoAlerta.DOSIS_ATRASADA, 0, 20);
        assertThat(dosis.total()).isEqualTo(1);
        assertThat(dosis.contenido().getFirst().getTomaId()).isEqualTo(toma);

        assertThat(alertas.listarPorPacientes(List.of(pacienteActivo, pacienteInactivo), 0, 20)
                .total()).isEqualTo(3);
    }

    // ------------------------------------------------------------------
    // Utilidades.
    // ------------------------------------------------------------------

    private GenerarAlertasUseCase generar() {
        return new GenerarAlertasUseCase(fuentes, alertas, CONFIGURACION, reloj());
    }

    private static Clock reloj() {
        return Clock.fixed(AHORA, BOGOTA);
    }

    /**
     * Auditoria sobre la tabla real de V2 (mismo esquema que usa el adaptador de
     * produccion). Se escribe aqui para no depender de clases internas de otro
     * modulo.
     */
    private AuditoriaPort auditoriaAdapter() {
        return (usuarioId, accion, entidad, entidadId, resultado, detalle) ->
                jdbc.update("""
                        insert into auditoria_acceso (usuario_id, accion, entidad, entidad_id,
                                                     fecha_hora, resultado, detalle)
                        values (?, ?, ?, ?, now(), ?, ?)
                        """, usuarioId, accion.name(), entidad, entidadId, resultado.name(), detalle);
    }

    private static FuentesAlertaPort.UmbralesConsulta umbralesConsulta() {
        return new FuentesAlertaPort.UmbralesConsulta(90, 180, 50, 110, 70, 250, 35.0, 38.0);
    }

    private Instant instanteDelDia(String hora) {
        return LocalDate.of(2026, 3, 10).atTime(java.time.LocalTime.parse(hora))
                .atZone(BOGOTA).toInstant();
    }

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

    private Long crearMedicamento(String nombre) {
        jdbc.update("insert into catalogo_medicamento (nombre, activo) values (?, true)", nombre);
        return jdbc.queryForObject(
                "select id from catalogo_medicamento where nombre = ?", Long.class, nombre);
    }

    private Long crearEsquema(Long pacienteId, Long medicamentoId, boolean activo) {
        return crearEsquemaConVigencia(pacienteId, medicamentoId, LocalDate.of(2026, 1, 1), null, activo);
    }

    private Long crearEsquemaConVigencia(
            Long pacienteId, Long medicamentoId, LocalDate inicio, LocalDate fin) {
        return crearEsquemaConVigencia(pacienteId, medicamentoId, inicio, fin, true);
    }

    private Long crearEsquemaConVigencia(
            Long pacienteId, Long medicamentoId, LocalDate inicio, LocalDate fin, boolean activo) {
        jdbc.update("""
                insert into esquema_medicacion (paciente_id, medicamento_id, dosis, frecuencia_horas, via,
                                                fecha_inicio, fecha_fin, activo, creado_en, actualizado_en)
                values (?, ?, '1 tableta', 8, 'ORAL', ?, ?, ?, now(), now())
                """, pacienteId, medicamentoId, inicio, fin, activo);
        return jdbc.queryForObject(
                "select id from esquema_medicacion where paciente_id = ? order by id desc limit 1",
                Long.class, pacienteId);
    }

    private Long crearRegistroToma(Long esquemaId, String hora) {
        jdbc.update("""
                insert into registro_toma (esquema_id, registrado_por, fecha_hora_programada,
                                           fecha_hora_registro, estado, creado_en)
                values (?, ?, ?, ?, 'ADMINISTRADA', now())
                """, esquemaId, usuario, Timestamp.from(instanteDelDia(hora)),
                Timestamp.from(instanteDelDia(hora)));
        return jdbc.queryForObject(
                "select id from registro_toma where esquema_id = ? order by id desc limit 1",
                Long.class, esquemaId);
    }

    private Long crearHorario(Long esquemaId, String hora) {
        jdbc.update("insert into horario_medicacion (esquema_id, hora) values (?, ?::time)",
                esquemaId, hora);
        return jdbc.queryForObject(
                "select id from horario_medicacion where esquema_id = ? order by id desc limit 1",
                Long.class, esquemaId);
    }

    private Long idEspecialidad(String nombre) {
        return jdbc.queryForObject(
                "select id from catalogo_especialidad where nombre = ?", Long.class, nombre);
    }

    private Long crearCita(Long pacienteId, Long especialidadId, Instant fechaHora, String estado) {
        jdbc.update("""
                insert into cita (paciente_id, especialidad_id, registrada_por, profesional,
                                  fecha_hora, lugar, motivo, estado, creado_en, actualizado_en)
                values (?, ?, ?, ?, ?, ?, ?, ?, now(), now())
                """, pacienteId, especialidadId, usuario, "Dra. Perez",
                Timestamp.from(fechaHora), "Consultorio 2", "Control", estado);
        return jdbc.queryForObject(
                "select id from cita where paciente_id = ? order by id desc limit 1",
                Long.class, pacienteId);
    }

    private String estadoCita(Long citaId) {
        return jdbc.queryForObject("select estado from cita where id = ?", String.class, citaId);
    }

    private Long crearSignos(Long pacienteId, Instant fechaHora,
                             Integer sistolica, Integer diastolica,
                             Integer glucosa, Double temperatura) {
        jdbc.update("""
                insert into registro_signos_vitales (paciente_id, registrado_por, fecha_hora,
                                                     presion_sistolica, presion_diastolica,
                                                     glucosa_mg_dl, temperatura_c, creado_en)
                values (?, ?, ?, ?, ?, ?, ?, now())
                """, pacienteId, usuario, Timestamp.from(fechaHora),
                sistolica, diastolica, glucosa, temperatura);
        return jdbc.queryForObject(
                "select id from registro_signos_vitales where paciente_id = ? order by id desc limit 1",
                Long.class, pacienteId);
    }

    private Long crearAlertaCita(Long pacienteId, Long citaId) {
        return insertarAlerta(pacienteId, TipoAlerta.CITA_PROXIMA, null, null, citaId);
    }

    private Long crearAlertaToma(Long pacienteId, Long tomaId) {
        return insertarAlerta(pacienteId, TipoAlerta.DOSIS_ATRASADA, tomaId, null, null);
    }

    private Long insertarAlerta(Long pacienteId, TipoAlerta tipo,
                                Long tomaId, Long signosId, Long citaId) {
        jdbc.update("""
                insert into alerta (paciente_id, tipo, severidad, mensaje, origen,
                                    signos_id, toma_id, cita_id, estado, creado_en)
                values (?, ?, 'MEDIA', 'Alerta de prueba', 'SISTEMA', ?, ?, ?, 'ACTIVA', now())
                """, pacienteId, tipo.name(), signosId, tomaId, citaId);
        return jdbc.queryForObject("select max(id) from alerta", Long.class);
    }

    /** Conteo por SQL: no consulta el contexto de persistencia de la prueba. */
    private long cuentaAlertas() {
        return jdbc.queryForObject("select count(*) from alerta", Long.class);
    }

    /** Confirma que la dependencia de persistencia de pacientes se usa (alcance). */
    @Test
    void elResidenteActivoEsVisibleParaElAdministrador() {
        assertThat(pacientesJpa.findById(pacienteActivo)).isPresent();
        assertThat(alcancePaciente.visible(pacienteActivo, usuario, RolUsuario.ADMINISTRADOR)).isTrue();
    }

    /** Verifica que la auditoria del adaptador registra el evento con sus campos. */
    @Test
    void elAdaptadorDeAuditoriaRegistraElEvento() {
        long antes = auditoriaJpa.count();
        auditoriaAdapter().registrar(usuario, AccionAuditoria.UPDATE, "alerta", 1L,
                ResultadoAuditoria.EXITOSO, "Atencion de alerta");
        assertThat(auditoriaJpa.count()).isEqualTo(antes + 1);
    }
}
