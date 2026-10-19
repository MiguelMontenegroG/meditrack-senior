package co.edu.uniquindio.meditrack.bitacora.config;

import co.edu.uniquindio.meditrack.bitacora.application.ListarSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.ObtenerUltimoSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.RegistrarSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.port.SignosVitalesRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra los casos de uso del modulo de bitacora de signos vitales como beans de
 * Spring.
 *
 * <p>Los casos de uso son POJOs sin anotaciones de Spring; se cablean aqui. Se
 * reutilizan el {@link VerificadorAlcanceResidente} (definido en
 * {@code MedicacionUseCaseConfig}) y el bean {@code Clock} (definido en
 * {@code PacienteUseCaseConfig}) para no duplicar el alcance ni el reloj.</p>
 */
@Configuration
public class BitacoraUseCaseConfig {

    @Bean
    public RegistrarSignosVitalesUseCase registrarSignosVitalesUseCase(
            SignosVitalesRepositoryPort signos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria,
            Clock clock) {
        return new RegistrarSignosVitalesUseCase(signos, alcance, auditoria, clock);
    }

    @Bean
    public ListarSignosVitalesUseCase listarSignosVitalesUseCase(
            SignosVitalesRepositoryPort signos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ListarSignosVitalesUseCase(signos, alcance, auditoria);
    }

    @Bean
    public ObtenerUltimoSignosVitalesUseCase obtenerUltimoSignosVitalesUseCase(
            SignosVitalesRepositoryPort signos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        return new ObtenerUltimoSignosVitalesUseCase(signos, alcance, auditoria);
    }
}
