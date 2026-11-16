package co.edu.uniquindio.meditrack.alerta.application.port;

import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de alertas.
 *
 * <p>Los filtros opcionales (estado, tipo y residente concreto) se exponen como
 * <b>metodos separados</b>, igual que en los modulos paciente, medicacion, cita y
 * bitacora: nunca se envia a PostgreSQL un parametro nulo y no se pasa el rol como
 * parametro a la consulta. El caso de uso elige el metodo segun que filtros llegan
 * informados.</p>
 *
 * <p>Las alertas son append-only salvo la atencion (ACTIVA -> REVISADA): no hay
 * borrado. {@code guardar} devuelve {@code Optional.empty()} cuando la base
 * rechaza el alta por violar un indice unico parcial
 * ({@code uq_alerta_toma}/{@code uq_alerta_cita}/{@code uq_alerta_signos}), que es
 * el mecanismo de idempotencia de la generacion automatica.</p>
 */
public interface AlertaRepositoryPort {

    /** Persiste una alerta nueva; vacio si ya existia una alerta para su fuente. */
    Optional<Alerta> guardar(Alerta alerta);

    Optional<Alerta> buscarPorId(Long id);

    /** Actualiza el estado y los datos de atencion de una alerta existente. */
    Alerta actualizar(Alerta alerta);

    // ------------------------------------------------------------------
    // Listado por conjunto de residentes visibles (sin filtro de residente).
    // ------------------------------------------------------------------

    PaginaAlertasDominio listarPorPacientes(List<Long> pacienteIds, int pagina, int tamano);

    PaginaAlertasDominio listarPorPacientesYEstado(
            List<Long> pacienteIds, EstadoAlerta estado, int pagina, int tamano);

    PaginaAlertasDominio listarPorPacientesYTipo(
            List<Long> pacienteIds, TipoAlerta tipo, int pagina, int tamano);

    PaginaAlertasDominio listarPorPacientesEstadoYTipo(
            List<Long> pacienteIds, EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano);

    // ------------------------------------------------------------------
    // Listado de un residente concreto.
    // ------------------------------------------------------------------

    PaginaAlertasDominio listarPorPaciente(Long pacienteId, int pagina, int tamano);

    PaginaAlertasDominio listarPorPacienteYEstado(
            Long pacienteId, EstadoAlerta estado, int pagina, int tamano);

    PaginaAlertasDominio listarPorPacienteYTipo(
            Long pacienteId, TipoAlerta tipo, int pagina, int tamano);

    PaginaAlertasDominio listarPorPacienteEstadoYTipo(
            Long pacienteId, EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano);

    /**
     * Localiza la toma programada del dia para el horario dado; si no existe la
     * materializa como OMITIDA y devuelve su id.
     *
     * <p>Es necesario porque {@code alerta.toma_id} tiene clave foranea a
     * {@code registro_toma(id)} y el esquema V2 no guarda las tomas futuras: la
     * primera vez que se alerta una toma atrasada hay que crear la fila. La toma
     * queda OMITIDA (lo que de hecho ocurrio), con una observacion que deja claro
     * que la marco el sistema.</p>
     *
     * @param horarioId   horario que origino la toma
     * @param pacienteId  residente dueno del esquema
     * @param fechaCentro fecha del centro en ISO-8601 ({@code yyyy-MM-dd})
     * @param zona        zona horaria del centro
     * @param ahora       instante de la generacion
     */
    TomaLocalizada localizarOMaterializarTomaOmitida(
            Long horarioId, Long pacienteId, String fechaCentro, String zona, Instant ahora);

    /** Numero total de alertas (solo para el resumen de la generacion). */
    long contar();

    /**
     * Toma ya asociada a la alerta.
     *
     * @param id       id de {@code registro_toma}
     * @param existente {@code true} si la fila ya existia (no se creo aqui)
     */
    record TomaLocalizada(Long id, boolean existente) {
    }
}
