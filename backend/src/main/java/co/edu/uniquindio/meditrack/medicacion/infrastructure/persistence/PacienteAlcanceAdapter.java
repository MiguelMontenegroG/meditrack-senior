package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * Adaptador que resuelve el alcance de un residente para el modulo de medicacion
 * reutilizando el {@link PacienteRepositoryPort} del modulo paciente (no se
 * duplica la logica de alcance ni se accede a sus tablas directamente).
 *
 * <p>El alcance se elige por rol con metodos separados, igual que en el modulo
 * paciente, para evitar el problema de tipado de Hibernate 6.6.</p>
 */
@Component
public class PacienteAlcanceAdapter implements PacienteAlcancePort {

    private final PacienteRepositoryPort pacientes;

    public PacienteAlcanceAdapter(PacienteRepositoryPort pacientes) {
        this.pacientes = pacientes;
    }

    @Override
    public boolean existe(Long pacienteId) {
        return pacientes.buscarPorId(pacienteId).isPresent();
    }

    @Override
    public boolean estaActivo(Long pacienteId) {
        return pacientes.buscarPorId(pacienteId).map(p -> p.isActivo()).orElse(false);
    }

    @Override
    public boolean visible(Long pacienteId, Long usuarioId, RolUsuario rol) {
        return switch (rol) {
            case ADMINISTRADOR -> pacientes.esVisibleParaAdmin(pacienteId);
            case CUIDADOR_ENFERMERO -> pacientes.esVisibleParaCuidador(pacienteId, usuarioId);
            case FAMILIAR_AUTORIZADO -> pacientes.esVisibleParaFamiliar(pacienteId, usuarioId);
        };
    }
}
