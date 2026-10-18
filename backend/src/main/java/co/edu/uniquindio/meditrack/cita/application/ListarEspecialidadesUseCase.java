package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort.Especialidad;

import java.util.List;

/**
 * Caso de uso: listar el catalogo de especialidades medicas (solo lectura).
 *
 * <p>El catalogo se siembra en la migracion V3 y no tiene estado: no existe una
 * nocion de "especialidad activa" en el esquema, por lo que se listan todas.</p>
 */
public class ListarEspecialidadesUseCase {

    private final EspecialidadRepositoryPort especialidades;

    public ListarEspecialidadesUseCase(EspecialidadRepositoryPort especialidades) {
        this.especialidades = especialidades;
    }

    public List<Especialidad> ejecutar() {
        return especialidades.listarTodas();
    }
}
