package co.edu.uniquindio.meditrack.bitacora.application.port;

import co.edu.uniquindio.meditrack.bitacora.application.PaginaSignosVitalesDominio;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;

import java.time.Instant;
import java.util.Optional;

/**
 * Puerto de persistencia de la bitacora de signos vitales.
 *
 * <p>Los filtros opcionales del rango de fechas ({@code desde}/{@code hasta}) se
 * exponen como <b>metodos separados</b> por combinacion, igual que en los modulos
 * paciente, medicacion y cita: nunca se envia a PostgreSQL un parametro nulo y no
 * se pasa el rol como parametro a la consulta. El caso de uso elige el metodo
 * segun que filtros llegan informados.</p>
 *
 * <p>Los registros son inmutables: solo hay {@code guardar} (altas). No hay
 * actualizaciones ni borrados.</p>
 */
public interface SignosVitalesRepositoryPort {

    /** Persiste un registro nuevo (sin id) y lo devuelve con su id asignado. */
    RegistroSignosVitales guardar(RegistroSignosVitales registro);

    /**
     * Historial paginado de un residente, sin filtro de fechas, ordenado por fecha
     * descendente (registro mas reciente primero).
     */
    PaginaSignosVitalesDominio listarPorPaciente(Long pacienteId, int pagina, int tamano);

    /** Historial paginado desde una fecha (incluida), orden descendente. */
    PaginaSignosVitalesDominio listarPorPacienteDesde(
            Long pacienteId, Instant desde, int pagina, int tamano);

    /** Historial paginado hasta una fecha (incluida), orden descendente. */
    PaginaSignosVitalesDominio listarPorPacienteHasta(
            Long pacienteId, Instant hasta, int pagina, int tamano);

    /** Historial paginado por rango de fechas (ambos extremos incluidos), orden descendente. */
    PaginaSignosVitalesDominio listarPorPacienteYRango(
            Long pacienteId, Instant desde, Instant hasta, int pagina, int tamano);

    /** Registro mas reciente de un residente, si tiene alguno. */
    Optional<RegistroSignosVitales> buscarUltimoPorPaciente(Long pacienteId);
}
