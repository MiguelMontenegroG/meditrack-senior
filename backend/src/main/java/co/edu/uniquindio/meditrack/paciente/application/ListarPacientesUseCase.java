package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;

import java.util.List;

/**
 * Caso de uso: listar pacientes de forma paginada, filtrando por el alcance del
 * usuario.
 *
 * <p>Solo el administrador puede ver pacientes inactivos; cualquier otro rol ve
 * siempre {@code activo = true} dentro de su alcance. El caso de uso elige el
 * metodo de repositorio segun el alcance, de modo que la restriccion de
 * visibilidad vive en la consulta y no se pasa el rol como parametro.</p>
 */
public class ListarPacientesUseCase {

    private final PacienteRepositoryPort pacientes;

    public ListarPacientesUseCase(PacienteRepositoryPort pacientes) {
        this.pacientes = pacientes;
    }

    public List<Paciente> ejecutar(String texto, Boolean activo, AlcancePaciente alcance, int pagina, int tamano) {
        Boolean filtroActivo = alcance.puedeVerInactivos() ? activo : Boolean.TRUE;
        return switch (alcance.rol()) {
            case ADMINISTRADOR -> pacientes.listarTodos(texto, filtroActivo, pagina, tamano);
            case CUIDADOR_ENFERMERO -> pacientes.listarPorCuidador(texto, filtroActivo, alcance.usuarioId(), pagina, tamano);
            case FAMILIAR_AUTORIZADO -> pacientes.listarPorFamiliar(texto, filtroActivo, alcance.usuarioId(), pagina, tamano);
        };
    }

    public long contar(String texto, Boolean activo, AlcancePaciente alcance) {
        Boolean filtroActivo = alcance.puedeVerInactivos() ? activo : Boolean.TRUE;
        return switch (alcance.rol()) {
            case ADMINISTRADOR -> pacientes.contarTodos(texto, filtroActivo);
            case CUIDADOR_ENFERMERO -> pacientes.contarPorCuidador(texto, filtroActivo, alcance.usuarioId());
            case FAMILIAR_AUTORIZADO -> pacientes.contarPorFamiliar(texto, filtroActivo, alcance.usuarioId());
        };
    }
}
