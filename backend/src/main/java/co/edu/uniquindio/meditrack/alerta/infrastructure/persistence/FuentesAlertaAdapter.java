package co.edu.uniquindio.meditrack.alerta.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Adaptador que ejecuta las tres consultas de fuentes de alerta.
 *
 * <p>Se usa {@link NamedParameterJdbcOperations} (inyectado como
 * {@code NamedParameterJdbcTemplate}, ya disponible por {@code spring-jdbc}, sin
 * dependencias nuevas) en lugar de JPQL porque las condiciones "no existe registro
 * para esa toma", "residente activo" y "umbral de referencia sobre columnas
 * nullable" se expresan de forma directa en SQL y en una sola consulta por fuente:
 * no hay N+1 ni parametros nulos condicionales.</p>
 *
 * <p>El esquema de V2 no guarda "la toma de hoy": las tomas programadas se derivan
 * de {@code horario_medicacion} (esquema + hora). Como
 * {@code alerta.toma_id} referencia {@code registro_toma(id)} (FK
 * {@code fk_alerta_toma}), la alerta de una toma atrasada se asocia; cuando no
 * existe el registro programado del dia se crea como {@code OMITIDA} con
 * {@code registrado_por} del sistema antes de insertar la alerta (ver
 * {@code GenerarAlertasUseCase}).</p>
 *
 * <p>Todas las consultas: (1) solo consideran residentes activos, (2) excluyen las
 * fuentes que ya tienen alerta (idempotencia preparada en lectura; el indice unico
 * parcial de V2 es la garantia final) y (3) devuelven solo las columnas necesarias
 * (id de la fuente y del residente), sin datos clinicos. Los {@code Instant} se
 * envian como {@link Timestamp} porque el driver no infiere el tipo de
 * {@code java.time.Instant}.</p>
 */
@Component
public class FuentesAlertaAdapter implements FuentesAlertaPort {

    /**
     * Tomas atrasadas. La toma programada de hoy para un horario es
     * "fecha de hoy en la zona del centro + hora del horario" convertida a
     * TIMESTAMPTZ (igual que {@code registro_toma.fecha_hora_programada}). Se
     * excluye si ya hay registro para ese momento o si el horario ya tiene alerta.
     */
    private static final String SQL_TOMAS_ATRASADAS = """
            select h.id as horario_id, e.paciente_id as paciente_id,
                   (select t.id
                      from registro_toma t
                     where t.esquema_id = e.id
                       and t.fecha_hora_programada = (cast(:fechaCentro as date) + h.hora) at time zone :zona
                     limit 1) as toma_id
            from horario_medicacion h
            join esquema_medicacion e on e.id = h.esquema_id
            join paciente p on p.id = e.paciente_id and p.activo = true
            where e.activo = true
              and e.fecha_inicio <= cast(:fechaCentro as date)
              and (e.fecha_fin is null or e.fecha_fin >= cast(:fechaCentro as date))
              and (cast(:fechaCentro as date) + h.hora) at time zone :zona <= :limite
              and not exists (
                  select 1 from registro_toma t
                  where t.esquema_id = e.id
                    and t.fecha_hora_programada = (cast(:fechaCentro as date) + h.hora) at time zone :zona
              )
              and not exists (
                  select 1 from alerta a where a.toma_id = h.id
              )
            order by e.paciente_id, h.hora
            """;

    /** Citas PROGRAMADA vencidas; no se modifica su estado, solo se alerta. */
    private static final String SQL_CITAS_VENCIDAS = """
            select c.id as cita_id, c.paciente_id as paciente_id
            from cita c
            join paciente p on p.id = c.paciente_id and p.activo = true
            where c.estado = 'PROGRAMADA'
              and c.fecha_hora <= :limite
              and not exists (
                  select 1 from alerta a where a.cita_id = c.id
              )
            order by c.paciente_id, c.fecha_hora
            """;

    /**
     * Signos fuera de los umbrales de referencia. El residente sale de la propia
     * bitacora (registro_signos_vitales.paciente_id); los umbrales se aplican en
     * SQL para no traer la bitacora completa. Las columnas clinicas son nullable:
     * en SQL una comparacion con NULL es falsa y no alerta, que es lo deseado.
     */
    private static final String SQL_SIGNOS_FUERA_DE_RANGO = """
            select s.id as signos_id, s.paciente_id as paciente_id
            from registro_signos_vitales s
            join paciente p on p.id = s.paciente_id and p.activo = true
            where s.fecha_hora >= :desde
              and (
                    s.presion_sistolica  < :sistolicaMin  or s.presion_sistolica  > :sistolicaMax
                 or s.presion_diastolica < :diastolicaMin or s.presion_diastolica > :diastolicaMax
                 or s.glucosa_mg_dl      < :glucosaMin    or s.glucosa_mg_dl      > :glucosaMax
                 or s.temperatura_c      < :temperaturaMin or s.temperatura_c    > :temperaturaMax
              )
              and not exists (
                  select 1 from alerta a where a.signos_id = s.id
              )
            order by s.paciente_id, s.fecha_hora
            """;

    private final NamedParameterJdbcOperations jdbc;

    public FuentesAlertaAdapter(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuenteToma> tomasAtrasadas(Instant limite, String fechaCentro, String zona) {
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("limite", Timestamp.from(limite))
                .addValue("fechaCentro", fechaCentro)
                .addValue("zona", zona);
        return jdbc.query(SQL_TOMAS_ATRASADAS, parametros,
                (rs, fila) -> new FuenteToma(
                        rs.getLong("horario_id"), rs.getLong("paciente_id"),
                        rs.getObject("toma_id", Long.class)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuenteCita> citasProgramadasVencidas(Instant limite) {
        MapSqlParameterSource parametros =
                new MapSqlParameterSource().addValue("limite", Timestamp.from(limite));
        return jdbc.query(SQL_CITAS_VENCIDAS, parametros,
                (rs, fila) -> new FuenteCita(rs.getLong("cita_id"), rs.getLong("paciente_id")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuenteSignos> signosFueraDeRango(Instant desde, UmbralesConsulta umbrales) {
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("desde", Timestamp.from(desde))
                .addValue("sistolicaMin", umbrales.sistolicaMin())
                .addValue("sistolicaMax", umbrales.sistolicaMax())
                .addValue("diastolicaMin", umbrales.diastolicaMin())
                .addValue("diastolicaMax", umbrales.diastolicaMax())
                .addValue("glucosaMin", umbrales.glucosaMin())
                .addValue("glucosaMax", umbrales.glucosaMax())
                .addValue("temperaturaMin", umbrales.temperaturaMin())
                .addValue("temperaturaMax", umbrales.temperaturaMax());
        return jdbc.query(SQL_SIGNOS_FUERA_DE_RANGO, parametros,
                (rs, fila) -> new FuenteSignos(rs.getLong("signos_id"), rs.getLong("paciente_id")));
    }
}