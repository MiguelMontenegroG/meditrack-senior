package co.edu.uniquindio.meditrack.alerta.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.application.port.AlertaRepositoryPort;
import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de alertas sobre el repositorio Spring Data.
 *
 * <p><b>Idempotencia:</b> los indices unicos parciales {@code uq_alerta_toma},
 * {@code uq_alerta_cita} y {@code uq_alerta_signos} garantizan una alerta por
 * fuente. La generacion evita duplicados leyendo antes de escribir; si aun asi la
 * base rechaza un alta (dos ejecuciones simultaneas), se propaga la excepcion y la
 * transaccion se revierte completa, lo que tambien revierte el {@code registro_toma}
 * OMITIDA que la acompanaba.</p>
 *
 * <p><b>Materializacion de la toma:</b> {@code alerta.toma_id} tiene clave foranea
 * a {@code registro_toma(id)} y V2 no guarda las tomas futuras. Cuando el generador
 * detecta una toma atrasada sin fila, este adaptador crea el registro como
 * {@code OMITIDA} ({@code registrado_por} es NOT NULL) y el generador emite la
 * alerta referenciandolo. La OMITIDA es justo lo que refleja el hecho: la toma no se
 * administro.</p>
 */
@Component
public class AlertaRepositoryAdapter implements AlertaRepositoryPort {

    /**
     * Inserta la toma programada del dia como OMITIDA usando el horario como fuente
     * de la hora y el esquema (y con el paciente como salvaguarda).
     */
    private static final String SQL_TOMA_OMITIDA = """
            insert into registro_toma (esquema_id, registrado_por, fecha_hora_programada,
                                       fecha_hora_registro, estado, observaciones, creado_en)
            select e.id,
                   (select u.id from usuario u
                     order by case when u.rol_id = (select r.id from rol r
                                                     where r.nombre = 'ADMINISTRADOR')
                              then 0 else 1 end, u.id
                     limit 1),
                   (cast(:fechaCentro as date) + h.hora) at time zone :zona,
                   null, 'OMITIDA', :observacion, :ahora
            from horario_medicacion h
            join esquema_medicacion e on e.id = h.esquema_id and e.paciente_id = :pacienteId
            where h.id = :horarioId
            returning id
            """;

    /** Id de la toma programada del horario para una fecha dada (si ya existe). */
    private static final String SQL_TOMA_EXISTENTE = """
            select t.id
            from registro_toma t
            join horario_medicacion h on h.esquema_id = t.esquema_id
            where h.id = :horarioId
              and t.fecha_hora_programada = (cast(:fechaCentro as date) + h.hora) at time zone :zona
            """;

    private final AlertaJpaRepository alertas;
    private final NamedParameterJdbcOperations jdbc;
    private final AuditoriaPort auditoria;

    public AlertaRepositoryAdapter(
            AlertaJpaRepository alertas,
            NamedParameterJdbcOperations jdbc,
            AuditoriaPort auditoria) {
        this.alertas = alertas;
        this.jdbc = jdbc;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public Optional<Alerta> guardar(Alerta alerta) {
        AlertaEntity entidad = new AlertaEntity();
        entidad.setPacienteId(alerta.getPacienteId());
        entidad.setTipo(alerta.getTipo());
        entidad.setSeveridad(alerta.getSeveridad());
        entidad.setMensaje(alerta.getMensaje());
        entidad.setOrigen(alerta.getOrigen());
        entidad.setSignosId(alerta.getSignosId());
        entidad.setTomaId(alerta.getTomaId());
        entidad.setCitaId(alerta.getCitaId());
        entidad.setEstado(alerta.getEstado());
        entidad.setRevisadaEn(alerta.getRevisadaEn());
        entidad.setRevisadaPor(alerta.getRevisadaPor());
        try {
            return Optional.of(aDominio(alertas.saveAndFlush(entidad)));
        } catch (DataIntegrityViolationException excepcion) {
            // Alerta duplicada: la fuente ya fue alertada por otra ejecucion. No
            // basta con devolver vacio: PostgreSQL deja la transaccion abortada
            // despues del error, asi que se propaga y se revierte la pasada completa
            // (el siguiente ciclo vuelve a evaluar las fuentes, que ya excluyen las
            // que tienen alerta).
            throw excepcion;
        }
    }

    @Override
    @Transactional
    public TomaLocalizada localizarOMaterializarTomaOmitida(
            Long horarioId, Long pacienteId, String fechaCentro, String zona, Instant ahora) {
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("horarioId", horarioId)
                .addValue("pacienteId", pacienteId)
                .addValue("fechaCentro", fechaCentro)
                .addValue("zona", zona)
                .addValue("observacion", "Toma no registrada: marcada como omitida por el sistema")
                .addValue("ahora", Timestamp.from(ahora));
        Long existente = jdbc.query(SQL_TOMA_EXISTENTE, parametros,
                (rs, fila) -> rs.getLong("id")).stream().findFirst().orElse(null);
        if (existente != null) {
            return new TomaLocalizada(existente, true);
        }
        try {
            Long creada = jdbc.queryForObject(SQL_TOMA_OMITIDA, parametros, Long.class);
            auditoria.registrar(pacienteId, AccionAuditoria.CREATE, "registro_toma", creada,
                    ResultadoAuditoria.EXITOSO,
                    "Toma atrasada marcada como OMITIDA por la generacion automatica");
            return new TomaLocalizada(creada, false);
        } catch (DataIntegrityViolationException excepcion) {
            // Otra ejecucion la creo entre la consulta y el insert
            // (uq_toma_esquema_programada).
            Long otra = jdbc.query(SQL_TOMA_EXISTENTE, parametros,
                    (rs, fila) -> rs.getLong("id")).stream().findFirst()
                    .orElseThrow(() -> new ConflictoDeNegocioException(
                            "No se pudo registrar la toma omitida"));
            return new TomaLocalizada(otra, false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alerta> buscarPorId(Long id) {
        return alertas.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional
    public Alerta actualizar(Alerta alerta) {
        AlertaEntity entidad = alertas.findById(alerta.getId()).orElseThrow();
        entidad.setEstado(alerta.getEstado());
        entidad.setRevisadaEn(alerta.getRevisadaEn());
        entidad.setRevisadaPor(alerta.getRevisadaPor());
        return aDominio(alertas.save(entidad));
    }

    @Override
    public long contar() {
        return alertas.count();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacientes(
            List<Long> pacienteIds, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacientes(pacienteIds, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacientesYEstado(
            List<Long> pacienteIds, EstadoAlerta estado, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacientesYEstado(pacienteIds, estado, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacientesYTipo(
            List<Long> pacienteIds, TipoAlerta tipo, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacientesYTipo(pacienteIds, tipo, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacientesEstadoYTipo(
            List<Long> pacienteIds, EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacientesEstadoYTipo(
                pacienteIds, estado, tipo, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPaciente(
            Long pacienteId, int pagina, int tamano) {
        return aPagina(alertas.listarPorPaciente(pacienteId, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacienteYEstado(
            Long pacienteId, EstadoAlerta estado, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacienteYEstado(pacienteId, estado, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacienteYTipo(
            Long pacienteId, TipoAlerta tipo, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacienteYTipo(pacienteId, tipo, pageable(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAlertasDominio listarPorPacienteEstadoYTipo(
            Long pacienteId, EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano) {
        return aPagina(alertas.listarPorPacienteEstadoYTipo(
                pacienteId, estado, tipo, pageable(pagina, tamano)));
    }

    private PaginaAlertasDominio aPagina(Page<AlertaEntity> pagina) {
        List<Alerta> contenido = pagina.getContent().stream().map(this::aDominio).toList();
        return new PaginaAlertasDominio(contenido, pagina.getTotalElements());
    }

    private static Pageable pageable(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano);
    }

    private Alerta aDominio(AlertaEntity entidad) {
        return new Alerta(
                entidad.getId(),
                entidad.getPacienteId(),
                entidad.getTipo(),
                entidad.getSeveridad(),
                entidad.getMensaje(),
                entidad.getOrigen(),
                entidad.getSignosId(),
                entidad.getTomaId(),
                entidad.getCitaId(),
                entidad.getEstado(),
                entidad.getCreadoEn(),
                entidad.getRevisadaEn(),
                entidad.getRevisadaPor());
    }
}