package co.edu.uniquindio.meditrack.alerta.config;

import co.edu.uniquindio.meditrack.alerta.application.AtenderAlertaUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ConfiguracionAlertas;
import co.edu.uniquindio.meditrack.alerta.application.GenerarAlertasUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ListarAlertasUseCase;
import co.edu.uniquindio.meditrack.alerta.application.port.AlertaRepositoryPort;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.time.Clock;

/**
 * Registra los casos de uso del modulo de alertas como beans de Spring y habilita
 * la planificacion de tareas.
 *
 * <p>Los casos de uso son POJOs sin anotaciones de Spring; se cablean aqui. Se
 * reutilizan el {@link VerificadorAlcanceResidente} (modulo medicacion), el bean
 * {@code Clock} compartido (America/Bogota, definido en {@code PacienteUseCaseConfig})
 * y el {@link PacienteAlcancePort} para no duplicar el alcance.</p>
 *
 * <p>{@code @EnableScheduling} se declara en este modulo: la generacion automatica
 * corre en un componente {@code @Scheduled} fino (ver
 * {@code infrastructure/scheduling}) que solo delega en el caso de uso. En las
 * pruebas la generacion queda deshabilitada por propiedad.</p>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(PropiedadesAlertas.class)
public class AlertaUseCaseConfig {

    /**
     * Plantilla JDBC de parametros con nombre para las consultas de fuentes de
     * alerta. Se declara explicitamente sobre el {@code DataSource} para no
     * depender de la autoconfiguracion JDBC de Spring Boot.
     */
    @Bean
    public NamedParameterJdbcOperations alertasJdbc(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public ConfiguracionAlertas configuracionAlertas(PropiedadesAlertas propiedades) {
        return propiedades.aConfiguracion();
    }

    @Bean
    public GenerarAlertasUseCase generarAlertasUseCase(
            FuentesAlertaPort fuentes,
            AlertaRepositoryPort alertas,
            ConfiguracionAlertas configuracion,
            Clock clock) {
        return new GenerarAlertasUseCase(fuentes, alertas, configuracion, clock);
    }

    @Bean
    public ListarAlertasUseCase listarAlertasUseCase(
            AlertaRepositoryPort alertas,
            PacienteRepositoryPort pacientes,
            PacienteAlcancePort alcancePaciente,
            AuditoriaPort auditoria) {
        return new ListarAlertasUseCase(alertas, pacientes, alcancePaciente, auditoria);
    }

    @Bean
    public AtenderAlertaUseCase atenderAlertaUseCase(
            AlertaRepositoryPort alertas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new AtenderAlertaUseCase(alertas, alcance, auditoria, clock);
    }
}
