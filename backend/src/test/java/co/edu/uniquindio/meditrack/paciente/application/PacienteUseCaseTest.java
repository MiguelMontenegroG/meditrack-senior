package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas de creacion de paciente: documento duplicado, fecha de
 * nacimiento futura, situacion clinica obligatoria y habitacion opcional.
 */
class PacienteUseCaseTest {

    private static final LocalDate HOY = LocalDate.of(2026, 1, 15);

    private PacienteRepositoryPort pacientes;
    private AuditoriaPort auditoria;
    private Clock reloj;

    @BeforeEach
    void configurar() {
        pacientes = mock(PacienteRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
        reloj = Clock.fixed(Instant.parse("2026-01-15T12:00:00Z"), ZoneOffset.UTC);
    }

    private ComandoCrearPaciente comando(LocalDate nacimiento) {
        return new ComandoCrearPaciente(
                "1001", "Ana", "Gomez", nacimiento, Sexo.FEMENINO,
                "101", "Hipertension controlada", HOY);
    }

    @Test
    void crearPacienteConDocumentoDuplicadoFalla() {
        when(pacientes.existePorDocumento("1001")).thenReturn(true);
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(comando(LocalDate.of(1940, 5, 1)), 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("documento");
    }

    @Test
    void crearPacienteConFechaDeNacimientoFuturaFalla() {
        when(pacientes.existePorDocumento("1001")).thenReturn(false);
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(comando(HOY.plusDays(1)), 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("futura");
    }

    @Test
    void crearPacienteValidoCalculaEdadYAudita() {
        when(pacientes.existePorDocumento("1001")).thenReturn(false);
        when(pacientes.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);

        Paciente creado = caso.ejecutar(comando(LocalDate.of(1940, 1, 10)), 1L);

        assertThat(creado.edad(HOY)).isEqualTo(86);
    }

    @Test
    void crearPacienteSinSituacionClinicaFalla() {
        when(pacientes.existePorDocumento("1001")).thenReturn(false);
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);
        ComandoCrearPaciente sinSituacion = new ComandoCrearPaciente(
                "1001", "Ana", "Gomez", LocalDate.of(1940, 5, 1), Sexo.FEMENINO,
                "101", null, HOY);

        assertThatThrownBy(() -> caso.ejecutar(sinSituacion, 1L))
                .isInstanceOf(ValidacionDeNegocioException.class)
                .hasMessageContaining("situacion clinica");
    }

    @Test
    void crearPacienteSinHabitacionEsValidoPorqueEsOpcional() {
        when(pacientes.existePorDocumento("1001")).thenReturn(false);
        when(pacientes.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);
        ComandoCrearPaciente sinHabitacion = new ComandoCrearPaciente(
                "1001", "Ana", "Gomez", LocalDate.of(1940, 5, 1), Sexo.FEMENINO,
                null, "Hipertension controlada", HOY);

        Paciente creado = caso.ejecutar(sinHabitacion, 1L);

        assertThat(creado.getHabitacion()).isNull();
        assertThat(creado.getSituacionClinica()).isEqualTo("Hipertension controlada");
        assertThat(creado.datosClinicosPendientes()).isEmpty();
    }

    @Test
    void crearPacienteConFechaIngresoFuturaFalla() {
        when(pacientes.existePorDocumento("1001")).thenReturn(false);
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);
        ComandoCrearPaciente ingresoFuturo = new ComandoCrearPaciente(
                "1001", "Ana", "Gomez", LocalDate.of(1940, 5, 1), Sexo.FEMENINO,
                "101", "Hipertension controlada", HOY.plusDays(1));

        assertThatThrownBy(() -> caso.ejecutar(ingresoFuturo, 1L))
                .isInstanceOf(ValidacionDeNegocioException.class)
                .hasMessageContaining("ingreso");
    }

    @Test
    void crearPacienteCompletoGuardaHabitacionYSituacionClinica() {
        when(pacientes.existePorDocumento("1001")).thenReturn(false);
        when(pacientes.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        CrearPacienteUseCase caso = new CrearPacienteUseCase(pacientes, auditoria, reloj);
        ComandoCrearPaciente completo = new ComandoCrearPaciente(
                "1001", "Ana", "Gomez", LocalDate.of(1940, 5, 1), Sexo.FEMENINO,
                " 101 ", " Hipertension controlada ", HOY);

        Paciente creado = caso.ejecutar(completo, 1L);

        assertThat(creado.getHabitacion()).isEqualTo("101");
        assertThat(creado.getSituacionClinica()).isEqualTo("Hipertension controlada");
        assertThat(creado.datosClinicosPendientes()).isEmpty();
    }
}
