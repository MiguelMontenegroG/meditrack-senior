package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import co.edu.uniquindio.meditrack.cita.domain.RolCita;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de los casos de uso de citas: reglas 400/404/409/403,
 * transiciones de estado y calendario paginado (total coherente, filtro por
 * residente y alcance).
 *
 * <p>El reloj se fija para que las reglas basadas en tiempo sean
 * deterministas.</p>
 */
class CitasUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-01-15T12:00:00Z");
    private static final Instant FUTURO = Instant.parse("2026-01-20T09:00:00Z");
    private static final Instant PASADO = Instant.parse("2026-01-10T09:00:00Z");
    private static final Instant DESDE = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant HASTA = Instant.parse("2026-01-31T23:59:59Z");
    private static final Long PACIENTE = 10L;
    private static final Long ADMIN = 1L;
    private static final Long CUIDADOR = 7L;
    private static final Long ESPECIALIDAD = 3L;

    private CitaRepositoryPort citas;
    private EspecialidadRepositoryPort especialidades;
    private PacienteRepositoryPort pacientes;
    private PacienteAlcancePort alcancePaciente;
    private AuditoriaPort auditoria;
    private VerificadorAlcanceResidente verificador;
    private Clock reloj;

    @BeforeEach
    void configurar() {
        citas = mock(CitaRepositoryPort.class);
        especialidades = mock(EspecialidadRepositoryPort.class);
        pacientes = mock(PacienteRepositoryPort.class);
        alcancePaciente = mock(PacienteAlcancePort.class);
        auditoria = mock(AuditoriaPort.class);
        reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        verificador = new VerificadorAlcanceResidente(alcancePaciente, auditoria);

        when(alcancePaciente.visible(PACIENTE, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(true);
        when(alcancePaciente.visible(PACIENTE, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(true);
        when(alcancePaciente.estaActivo(PACIENTE)).thenReturn(true);
        when(especialidades.existe(ESPECIALIDAD)).thenReturn(true);
    }

    private AlcanceCita admin() {
        return AlcanceCita.de(ADMIN, RolCita.ADMINISTRADOR);
    }

    private AlcanceCita cuidador() {
        return AlcanceCita.de(CUIDADOR, RolCita.CUIDADOR_ENFERMERO);
    }

    private ComandoCita comando(Instant fechaHora) {
        return new ComandoCita(ESPECIALIDAD, "Dra. Perez", fechaHora, "Consultorio 2", "Control");
    }

    private Cita citaEnEstado(EstadoCita estado, Instant fechaHora) {
        return new Cita(55L, PACIENTE, ADMIN, ESPECIALIDAD, "Dra. Perez", fechaHora,
                "Consultorio 2", "Control", estado);
    }

    // ------------------------------------------------------------------
    // Agendar
    // ------------------------------------------------------------------

    @Test
    void agendarCreaCitaEnEstadoProgramada() {
        var caso = new AgendarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);
        when(citas.guardar(any())).thenAnswer(inv -> {
            Cita c = inv.getArgument(0);
            return new Cita(100L, c.getPacienteId(), c.getRegistradaPor(), c.getEspecialidadId(),
                    c.getProfesional(), c.getFechaHora(), c.getLugar(), c.getMotivo(), c.getEstado());
        });

        Cita creada = caso.ejecutar(PACIENTE, comando(FUTURO), admin());

        assertThat(creada.getId()).isEqualTo(100L);
        assertThat(creada.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(creada.getRegistradaPor()).isEqualTo(ADMIN);
        assertThat(creada.getPacienteId()).isEqualTo(PACIENTE);
    }

    @Test
    void agendarConFechaPasadaEs400() {
        var caso = new AgendarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, comando(PASADO), admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void agendarConEspecialidadInexistenteEs404() {
        when(especialidades.existe(999L)).thenReturn(false);
        var caso = new AgendarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);
        ComandoCita conEspecialidadInvalida = new ComandoCita(999L, "Dra. Perez", FUTURO, null, null);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, conEspecialidadInvalida, admin()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void agendarConResidenteInexistenteOFueraDeAlcanceEs404() {
        Long otro = 99L;
        when(alcancePaciente.visible(otro, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(false);
        var caso = new AgendarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(otro, comando(FUTURO), admin()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void agendarConResidenteInactivoEs409() {
        when(alcancePaciente.estaActivo(PACIENTE)).thenReturn(false);
        var caso = new AgendarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, comando(FUTURO), admin()))
                .isInstanceOf(ConflictoDeNegocioException.class);
    }

    // ------------------------------------------------------------------
    // Editar
    // ------------------------------------------------------------------

    @Test
    void editarCitaEnEstadoInicialActualizaDatos() {
        var caso = new EditarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);
        Cita existente = citaEnEstado(EstadoCita.PROGRAMADA, FUTURO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(existente));
        when(citas.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant nuevaFecha = Instant.parse("2026-01-25T10:00:00Z");
        Cita editada = caso.ejecutar(PACIENTE, 55L, comando(nuevaFecha), admin());

        assertThat(editada.getFechaHora()).isEqualTo(nuevaFecha);
    }

    @Test
    void editarCitaNoInicialEs409() {
        var caso = new EditarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);
        Cita cancelada = citaEnEstado(EstadoCita.CANCELADA, FUTURO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, 55L, comando(FUTURO), admin()))
                .isInstanceOf(ConflictoDeNegocioException.class);
    }

    @Test
    void editarCitaDeOtroResidenteEs404() {
        var caso = new EditarCitaUseCase(citas, especialidades, verificador, auditoria, reloj);
        Cita existente = citaEnEstado(EstadoCita.PROGRAMADA, FUTURO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(existente));

        // La cita pertenece a PACIENTE (10); se consulta con otro residente visible.
        Long otro = 11L;
        when(alcancePaciente.visible(otro, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(true);
        when(alcancePaciente.estaActivo(otro)).thenReturn(true);

        assertThatThrownBy(() -> caso.ejecutar(otro, 55L, comando(FUTURO), admin()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // Cambiar estado
    // ------------------------------------------------------------------

    @Test
    void marcarCumplidaDesdeProgramada() {
        var caso = new CambiarEstadoCitaUseCase(citas, verificador, auditoria, reloj);
        Cita programada = citaEnEstado(EstadoCita.PROGRAMADA, PASADO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(programada));
        when(citas.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita resultado = caso.ejecutar(PACIENTE, 55L, EstadoCita.CUMPLIDA, cuidador());

        assertThat(resultado.getEstado()).isEqualTo(EstadoCita.CUMPLIDA);
    }

    @Test
    void marcarCitaFuturaEs400() {
        var caso = new CambiarEstadoCitaUseCase(citas, verificador, auditoria, reloj);
        Cita programada = citaEnEstado(EstadoCita.PROGRAMADA, FUTURO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(programada));

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, 55L, EstadoCita.CUMPLIDA, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void cuidadorNoPuedeCancelarEs403() {
        var caso = new CambiarEstadoCitaUseCase(citas, verificador, auditoria, reloj);
        Cita programada = citaEnEstado(EstadoCita.PROGRAMADA, PASADO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(programada));

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, 55L, EstadoCita.CANCELADA, cuidador()))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void administradorCancelaDesdeProgramada() {
        var caso = new CambiarEstadoCitaUseCase(citas, verificador, auditoria, reloj);
        Cita programada = citaEnEstado(EstadoCita.PROGRAMADA, FUTURO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(programada));
        when(citas.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Cita resultado = caso.ejecutar(PACIENTE, 55L, EstadoCita.CANCELADA, admin());

        assertThat(resultado.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    void marcarCitaYaFinalEs409() {
        var caso = new CambiarEstadoCitaUseCase(citas, verificador, auditoria, reloj);
        Cita cumplida = citaEnEstado(EstadoCita.CUMPLIDA, PASADO);
        when(citas.buscarPorId(55L)).thenReturn(Optional.of(cumplida));

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, 55L, EstadoCita.INCUMPLIDA, admin()))
                .isInstanceOf(ConflictoDeNegocioException.class);
    }

    @Test
    void marcarEstadoProgramadaOReProgramadaEs400() {
        var caso = new CambiarEstadoCitaUseCase(citas, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, 55L, EstadoCita.PROGRAMADA, admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, 55L, EstadoCita.REPROGRAMADA, admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    // ------------------------------------------------------------------
    // Calendario paginado
    // ------------------------------------------------------------------

    private ListarCalendarioCitasUseCase casoCalendario() {
        return new ListarCalendarioCitasUseCase(citas, pacientes, alcancePaciente, auditoria);
    }

    @Test
    void calendarioConRangoInvertidoEs400() {
        assertThatThrownBy(() -> casoCalendario()
                .ejecutar(HASTA, DESDE, null, null, 0, 20, admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void calendarioFiltradoPorResidenteDevuelveSuPaginaYTotal() {
        Cita c1 = citaEnEstado(EstadoCita.PROGRAMADA, FUTURO);
        when(citas.listarPorPaciente(PACIENTE, null, DESDE, HASTA, 0, 20))
                .thenReturn(new PaginaCitasDominio(List.of(c1), 1L));

        PaginaCitasDominio pagina = casoCalendario()
                .ejecutar(DESDE, HASTA, null, PACIENTE, 0, 20, admin());

        assertThat(pagina.contenido()).hasSize(1);
        assertThat(pagina.total()).isEqualTo(1L);
    }

    @Test
    void calendarioPorResidenteFueraDeAlcanceDevuelvePaginaVaciaSinConsultar() {
        Long ajeno = 99L;
        when(alcancePaciente.visible(ajeno, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(false);

        PaginaCitasDominio pagina = casoCalendario()
                .ejecutar(DESDE, HASTA, null, ajeno, 0, 20, admin());

        // Lista vacia y total 0, sin revelar existencia del residente.
        assertThat(pagina.contenido()).isEmpty();
        assertThat(pagina.total()).isZero();
    }

    @Test
    void calendarioSinFiltroPorResidenteUsaConjuntoVisible() {
        when(pacientes.listarTodos(null, null, 0, 500))
                .thenReturn(List.of(pacienteConId(PACIENTE)));
        Cita c1 = citaEnEstado(EstadoCita.PROGRAMADA, FUTURO);
        when(citas.listarPorPacientes(eq(List.of(PACIENTE)), eq(null), eq(DESDE), eq(HASTA), anyInt(), anyInt()))
                .thenReturn(new PaginaCitasDominio(List.of(c1), 1L));

        PaginaCitasDominio pagina = casoCalendario()
                .ejecutar(DESDE, HASTA, null, null, 0, 20, admin());

        assertThat(pagina.total()).isEqualTo(1L);
    }

    @Test
    void calendarioSinResidentesVisiblesDevuelvePaginaVacia() {
        when(pacientes.listarTodos(null, null, 0, 500)).thenReturn(List.of());

        PaginaCitasDominio pagina = casoCalendario()
                .ejecutar(DESDE, HASTA, null, null, 0, 20, admin());

        assertThat(pagina.contenido()).isEmpty();
        assertThat(pagina.total()).isZero();
    }

    @Test
    void calendarioAlcanceDeCuidadorConAsignacionVencidaDevuelvePaginaVacia() {
        Long vencido = 21L;
        when(alcancePaciente.visible(vencido, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO))
                .thenReturn(false);

        PaginaCitasDominio pagina = casoCalendario()
                .ejecutar(DESDE, HASTA, null, vencido, 0, 20, cuidador());

        assertThat(pagina.contenido()).isEmpty();
        assertThat(pagina.total()).isZero();
    }

    @Test
    void calendarioAlcanceDeFamiliarSinVinculacionAutorizadaDevuelvePaginaVacia() {
        Long sinAcceso = 31L;
        AlcanceCita familiar = AlcanceCita.de(40L, RolCita.FAMILIAR_AUTORIZADO);
        when(alcancePaciente.visible(sinAcceso, 40L, RolUsuario.FAMILIAR_AUTORIZADO))
                .thenReturn(false);

        PaginaCitasDominio pagina = casoCalendario()
                .ejecutar(DESDE, HASTA, null, sinAcceso, 0, 20, familiar);

        assertThat(pagina.contenido()).isEmpty();
        assertThat(pagina.total()).isZero();
    }

    private Paciente pacienteConId(Long id) {
        return new Paciente(id, "DOC", "Nombre", "Apellido", java.time.LocalDate.of(1940, 1, 1),
                Sexo.MASCULINO, "101", "Estable", java.time.LocalDate.of(2020, 1, 1), true,
                null, null);
    }
}