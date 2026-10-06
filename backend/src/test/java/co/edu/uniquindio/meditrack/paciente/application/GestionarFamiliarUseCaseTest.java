package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.VinculacionFamiliarRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.paciente.domain.VinculacionFamiliar;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas de vinculacion de familiar: rol incorrecto, usuario
 * inactivo, duplicado y reactivacion.
 */
class GestionarFamiliarUseCaseTest {

    private PacienteRepositoryPort pacientes;
    private VinculacionFamiliarRepositoryPort vinculaciones;
    private UsuarioRepositoryPort usuarios;
    private AuditoriaPort auditoria;

    @BeforeEach
    void configurar() {
        pacientes = mock(PacienteRepositoryPort.class);
        vinculaciones = mock(VinculacionFamiliarRepositoryPort.class);
        usuarios = mock(UsuarioRepositoryPort.class);
        auditoria = mock(AuditoriaPort.class);
    }

    private Paciente pacienteActivo() {
        return new Paciente(10L, "1001", "Ana", "Gomez", LocalDate.of(1940, 1, 1), Sexo.FEMENINO,
                "101", null, LocalDate.of(2020, 1, 1), true, null, null);
    }

    private Usuario usuario(Long id, boolean activo, RolNombre rol) {
        return new Usuario(id, Correo.de("u" + id + "@meditrack.co"), "Usuario " + id, null,
                "$2a$hash", new Rol(1L, rol, null), activo, null, null, null);
    }

    private GestionarFamiliarUseCase caso() {
        when(pacientes.buscarPorId(10L)).thenReturn(Optional.of(pacienteActivo()));
        return new GestionarFamiliarUseCase(pacientes, vinculaciones, usuarios, auditoria);
    }

    @Test
    void usuarioConRolIncorrectoNoSeVincula() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, true, RolNombre.CUIDADOR_ENFERMERO)));

        assertThatThrownBy(() -> caso().vincular(10L, 2L, "hijo", 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("FAMILIAR_AUTORIZADO");
    }

    @Test
    void usuarioInactivoNoSeVincula() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, false, RolNombre.FAMILIAR_AUTORIZADO)));

        assertThatThrownBy(() -> caso().vincular(10L, 2L, "hijo", 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("no esta activo");
    }

    @Test
    void vincularDuplicadoAutorizadoFalla() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, true, RolNombre.FAMILIAR_AUTORIZADO)));
        VinculacionFamiliar existente = new VinculacionFamiliar(5L, 2L, 10L, "hijo", true, null);
        when(vinculaciones.buscarPorUsuarioYPaciente(2L, 10L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> caso().vincular(10L, 2L, "hijo", 1L))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("ya esta vinculado");
    }

    @Test
    void revincularReactivacionExistenteRevocada() {
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(usuario(2L, true, RolNombre.FAMILIAR_AUTORIZADO)));
        VinculacionFamiliar revocada = new VinculacionFamiliar(5L, 2L, 10L, "hijo", false, null);
        when(vinculaciones.buscarPorUsuarioYPaciente(2L, 10L)).thenReturn(Optional.of(revocada));
        when(vinculaciones.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        VinculacionFamiliar resultado = caso().vincular(10L, 2L, "hijo mayor", 1L);

        assertThat(resultado.isAutorizado()).isTrue();
        assertThat(resultado.getParentesco()).isEqualTo("hijo mayor");
    }

    @Test
    void usuarioInexistenteNoSeVincula() {
        when(usuarios.buscarPorId(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caso().vincular(10L, 99L, "hijo", 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
