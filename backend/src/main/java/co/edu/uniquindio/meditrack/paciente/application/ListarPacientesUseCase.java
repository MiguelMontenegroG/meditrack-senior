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
 * siempre {@code activo = true} dentro de su alcance. El listado nunca devuelve
 * pacientes fuera del alcance del usuario.</p>
 */
public class ListarPacientesUseCase {

    private final PacienteRepositoryPort pacientes;

    public ListarPacientesUseCase(PacienteRepositoryPort pacientes) {
        this.pacientes = pacientes;
    }

    public List<Paciente> ejecutar(String texto, Boolean activo, AlcancePaciente alcance, int pagina, int tamano) {
        Boolean filtroActivo = alcance.puedeVerInactivos() ? activo : Boolean.TRUE;
        return pacientes.listar(texto, filtroActivo, alcance.rol(), alcance.usuarioId(), pagina, tamano);
    }

    public long contar(String texto, Boolean activo, AlcancePaciente alcance) {
        Boolean filtroActivo = alcance.puedeVerInactivos() ? activo : Boolean.TRUE;
        return pacientes.contar(texto, filtroActivo, alcance.rol(), alcance.usuarioId());
    }
}
