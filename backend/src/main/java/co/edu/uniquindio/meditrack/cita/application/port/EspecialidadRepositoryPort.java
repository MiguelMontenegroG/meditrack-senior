package co.edu.uniquindio.meditrack.cita.application.port;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de consulta del catalogo de especialidades medicas (solo lectura).
 *
 * <p>El catalogo se siembra en la migracion V3 y no tiene columna de estado en
 * el esquema (V2): no existe el concepto de "especialidad activa". Una cita
 * puede ademas registrarse sin especialidad ({@code especialidad_id} es nulo en
 * el esquema), por ejemplo para una visita general.</p>
 */
public interface EspecialidadRepositoryPort {

    /** Indica si la especialidad existe en el catalogo. */
    boolean existe(Long id);

    /** Busca el nombre de una especialidad por id. */
    Optional<String> nombrePorId(Long id);

    /** Nombre de las especialidades indicadas, indexado por id (para listados). */
    List<Especialidad> listarTodas();

    /** Proyeccion minima de una especialidad. */
    record Especialidad(Long id, String nombre) {
    }
}
