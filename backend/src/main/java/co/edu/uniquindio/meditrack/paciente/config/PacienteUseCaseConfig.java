package co.edu.uniquindio.meditrack.paciente.config;

import co.edu.uniquindio.meditrack.paciente.application.ActualizarPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.CambiarEstadoPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.CrearPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.GestionarContactoUseCase;
import co.edu.uniquindio.meditrack.paciente.application.GestionarCuidadorUseCase;
import co.edu.uniquindio.meditrack.paciente.application.GestionarFamiliarUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ListarContactosUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ListarPacientesUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ObtenerPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.port.AsignacionCuidadorRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.VinculacionFamiliarRepositoryPort;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra los casos de uso de paciente como beans de Spring y el reloj de la
 * aplicacion.
 *
 * <p>Los casos de uso viven en la capa de aplicacion y no llevan anotaciones de
 * Spring; se cablean aqui para mantener la independencia del framework.</p>
 */
@Configuration
public class PacienteUseCaseConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CrearPacienteUseCase crearPacienteUseCase(
            PacienteRepositoryPort pacientes,
            AuditoriaPort auditoria,
            Clock clock) {
        return new CrearPacienteUseCase(pacientes, auditoria, clock);
    }

    @Bean
    public ListarPacientesUseCase listarPacientesUseCase(PacienteRepositoryPort pacientes) {
        return new ListarPacientesUseCase(pacientes);
    }

    @Bean
    public ObtenerPacienteUseCase obtenerPacienteUseCase(
            PacienteRepositoryPort pacientes,
            AuditoriaPort auditoria) {
        return new ObtenerPacienteUseCase(pacientes, auditoria);
    }

    @Bean
    public ActualizarPacienteUseCase actualizarPacienteUseCase(
            PacienteRepositoryPort pacientes,
            AuditoriaPort auditoria,
            Clock clock) {
        return new ActualizarPacienteUseCase(pacientes, auditoria, clock);
    }

    @Bean
    public CambiarEstadoPacienteUseCase cambiarEstadoPacienteUseCase(
            PacienteRepositoryPort pacientes,
            AuditoriaPort auditoria) {
        return new CambiarEstadoPacienteUseCase(pacientes, auditoria);
    }

    @Bean
    public ListarContactosUseCase listarContactosUseCase(
            PacienteRepositoryPort pacientes,
            ContactoEmergenciaRepositoryPort contactos,
            AuditoriaPort auditoria) {
        return new ListarContactosUseCase(pacientes, contactos, auditoria);
    }

    @Bean
    public GestionarContactoUseCase gestionarContactoUseCase(
            PacienteRepositoryPort pacientes,
            ContactoEmergenciaRepositoryPort contactos,
            AuditoriaPort auditoria) {
        return new GestionarContactoUseCase(pacientes, contactos, auditoria);
    }

    @Bean
    public GestionarFamiliarUseCase gestionarFamiliarUseCase(
            PacienteRepositoryPort pacientes,
            VinculacionFamiliarRepositoryPort vinculaciones,
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria) {
        return new GestionarFamiliarUseCase(pacientes, vinculaciones, usuarios, auditoria);
    }

    @Bean
    public GestionarCuidadorUseCase gestionarCuidadorUseCase(
            PacienteRepositoryPort pacientes,
            AsignacionCuidadorRepositoryPort asignaciones,
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria,
            Clock clock) {
        return new GestionarCuidadorUseCase(pacientes, asignaciones, usuarios, auditoria, clock);
    }
}
