package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del cambio de estado del residente: la desactivacion es logica, exige
 * motivo y registra en la auditoria el motivo recortado; la activacion exige la
 * situacion clinica (la habitacion es opcional) y no usa motivo. Un paciente
 * inexistente no se modifica ni se audita.
 */
class CambiarEstadoPacienteUseCaseTest {

    private static final Long PACIENTE_ID = 10L;
    private static final Long AUTOR_ID = 1L;

    private PacienteRepositoryPort pacientes;
    private AuditoriaPort auditoria;
    private CambiarEstadoPacienteUseCase caso;

    @BeforeEach
    void configurar() {
        pacientes = mock(PacienteRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
        caso = new CambiarEstadoPacienteUseCase(pacientes, auditoria);
        when(pacientes.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Paciente paciente(boolean activo, String habitacion, String situacionClinica) {
        return new Paciente(PACIENTE_ID, "1001", "Ana", "Gomez", LocalDate.of(1940, 1, 1), Sexo.FEMENINO,
                habitacion, situacionClinica, LocalDate.of(2020, 1, 1), activo, null, null);
    }

    @Test
    void desactivarConMotivoHaceBorradoLogicoYAuditaElMotivoRecortado() {
        when(pacientes.buscarPorId(PACIENTE_ID))
                .thenReturn(Optional.of(paciente(true, "101", "Hipertension")));

        Paciente actualizado = caso.ejecutar(PACIENTE_ID, false, "  Traslado a otro centro  ", AUTOR_ID);

        assertThat(actualizado.isActivo()).isFalse();
        verify(pacientes).guardar(actualizado);
        verify(auditoria).registrar(
                eq(AUTOR_ID),
                eq(AccionAuditoria.UPDATE),
                eq("paciente"),
                eq(PACIENTE_ID),
                eq(ResultadoAuditoria.EXITOSO),
                eq("Desactivacion de paciente. Motivo: Traslado a otro centro"));
    }

    @Test
    void desactivarSinMotivoFallaYNoPersisteNiAudita() {
        when(pacientes.buscarPorId(PACIENTE_ID))
                .thenReturn(Optional.of(paciente(true, "101", "Hipertension")));

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE_ID, false, "   ", AUTOR_ID))
                .isInstanceOf(ValidacionDeNegocioException.class)
                .hasMessageContaining("motivo");

        verify(pacientes, never()).guardar(any());
        verify(auditoria, never()).registrar(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void activarConPerfilClinicoCompletoNoExigeMotivo() {
        when(pacientes.buscarPorId(PACIENTE_ID))
                .thenReturn(Optional.of(paciente(false, "101", "Hipertension")));

        Paciente actualizado = caso.ejecutar(PACIENTE_ID, true, null, AUTOR_ID);

        assertThat(actualizado.isActivo()).isTrue();
        verify(auditoria).registrar(
                eq(AUTOR_ID),
                eq(AccionAuditoria.UPDATE),
                eq("paciente"),
                eq(PACIENTE_ID),
                eq(ResultadoAuditoria.EXITOSO),
                eq("Activacion de paciente"));
    }

    @Test
    void activarSinSituacionClinicaFallaYNoModificaElEstado() {
        Paciente sinDatos = paciente(false, "101", null);
        when(pacientes.buscarPorId(PACIENTE_ID)).thenReturn(Optional.of(sinDatos));

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE_ID, true, "motivo ignorado", AUTOR_ID))
                .isInstanceOf(ValidacionDeNegocioException.class)
                .hasMessageContaining("residente activo");

        assertThat(sinDatos.isActivo()).isFalse();
        verify(pacientes, never()).guardar(any());
    }

    @Test
    void activarSinHabitacionEsValidoPorqueEsOpcional() {
        Paciente sinHabitacion = paciente(false, null, "Hipertension");
        when(pacientes.buscarPorId(PACIENTE_ID)).thenReturn(Optional.of(sinHabitacion));

        Paciente actualizado = caso.ejecutar(PACIENTE_ID, true, null, AUTOR_ID);

        assertThat(actualizado.isActivo()).isTrue();
    }

    @Test
    void pacienteInexistenteLanza404YNoAudita() {
        when(pacientes.buscarPorId(PACIENTE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE_ID, false, "Alta por recuperacion", AUTOR_ID))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no encontrado");

        verify(pacientes, never()).guardar(any());
        verify(auditoria, never()).registrar(anyLong(), any(), any(), any(), any(), any());
    }
}