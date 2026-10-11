package co.edu.uniquindio.meditrack.paciente.application.port;

import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de pacientes. Incluye consultas por alcance para que
 * el filtro de visibilidad por rol se aplique en la base de datos y no en
 * memoria.
 *
 * <p>El alcance se expone como <b>metodos separados</b> en lugar de un parametro
 * de rol: pasar el rol como parametro y compararlo contra literales de enum en
 * la consulta rompe la determinacion del tipo en Hibernate 6. El caso de uso
 * elige el metodo segun el rol del usuario que consulta.</p>
 */
public interface PacienteRepositoryPort {

    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    boolean existePorDocumento(String documento);

    boolean existePorDocumentoEnOtro(String documento, Long idExcluido);

    // ------------------------------------------------------------------
    // Listado y conteo por alcance.
    // ------------------------------------------------------------------

    /** Listado completo (alcance ADMINISTRADOR). */
    List<Paciente> listarTodos(String texto, Boolean activo, int pagina, int tamano);

    long contarTodos(String texto, Boolean activo);

    /** Listado limitado a las asignaciones vigentes del cuidador. */
    List<Paciente> listarPorCuidador(String texto, Boolean activo, Long cuidadorId, int pagina, int tamano);

    long contarPorCuidador(String texto, Boolean activo, Long cuidadorId);

    /** Listado limitado a las vinculaciones autorizadas del familiar. */
    List<Paciente> listarPorFamiliar(String texto, Boolean activo, Long familiarId, int pagina, int tamano);

    long contarPorFamiliar(String texto, Boolean activo, Long familiarId);

    // ------------------------------------------------------------------
    // Visibilidad de un paciente concreto, por alcance.
    // ------------------------------------------------------------------

    /** Indica si el paciente existe (alcance ADMINISTRADOR, que ve todo). */
    boolean esVisibleParaAdmin(Long pacienteId);

    /** Indica si el cuidador tiene asignacion vigente sobre el paciente. */
    boolean esVisibleParaCuidador(Long pacienteId, Long cuidadorId);

    /** Indica si el familiar tiene vinculacion autorizada sobre el paciente. */
    boolean esVisibleParaFamiliar(Long pacienteId, Long familiarId);
}
