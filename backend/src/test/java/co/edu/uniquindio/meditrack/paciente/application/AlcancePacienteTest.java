package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.RolUsuario;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del alcance de lectura: fuera de alcance se responde 404 y se audita
 * DENEGADO; dentro del alcance, 200 y auditoria EXITOSO.
 */
class AlcancePacienteTest {

    private PacienteRepositoryPort pacientes;
    private AuditoriaPort auditoria;

    @BeforeEach
    void configurar() {
        pacientes = mock(PacienteRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
    }

    private Paciente paciente() {
        return new Paciente(10L, "1001", "Ana", "Gomez", LocalDate.of(1940, 1, 1), Sexo.FEMENINO,
                "101", null, LocalDate.of(2020, 1, 1), true, null, null);
    }

    @Test
    void leerPacienteFueraDeAlcanceLanza404YAuditaDenegado() {
        when(pacientes.buscarPorId(10L)).thenReturn(Optional.of(paciente()));
        when(pacientes.esVisiblePara(eq(10L), any(), anyLong())).thenReturn(false);
        ObtenerPacienteUseCase caso = new ObtenerPacienteUseCase(pacientes, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(10L, AlcancePaciente.de(7L, RolUsuario.FAMILIAR_AUTORIZADO)))
                .isInstanceOf(RecursoNoEncontradoException.class);

        ArgumentCaptor<ResultadoAuditoria> resultado = ArgumentCaptor.forClass(ResultadoAuditoria.class);
        verify(auditoria).registrar(any(), eq(AccionAuditoria.READ), eq("paciente"), eq(10L),
                resultado.capture(), any());
        assertThat(resultado.getValue()).isEqualTo(ResultadoAuditoria.DENEGADO);
    }

    @Test
    void leerPacienteInexistenteLanza404() {
        when(pacientes.buscarPorId(99L)).thenReturn(Optional.empty());
        ObtenerPacienteUseCase caso = new ObtenerPacienteUseCase(pacientes, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(99L, AlcancePaciente.de(1L, RolUsuario.ADMINISTRADOR)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void leerPacienteEnAlcanceDevuelveElPaciente() {
        when(pacientes.buscarPorId(10L)).thenReturn(Optional.of(paciente()));
        when(pacientes.esVisiblePara(eq(10L), any(), anyLong())).thenReturn(true);
        ObtenerPacienteUseCase caso = new ObtenerPacienteUseCase(pacientes, auditoria);

        Paciente obtenido = caso.ejecutar(10L, AlcancePaciente.de(7L, RolUsuario.CUIDADOR_ENFERMERO));

        assertThat(obtenido.getId()).isEqualTo(10L);
    }

    @Test
    void noAdministradorNoVePacientesInactivos() {
        ListarPacientesUseCase caso = new ListarPacientesUseCase(pacientes);
        caso.ejecutar(null, null, AlcancePaciente.de(7L, RolUsuario.CUIDADOR_ENFERMERO), 0, 20);

        verify(pacientes).listar(eq(null), eq(Boolean.TRUE), eq(RolUsuario.CUIDADOR_ENFERMERO), eq(7L), eq(0), eq(20));
    }
}
