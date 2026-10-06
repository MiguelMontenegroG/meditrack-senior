package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.AsignacionCuidadorRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AsignacionCuidador;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas de asignacion de cuidador: rol incorrecto, asignacion
 * duplicada vigente y cierre de la asignacion.
 */
class GestionarCuidadorUseCaseTest {

    private static final LocalDate HOY = LocalDate.of(2026, 1, 15);

    private PacienteRepositoryPort pacientes;
    private AsignacionCuidadorRepositoryPort asignaciones;
    private UsuarioRepositoryPort usuarios;
    private AuditoriaPort auditoria;
    private Clock reloj;

    @BeforeEach
    void configurar() {
        pacientes = mock(PacienteRepositoryPort.class);
        asignaciones = mock(AsignacionCuidadorRepositoryPort.class);
        usuarios = mock(UsuarioRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
        reloj = Clock.fixed(Instant.parse("2026-01-15T12:00:00Z"), ZoneOffset.UTC);
    }

    private Paciente pacienteActivo() {
        return new Paciente(10L, "1001", "Ana", "Gomez", LocalDate.of(1940, 1, 1), Sexo.FEMENINO,
                "101", null, LocalDate.of(2020, 1, 1), true, null, null);
    }

    private Usuario usuario(Long id, RolNombre rol) {
        return new Usuario(id, Correo.de("u" + id + "@meditrack.co"), "Usuario " + id, null,
                "$2a$hash", new Rol(1L, rol, null), true, null, null, null);
    }

    private GestionarCuidadorUseCase caso() {
        when(pacientes.buscarPorId(10L)).thenReturn(Optional.of(pacienteActivo()));
        return new GestionarCuidadorUseCase(pacientes, asignaciones, usuarios, auditoria, reloj);
    }

    @Test
    void usuarioConRolIncorrectoNoSeAsigna() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, RolNombre.FAMILIAR_AUTORIZADO)));

        assertThatThrownBy(() -> caso().asignar(10L, 2L, 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("CUIDADOR_ENFERMERO");
    }

    @Test
    void asignacionDuplicadaVigenteFalla() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, RolNombre.CUIDADOR_ENFERMERO)));
        when(asignaciones.buscarVigente(2L, 10L))
                .thenReturn(Optional.of(new AsignacionCuidador(3L, 2L, 10L, HOY, null, null)));

        assertThatThrownBy(() -> caso().asignar(10L, 2L, 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("vigente");
    }

    @Test
    void asignarCuidadorValidoUsaLaFechaDeHoy() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, RolNombre.CUIDADOR_ENFERMERO)));
        when(asignaciones.buscarVigente(2L, 10L)).thenReturn(Optional.empty());
        when(asignaciones.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        AsignacionCuidador asignacion = caso().asignar(10L, 2L, 1L);

        assertThat(asignacion.getDesde()).isEqualTo(HOY);
        assertThat(asignacion.estaVigente()).isTrue();
    }

    @Test
    void desasignarCierraLaVigenciaConHoy() {
        AsignacionCuidador vigente = new AsignacionCuidador(3L, 2L, 10L, HOY.minusDays(10), null, null);
        when(asignaciones.buscarVigente(2L, 10L)).thenReturn(Optional.of(vigente));
        when(asignaciones.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        caso().desasignar(10L, 2L, 1L);

        assertThat(vigente.estaVigente()).isFalse();
        assertThat(vigente.getHasta()).isEqualTo(HOY);
    }
}
