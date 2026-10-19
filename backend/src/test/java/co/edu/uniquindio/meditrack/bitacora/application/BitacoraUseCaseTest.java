package co.edu.uniquindio.meditrack.bitacora.application;

import co.edu.uniquindio.meditrack.bitacora.application.port.SignosVitalesRepositoryPort;
import co.edu.uniquindio.meditrack.bitacora.domain.AlcanceBitacora;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;
import co.edu.uniquindio.meditrack.bitacora.domain.RolBitacora;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la bitacora de signos vitales: rangos de plausibilidad,
 * coherencia de la presion, subconjunto de signos, fecha futura, residente
 * inactivo, alcance (404) y lectura (historial y ultimo registro).
 *
 * <p>El reloj se fija para que las reglas basadas en tiempo sean deterministas.</p>
 */
class BitacoraUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-02-10T12:00:00Z");
    private static final Instant PASADO = Instant.parse("2026-02-10T08:00:00Z");
    private static final Instant FUTURO = Instant.parse("2026-02-11T08:00:00Z");
    private static final Long PACIENTE = 10L;
    private static final Long ADMIN = 1L;
    private static final Long CUIDADOR = 7L;

    private SignosVitalesRepositoryPort signos;
    private PacienteAlcancePort alcancePaciente;
    private AuditoriaPort auditoria;
    private VerificadorAlcanceResidente verificador;
    private Clock reloj;

    @BeforeEach
    void configurar() {
        signos = mock(SignosVitalesRepositoryPort.class);
        alcancePaciente = mock(PacienteAlcancePort.class);
        auditoria = mock(AuditoriaPort.class);
        reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        verificador = new VerificadorAlcanceResidente(alcancePaciente, auditoria);

        when(alcancePaciente.visible(PACIENTE, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(true);
        when(alcancePaciente.visible(PACIENTE, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(true);
        when(alcancePaciente.estaActivo(PACIENTE)).thenReturn(true);
    }

    private AlcanceBitacora admin() {
        return AlcanceBitacora.de(ADMIN, RolBitacora.ADMINISTRADOR);
    }

    private AlcanceBitacora cuidador() {
        return AlcanceBitacora.de(CUIDADOR, RolBitacora.CUIDADOR_ENFERMERO);
    }

    private AlcanceBitacora familiar() {
        return AlcanceBitacora.de(40L, RolBitacora.FAMILIAR_AUTORIZADO);
    }

    private ComandoSignosVitales comandoCompleto(Instant fechaHora) {
        return new ComandoSignosVitales(fechaHora, 120, 80, 95, new BigDecimal("36.7"), "Sin novedad");
    }

    private ComandoSignosVitales soloTemperatura(Instant fechaHora, String valor) {
        return new ComandoSignosVitales(fechaHora, null, null, null, new BigDecimal(valor), null);
    }

    // ------------------------------------------------------------------
    // Registrar: exito y autor
    // ------------------------------------------------------------------

    @Test
    void registrarCreaRegistroConAutorDelUsuarioAutenticado() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        when(signos.guardar(any())).thenAnswer(inv -> {
            RegistroSignosVitales r = inv.getArgument(0);
            return new RegistroSignosVitales(500L, r.getPacienteId(), r.getRegistradoPor(),
                    r.getFechaHora(), r.getPresionSistolica(), r.getPresionDiastolica(),
                    r.getGlucosaMgDl(), r.getTemperaturaC(), r.getObservaciones());
        });

        RegistroSignosVitales creado = caso.ejecutar(PACIENTE, comandoCompleto(null), cuidador());

        assertThat(creado.getId()).isEqualTo(500L);
        assertThat(creado.getRegistradoPor()).isEqualTo(CUIDADOR);
        assertThat(creado.getPacienteId()).isEqualTo(PACIENTE);
        // Sin fecha del cliente se usa el reloj del servidor.
        assertThat(creado.getFechaHora()).isEqualTo(AHORA);
        verify(auditoria).registrar(eq(CUIDADOR), eq(AccionAuditoria.CREATE), eq("signos_vitales"),
                eq(500L), eq(ResultadoAuditoria.EXITOSO), any());
    }

    @Test
    void registrarAdminSobreCualquierResidente() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        when(signos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroSignosVitales creado = caso.ejecutar(PACIENTE, comandoCompleto(null), admin());

        assertThat(creado.getRegistradoPor()).isEqualTo(ADMIN);
    }

    @Test
    void registrarSubconjuntoSoloTemperaturaEsValido() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        when(signos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroSignosVitales creado = caso.ejecutar(PACIENTE, soloTemperatura(null, "37.2"), cuidador());

        assertThat(creado.getTemperaturaC()).isEqualByComparingTo("37.2");
        assertThat(creado.getPresionSistolica()).isNull();
        assertThat(creado.getGlucosaMgDl()).isNull();
    }

    @Test
    void registrarSinNingunSignoEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        ComandoSignosVitales vacio = new ComandoSignosVitales(null, null, null, null, null, "solo texto");

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, vacio, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void registrarConFechaFuturaEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, comandoCompleto(FUTURO), cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void registrarConFechaPasadaAceptaLaFechaDelCliente() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        when(signos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroSignosVitales creado = caso.ejecutar(PACIENTE, comandoCompleto(PASADO), cuidador());

        assertThat(creado.getFechaHora()).isEqualTo(PASADO);
    }

    @Test
    void registrarEnResidenteInactivoEs409() {
        when(alcancePaciente.estaActivo(PACIENTE)).thenReturn(false);
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, comandoCompleto(null), cuidador()))
                .isInstanceOf(ConflictoDeNegocioException.class);
    }

    @Test
    void registrarEnResidenteFueraDeAlcanceEs404() {
        Long ajeno = 99L;
        when(alcancePaciente.visible(ajeno, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(false);
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(ajeno, comandoCompleto(null), cuidador()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(auditoria).registrar(eq(CUIDADOR), eq(AccionAuditoria.READ), eq("paciente"),
                eq(ajeno), eq(ResultadoAuditoria.DENEGADO), any());
    }

    // ------------------------------------------------------------------
    // Rangos de plausibilidad y coherencia
    // ------------------------------------------------------------------

    @Test
    void sistolicaFueraDeRangoEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        ComandoSignosVitales bajo = new ComandoSignosVitales(null, 40, null, null, null, null);
        ComandoSignosVitales alto = new ComandoSignosVitales(null, 310, null, null, null, null);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, bajo, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, alto, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void diastolicaFueraDeRangoEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        ComandoSignosVitales bajo = new ComandoSignosVitales(null, null, 20, null, null, null);
        ComandoSignosVitales alto = new ComandoSignosVitales(null, null, 210, null, null, null);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, bajo, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, alto, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void diastolicaMayorOIgualQueSistolicaEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        ComandoSignosVitales igual = new ComandoSignosVitales(null, 120, 120, null, null, null);
        ComandoSignosVitales invertida = new ComandoSignosVitales(null, 80, 130, null, null, null);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, igual, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, invertida, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void glucosaFueraDeRangoEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        ComandoSignosVitales bajo = new ComandoSignosVitales(null, null, null, 10, null, null);
        ComandoSignosVitales alto = new ComandoSignosVitales(null, null, null, 900, null, null);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, bajo, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, alto, cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void temperaturaFueraDeRangoEs400() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, soloTemperatura(null, "29.9"), cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, soloTemperatura(null, "45.1"), cuidador()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void limitesExactosSonValidos() {
        var caso = new RegistrarSignosVitalesUseCase(signos, verificador, auditoria, reloj);
        when(signos.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        ComandoSignosVitales bordes = new ComandoSignosVitales(
                null, 50, 30, 20, new BigDecimal("30.0"), null);

        RegistroSignosVitales creado = caso.ejecutar(PACIENTE, bordes, cuidador());

        assertThat(creado.getPresionSistolica()).isEqualTo(50);
        assertThat(creado.getPresionDiastolica()).isEqualTo(30);
        assertThat(creado.getGlucosaMgDl()).isEqualTo(20);
        assertThat(creado.getTemperaturaC()).isEqualByComparingTo("30.0");
    }

    // ------------------------------------------------------------------
    // Historial
    // ------------------------------------------------------------------

    private RegistroSignosVitales registro(Long id, Instant fecha) {
        return new RegistroSignosVitales(id, PACIENTE, ADMIN, fecha, 120, 80, 95,
                new BigDecimal("36.7"), null);
    }

    @Test
    void historialSinFiltrosUsaListadoBasePaginado() {
        var caso = new ListarSignosVitalesUseCase(signos, verificador, auditoria);
        when(signos.listarPorPaciente(PACIENTE, 0, 20))
                .thenReturn(new PaginaSignosVitalesDominio(List.of(registro(1L, PASADO)), 1L));

        PaginaSignosVitalesDominio pagina = caso.ejecutar(PACIENTE, null, null, 0, 20, admin());

        assertThat(pagina.total()).isEqualTo(1L);
        verify(signos).listarPorPaciente(PACIENTE, 0, 20);
    }

    @Test
    void historialSoloDesdeUsaMetodoDesde() {
        var caso = new ListarSignosVitalesUseCase(signos, verificador, auditoria);
        when(signos.listarPorPacienteDesde(PACIENTE, PASADO, 0, 20))
                .thenReturn(new PaginaSignosVitalesDominio(List.of(), 0L));

        caso.ejecutar(PACIENTE, PASADO, null, 0, 20, admin());

        verify(signos).listarPorPacienteDesde(PACIENTE, PASADO, 0, 20);
    }

    @Test
    void historialSoloHastaUsaMetodoHasta() {
        var caso = new ListarSignosVitalesUseCase(signos, verificador, auditoria);
        when(signos.listarPorPacienteHasta(PACIENTE, AHORA, 0, 20))
                .thenReturn(new PaginaSignosVitalesDominio(List.of(), 0L));

        caso.ejecutar(PACIENTE, null, AHORA, 0, 20, admin());

        verify(signos).listarPorPacienteHasta(PACIENTE, AHORA, 0, 20);
    }

    @Test
    void historialConRangoUsaMetodoRango() {
        var caso = new ListarSignosVitalesUseCase(signos, verificador, auditoria);
        when(signos.listarPorPacienteYRango(PACIENTE, PASADO, AHORA, 0, 20))
                .thenReturn(new PaginaSignosVitalesDominio(List.of(registro(1L, PASADO)), 1L));

        PaginaSignosVitalesDominio pagina = caso.ejecutar(PACIENTE, PASADO, AHORA, 0, 20, admin());

        assertThat(pagina.contenido()).hasSize(1);
    }

    @Test
    void historialConRangoInvertidoEs400() {
        var caso = new ListarSignosVitalesUseCase(signos, verificador, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, AHORA, PASADO, 0, 20, admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void historialDeResidenteFueraDeAlcanceEs404() {
        Long ajeno = 88L;
        when(alcancePaciente.visible(ajeno, 40L, RolUsuario.FAMILIAR_AUTORIZADO)).thenReturn(false);
        var caso = new ListarSignosVitalesUseCase(signos, verificador, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(ajeno, null, null, 0, 20, familiar()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(signos, never()).listarPorPaciente(ajeno, 0, 20);
    }

    // ------------------------------------------------------------------
    // Ultimo registro
    // ------------------------------------------------------------------

    @Test
    void ultimoDevuelveElMasReciente() {
        var caso = new ObtenerUltimoSignosVitalesUseCase(signos, verificador, auditoria);
        when(signos.buscarUltimoPorPaciente(PACIENTE)).thenReturn(Optional.of(registro(9L, PASADO)));

        RegistroSignosVitales ultimo = caso.ejecutar(PACIENTE, admin());

        assertThat(ultimo.getId()).isEqualTo(9L);
    }

    @Test
    void ultimoSinRegistrosEs404() {
        var caso = new ObtenerUltimoSignosVitalesUseCase(signos, verificador, auditoria);
        when(signos.buscarUltimoPorPaciente(PACIENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caso.ejecutar(PACIENTE, admin()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void ultimoDeResidenteFueraDeAlcanceEs404() {
        Long ajeno = 77L;
        when(alcancePaciente.visible(ajeno, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(false);
        var caso = new ObtenerUltimoSignosVitalesUseCase(signos, verificador, auditoria);

        assertThatThrownBy(() -> caso.ejecutar(ajeno, cuidador()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}