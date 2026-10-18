package co.edu.uniquindio.meditrack.cita.application.port;

import co.edu.uniquindio.meditrack.cita.application.PaginaCitasDominio;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
/**
 * Puerto de persistencia de citas.
 *
 * <p>Los filtros opcionales (estado, rango de fechas, residente concreto o
 * conjunto de residentes visibles) se exponen como <b>metodos separados</b>,
 * igual que en los modulos paciente y medicacion: nunca se envia a PostgreSQL un
 * parametro nulo y no se pasa el rol como parametro a la consulta. El caso de
 * uso elige el metodo segun que filtros llegan informados.</p>
 *
 * <p>Todos los listados son paginados y devuelven un {@link PaginaCitasDominio}
 * con el contenido y el total calculado con el mismo predicado que ese
 * contenido. El rango de fechas es obligatorio en el calendario (el caso de uso
 * lo exige), por lo que solo se separan las combinaciones por presencia de
 * estado y por residente concreto o conjunto de residentes visibles.</p>
 */
public interface CitaRepositoryPort {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(Long id);

    /**
     * Listado paginado de citas de un residente concreto dentro de un rango de
     * fechas. {@code estado} puede ser nulo; el adaptador elige la consulta.
     */
    PaginaCitasDominio listarPorPaciente(
            Long pacienteId, EstadoCita estado, Instant desde, Instant hasta, int pagina, int tamano);

    /**
     * Listado paginado por rango de fechas para el conjunto de residentes
     * visibles. {@code pacienteIds} nunca es vacio (el caso de uso responde
     * pagina vacia sin consultar cuando no hay residentes visibles).
     */
    PaginaCitasDominio listarPorPacientes(
            List<Long> pacienteIds, EstadoCita estado, Instant desde, Instant hasta,
            int pagina, int tamano);
}
