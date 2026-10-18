package co.edu.uniquindio.meditrack.cita.config;

import co.edu.uniquindio.meditrack.cita.application.AgendarCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.CambiarEstadoCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.EditarCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.ListarCalendarioCitasUseCase;
import co.edu.uniquindio.meditrack.cita.application.ListarEspecialidadesUseCase;
import co.edu.uniquindio.meditrack.cita.application.ObtenerCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra los casos de uso del modulo de citas como beans de Spring.
 *
 * <p>Los casos de uso son POJOs sin anotaciones de Spring; se cablean aqui. Se
 * reutilizan el {@link VerificadorAlcanceResidente} (definido en
 * {@code MedicacionUseCaseConfig}) y el bean {@code Clock} (definido en
 * {@code PacienteUseCaseConfig}) para no duplicar el alcance ni el reloj.</p>
 */
@Configuration
public class CitaUseCaseConfig {

    @Bean
    public AgendarCitaUseCase agendarCitaUseCase(
            CitaRepositoryPort citas,
            EspecialidadRepositoryPort especialidades,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new AgendarCitaUseCase(citas, especialidades, alcance, auditoria, clock);
    }

    @Bean
    public EditarCitaUseCase editarCitaUseCase(
            CitaRepositoryPort citas,
            EspecialidadRepositoryPort especialidades,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new EditarCitaUseCase(citas, especialidades, alcance, auditoria, clock);
    }

    @Bean
    public CambiarEstadoCitaUseCase cambiarEstadoCitaUseCase(
            CitaRepositoryPort citas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new CambiarEstadoCitaUseCase(citas, alcance, auditoria, clock);
    }

    @Bean
    public ObtenerCitaUseCase obtenerCitaUseCase(
            CitaRepositoryPort citas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ObtenerCitaUseCase(citas, alcance, auditoria);
    }

    @Bean
    public ListarCalendarioCitasUseCase listarCalendarioCitasUseCase(
            CitaRepositoryPort citas,
            PacienteRepositoryPort pacientes,
            PacienteAlcancePort alcancePaciente,
            AuditoriaPort auditoria) {
        return new ListarCalendarioCitasUseCase(citas, pacientes, alcancePaciente, auditoria);
    }

    @Bean
    public ListarEspecialidadesUseCase listarEspecialidadesUseCase(
            EspecialidadRepositoryPort especialidades) {
        return new ListarEspecialidadesUseCase(especialidades);
    }
}