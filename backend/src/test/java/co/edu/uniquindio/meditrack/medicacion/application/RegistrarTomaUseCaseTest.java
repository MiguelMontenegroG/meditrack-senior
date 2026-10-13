package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.application.port.RegistroTomaRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma;
import co.edu.uniquindio.meditrack.medicacion.domain.HorarioMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.medicacion.domain.ViaAdministracion;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link RegistrarTomaUseCase}.
 *
 * <p>Se centran en las reglas que producen 400/409, en especial la validacion de
 * que la hora programada corresponda a un horario del esquema (una hora no
 * programada se rechaza con validacion de negocio, antes no existia esa
 * comprobacion). El reloj se fija para que el calculo sea determinista.</p>
 */
class RegistrarTomaUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-01-15T12:00:00Z");
    private static final Long PACIENTE = 10L;
    private static final Long ESQUEMA = 55L;
    private static final Long AUTOR = 7L;

    private EsquemaMedicacionRepositoryPort esquemas;
    private RegistroTomaRepositoryPort tomas;
    private PacienteAlcancePort alcancePaciente;
    private AuditoriaPort auditoria;
    private RegistrarTomaUseCase caso;

    @BeforeEach
    void configurar() {
        esquemas = mock(EsquemaMedicacionRepositoryPort.class);
        tomas = mock(RegistroTomaRepositoryPort.class);
        alcancePaciente = mock(PacienteAlcancePort.class);
        auditoria = mock(AuditoriaPort.class);
        Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        caso = new RegistrarTomaUseCase(
                esquemas, tomas, new VerificadorAlcanceResidente(alcancePaciente, auditoria), auditoria, reloj);

        when(alcancePaciente.visible(PACIENTE, AUTOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(true);
        when(alcancePaciente.estaActivo(PACIENTE)).thenReturn(true);
        when(esquemas.buscarPorId(ESQUEMA)).thenReturn(Optional.of(esquemaConHorario(LocalTime.of(8, 0))));
    }

    private EsquemaMedicacion esquemaConHorario(LocalTime hora) {
        return new EsquemaMedicacion(
                ESQUEMA, PACIENTE, 100L, "1 tableta", 24, ViaAdministracion.ORAL,
                null, LocalDate.of(2026, 1, 1), null, true, List.of(HorarioMedicacion.de(hora)));
    }

    private AlcanceMedicacion autor() {
        return AlcanceMedicacion.de(AUTOR, RolUsuario.CUIDADOR_ENFERMERO);
    }

    @Test
    void registraTomaCuandoLaHoraEsUnHorarioProgramado() {
        when(tomas.existeParaEsquemaYProgramada(any(), any())).thenReturn(false);
        when(tomas.guardar(any())).thenReturn(new RegistroToma(
                1L, ESQUEMA, AUTOR, Instant.parse("2026-01-15T08:00:00Z"), AHORA,
                EstadoToma.ADMINISTRADA, null));

        RegistroToma registro = caso.ejecutar(
                PACIENTE, ESQUEMA,
                new ComandoRegistrarToma(Instant.parse("2026-01-15T08:00:00Z"), EstadoToma.ADMINISTRADA, null),
                autor());

        assertThat(registro.getId()).isEqualTo(1L);
    }

    @Test
    void rechazaHoraQueNoEsUnHorarioProgramado() {
        // El esquema tiene 08:00; se intenta registrar 09:30.
        assertThatThrownBy(() -> caso.ejecutar(
                PACIENTE, ESQUEMA,
                new ComandoRegistrarToma(Instant.parse("2026-01-15T09:30:00Z"), EstadoToma.ADMINISTRADA, null),
                autor()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void rechazaHoraFutura() {
        // 08:00 es horario valido, pero usamos una hora futura que tambien sea horario.
        when(esquemas.buscarPorId(ESQUEMA)).thenReturn(Optional.of(esquemaConHorario(LocalTime.of(20, 0))));
        assertThatThrownBy(() -> caso.ejecutar(
                PACIENTE, ESQUEMA,
                new ComandoRegistrarToma(Instant.parse("2026-01-15T20:00:00Z"), EstadoToma.ADMINISTRADA, null),
                autor()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void rechazaTomaDuplicadaParaLaMismaHoraProgramada() {
        when(tomas.existeParaEsquemaYProgramada(ESQUEMA, Instant.parse("2026-01-15T08:00:00Z"))).thenReturn(true);
        assertThatThrownBy(() -> caso.ejecutar(
                PACIENTE, ESQUEMA,
                new ComandoRegistrarToma(Instant.parse("2026-01-15T08:00:00Z"), EstadoToma.ADMINISTRADA, null),
                autor()))
                .isInstanceOf(ConflictoDeNegocioException.class);
    }
}