package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la regla de contacto principal: al marcar uno como principal, el
 * anterior deja de serlo en la misma operacion.
 */
class ContactoPrincipalTest {

    private PacienteRepositoryPort pacientes;
    private ContactoEmergenciaRepositoryPort contactos;
    private AuditoriaPort auditoria;

    @BeforeEach
    void configurar() {
        pacientes = mock(PacienteRepositoryPort.class);
        contactos = mock(ContactoEmergenciaRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
    }

    private Paciente paciente() {
        return new Paciente(10L, "1001", "Ana", "Gomez", LocalDate.of(1940, 1, 1), Sexo.FEMENINO,
                "101", null, LocalDate.of(2020, 1, 1), true, null, null);
    }

    @Test
    void marcarNuevoContactoComoPrincipalDegradaAlAnterior() {
        when(pacientes.buscarPorId(10L)).thenReturn(Optional.of(paciente()));
        ContactoEmergencia anterior = new ContactoEmergencia(1L, 10L, "Luis", "hijo", "300", true, null);
        when(contactos.buscarPrincipal(10L)).thenReturn(Optional.of(anterior));
        when(contactos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        GestionarContactoUseCase caso = new GestionarContactoUseCase(pacientes, contactos, auditoria);
        caso.crear(10L, new ComandoContacto("Marta", "hija", "301", true), 1L);

        verify(contactos, org.mockito.Mockito.times(2)).guardar(any());
        org.assertj.core.api.Assertions.assertThat(anterior.isPrincipal()).isFalse();
    }
}
