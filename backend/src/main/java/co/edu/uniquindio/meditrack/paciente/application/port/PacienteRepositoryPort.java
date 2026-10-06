package co.edu.uniquindio.meditrack.paciente.application.port;

import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.RolUsuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de pacientes. Incluye consultas por alcance para que
 * el filtro de visibilidad por rol se aplique en la base de datos y no en
 * memoria.
 */
public interface PacienteRepositoryPort {

    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    boolean existePorDocumento(String documento);

    boolean existePorDocumentoEnOtro(String documento, Long idExcluido);

    /**
     * Lista paginada filtrada por alcance.
     *
     * @param usuarioId id del usuario que consulta (ignorado si es administrador)
     * @param rol       rol del usuario que consulta
     * @param texto     busqueda por nombres, apellidos o documento (opcional)
     * @param activo    filtro por estado (solo relevante para administrador)
     * @param pagina    numero de pagina (base 0)
     * @param tamano    tamano de pagina
     */
    List<Paciente> listar(String texto, Boolean activo, RolUsuario rol, Long usuarioId, int pagina, int tamano);

    long contar(String texto, Boolean activo, RolUsuario rol, Long usuarioId);

    /**
     * Indica si el usuario puede ver al paciente segun su alcance.
     * El administrador ve todos; el cuidador solo sus asignaciones vigentes;
     * el familiar solo vinculaciones autorizadas.
     */
    boolean esVisiblePara(Long pacienteId, RolUsuario rol, Long usuarioId);
}
