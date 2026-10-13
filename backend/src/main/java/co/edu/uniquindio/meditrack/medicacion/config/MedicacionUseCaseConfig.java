package co.edu.uniquindio.meditrack.medicacion.config;

import co.edu.uniquindio.meditrack.medicacion.application.ActualizarEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ActualizarMedicamentoUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.CambiarEstadoEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.CambiarEstadoMedicamentoUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ConsultarAgendaDiaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.CrearEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.CrearMedicamentoUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ListarEsquemasUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ListarHistorialTomasUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ListarMedicamentosUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ObtenerEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.RegistrarTomaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.application.port.RegistroTomaRepositoryPort;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra los casos de uso del modulo medicacion como beans de Spring.
 *
 * <p>Los casos de uso son POJOs sin anotaciones de Spring; se cablean aqui para
 * mantener la independencia del framework. Se reutiliza el bean {@code Clock}
 * definido en {@code PacienteUseCaseConfig} (no se crea otro) para que la fecha
 * sea coherente en toda la aplicacion y testeable.</p>
 */
@Configuration
public class MedicacionUseCaseConfig {

    @Bean
    public VerificadorAlcanceResidente verificadorAlcanceResidente(
            PacienteAlcancePort pacientes, AuditoriaPort auditoria) {
        return new VerificadorAlcanceResidente(pacientes, auditoria);
    }

    // Catalogo de medicamentos.

    @Bean
    public CrearMedicamentoUseCase crearMedicamentoUseCase(
            MedicamentoRepositoryPort medicamentos, AuditoriaPort auditoria) {
        return new CrearMedicamentoUseCase(medicamentos, auditoria);
    }

    @Bean
    public ActualizarMedicamentoUseCase actualizarMedicamentoUseCase(
            MedicamentoRepositoryPort medicamentos, AuditoriaPort auditoria) {
        return new ActualizarMedicamentoUseCase(medicamentos, auditoria);
    }

    @Bean
    public CambiarEstadoMedicamentoUseCase cambiarEstadoMedicamentoUseCase(
            MedicamentoRepositoryPort medicamentos, AuditoriaPort auditoria) {
        return new CambiarEstadoMedicamentoUseCase(medicamentos, auditoria);
    }

    @Bean
    public ListarMedicamentosUseCase listarMedicamentosUseCase(MedicamentoRepositoryPort medicamentos) {
        return new ListarMedicamentosUseCase(medicamentos);
    }

    // Esquemas de medicacion.

    @Bean
    public CrearEsquemaUseCase crearEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            MedicamentoRepositoryPort medicamentos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new CrearEsquemaUseCase(esquemas, medicamentos, alcance, auditoria);
    }

    @Bean
    public ActualizarEsquemaUseCase actualizarEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            MedicamentoRepositoryPort medicamentos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ActualizarEsquemaUseCase(esquemas, medicamentos, alcance, auditoria);
    }

    @Bean
    public CambiarEstadoEsquemaUseCase cambiarEstadoEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new CambiarEstadoEsquemaUseCase(esquemas, alcance, auditoria);
    }

    @Bean
    public ListarEsquemasUseCase listarEsquemasUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ListarEsquemasUseCase(esquemas, alcance, auditoria);
    }

    @Bean
    public ObtenerEsquemaUseCase obtenerEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ObtenerEsquemaUseCase(esquemas, alcance, auditoria);
    }

    // Tomas.

    @Bean
    public RegistrarTomaUseCase registrarTomaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            RegistroTomaRepositoryPort tomas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new RegistrarTomaUseCase(esquemas, tomas, alcance, auditoria, clock);
    }

    @Bean
    public ListarHistorialTomasUseCase listarHistorialTomasUseCase(
            RegistroTomaRepositoryPort tomas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ListarHistorialTomasUseCase(tomas, alcance, auditoria);
    }

    @Bean
    public ConsultarAgendaDiaUseCase consultarAgendaDiaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            RegistroTomaRepositoryPort tomas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new ConsultarAgendaDiaUseCase(esquemas, tomas, alcance, auditoria, clock);
    }
}
