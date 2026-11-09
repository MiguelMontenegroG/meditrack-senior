package co.edu.uniquindio.meditrack.reporte.application;

import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.reporte.application.port.DatosReportePort;
import co.edu.uniquindio.meditrack.reporte.application.port.GeneradorPdfPort;
import co.edu.uniquindio.meditrack.reporte.application.port.ReporteGeneradoRepositoryPort;
import co.edu.uniquindio.meditrack.reporte.domain.AlcanceReporte;
import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;
import co.edu.uniquindio.meditrack.reporte.domain.PaginaReportesDominio;
import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;
import co.edu.uniquindio.meditrack.reporte.domain.RolReporte;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte;
import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de los casos de uso del modulo de reportes: validacion del
 * rango (400/409), alcance del residente (404), generacion con PDF y registro de
 * metadatos, y el historial paginado.
 *
 * <p>El reloj se fija para que los metadatos (creadoEn) sean deterministas.</p>
 */
class ReporteUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-02-10T15:00:00Z");
    private static final Instant DESDE = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant HASTA = Instant.parse("2026-02-28T23:59:59Z");
    private static final Long PACIENTE = 10L;
    private static final Long ADMIN = 1L;
    private static final Long CUIDADOR = 7L;

    private ReporteGeneradoRepositoryPort reportes;
    private DatosReportePort datos;
    private GeneradorPdfPort generador;
    private UsuarioRepositoryPort usuarios;
    private PacienteAlcancePort alcancePaciente;
    private AuditoriaPort auditoria;
    private VerificadorAlcanceResidente verificador;
    private Clock reloj;

    @BeforeEach
    void configurar() {
        reportes = mock(ReporteGeneradoRepositoryPort.class);
        datos = mock(DatosReportePort.class);
        generador = mock(GeneradorPdfPort.class);
        usuarios = mock(UsuarioRepositoryPort.class);
        alcancePaciente = mock(PacienteAlcancePort.class);
        auditoria = mock(AuditoriaPort.class);
        reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        verificador = new VerificadorAlcanceResidente(alcancePaciente, auditoria);

        when(alcancePaciente.visible(PACIENTE, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(true);
        when(alcancePaciente.visible(PACIENTE, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(true);
        Usuario usuarioAdmin = usuarioConNombre(ADMIN, "Administrador");
        when(usuarios.buscarPorId(ADMIN)).thenReturn(Optional.of(usuarioAdmin));
    }

    private AlcanceReporte admin() {
        return AlcanceReporte.de(ADMIN, RolReporte.ADMINISTRADOR);
    }

    private AlcanceReporte cuidador() {
        return AlcanceReporte.de(CUIDADOR, RolReporte.CUIDADOR_ENFERMERO);
    }

    private static Usuario usuarioConNombre(Long id, String nombre) {
        Usuario u = mock(Usuario.class);
        when(u.getNombreCompleto()).thenReturn(nombre);
        return u;
    }

    private GenerarReporteUseCase caso() {
        return new GenerarReporteUseCase(verificador, datos, generador, reportes, usuarios, auditoria, reloj);
    }

    private static ContenidoReporte contenidoVacio(TipoReporte tipo) {
        SeccionReporte.Cabecera cabecera = new SeccionReporte.Cabecera(
                "Nombre Apellido", "DOC", 80, "MASCULINO", "101", "Estable",
                java.time.LocalDate.of(2020, 1, 1), true);
        return new ContenidoReporte(tipo, DESDE, HASTA, AHORA, cabecera,
                List.of(), List.of(), List.of(), List.of());
    }

    // ------------------------------------------------------------------
    // Generacion
    // ------------------------------------------------------------------

    @Test
    void generarDevuelvePdfYRegistraMetadatos() {
        when(datos.recolectar(eq(PACIENTE), eq(TipoReporte.HISTORIA_CLINICA), eq(DESDE), eq(HASTA),
                any(), any())).thenReturn(contenidoVacio(TipoReporte.HISTORIA_CLINICA));
        when(generador.generar(any())).thenReturn(new byte[] { 1, 2, 3 });
        when(reportes.guardar(any())).thenAnswer(inv -> {
            ReporteGenerado r = inv.getArgument(0);
            return new ReporteGenerado(100L, r.getPacienteId(), r.getGeneradoPor(), r.getTipo(),
                    r.getRangoDesde(), r.getRangoHasta(), r.getRutaAlmacenamiento(), r.getCreadoEn());
        });

        GenerarReporteUseCase.Resultado resultado = caso().generar(
                PACIENTE, new ComandoReporte(TipoReporte.HISTORIA_CLINICA, DESDE, HASTA), admin());

        assertThat(resultado.pdf()).containsExactly(1, 2, 3);
        assertThat(resultado.reporte().getId()).isEqualTo(100L);
        assertThat(resultado.reporte().getGeneradoPor()).isEqualTo(ADMIN);
        assertThat(resultado.reporte().getCreadoEn()).isEqualTo(AHORA);
        verify(auditoria).registrar(eq(ADMIN), eq(AccionAuditoria.CREATE), eq("reporte"),
                eq(100L), eq(ResultadoAuditoria.EXITOSO), any());
    }

    @Test
    void generarConRangoInvertidoEs400() {
        assertThatThrownBy(() -> caso().generar(
                PACIENTE, new ComandoReporte(TipoReporte.HISTORIA_CLINICA, HASTA, DESDE), admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
        verify(generador, never()).generar(any());
    }

    @Test
    void generarSinFechasEs400() {
        assertThatThrownBy(() -> caso().generar(
                PACIENTE, new ComandoReporte(TipoReporte.HISTORIA_CLINICA, null, HASTA), admin()))
                .isInstanceOf(ValidacionDeNegocioException.class);
    }

    @Test
    void generarConRangoDemasiadoAmplioEs409() {
        Instant haceDosAnios = HASTA.minus(Duration.ofDays(400));
        assertThatThrownBy(() -> caso().generar(
                PACIENTE, new ComandoReporte(TipoReporte.HISTORIA_CLINICA, haceDosAnios, HASTA), admin()))
                .isInstanceOf(ConflictoDeNegocioException.class);
        verify(generador, never()).generar(any());
    }

    @Test
    void generarResidenteFueraDeAlcanceEs404() {
        Long otro = 99L;
        when(alcancePaciente.visible(otro, ADMIN, RolUsuario.ADMINISTRADOR)).thenReturn(false);

        assertThatThrownBy(() -> caso().generar(
                otro, new ComandoReporte(TipoReporte.BITACORA, DESDE, HASTA), admin()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(datos, never()).recolectar(any(), any(), any(), any(), any(), any());
        verify(generador, never()).generar(any());
    }

    @Test
    void generarComoFamiliarAutorizadoVisibleEsPosible() {
        AlcanceReporte familiar = AlcanceReporte.de(40L, RolReporte.FAMILIAR_AUTORIZADO);
        when(alcancePaciente.visible(PACIENTE, 40L, RolUsuario.FAMILIAR_AUTORIZADO)).thenReturn(true);
        Usuario usuarioFamiliar = usuarioConNombre(40L, "Familiar");
        when(usuarios.buscarPorId(40L)).thenReturn(Optional.of(usuarioFamiliar));
        when(datos.recolectar(any(), any(), any(), any(), any(), any()))
                .thenReturn(contenidoVacio(TipoReporte.MEDICACION));
        when(generador.generar(any())).thenReturn(new byte[] { 9 });
        when(reportes.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        GenerarReporteUseCase.Resultado resultado = caso().generar(
                PACIENTE, new ComandoReporte(TipoReporte.MEDICACION, DESDE, HASTA), familiar);

        assertThat(resultado.reporte().getGeneradoPor()).isEqualTo(40L);
    }

    // ------------------------------------------------------------------
    // Historial
    // ------------------------------------------------------------------

    private ListarReportesUseCase casoHistorial() {
        return new ListarReportesUseCase(reportes, verificador, auditoria);
    }

    @Test
    void historialDevuelvePaginaYTotal() {
        ReporteGenerado unReporte = new ReporteGenerado(1L, PACIENTE, ADMIN, TipoReporte.GENERAL,
                DESDE, HASTA, "reportes/10/general", AHORA);
        when(reportes.listarPorPaciente(PACIENTE, 0, 20))
                .thenReturn(new PaginaReportesDominio(List.of(unReporte), 1L));

        PaginaReportesDominio pagina = casoHistorial().ejecutar(PACIENTE, 0, 20, admin());

        assertThat(pagina.contenido()).hasSize(1);
        assertThat(pagina.total()).isEqualTo(1L);
        verify(auditoria).registrar(eq(ADMIN), eq(AccionAuditoria.READ), eq("reporte"),
                eq(PACIENTE), eq(ResultadoAuditoria.EXITOSO), any());
    }

    @Test
    void historialDeResidenteFueraDeAlcanceEs404() {
        Long ajeno = 99L;
        when(alcancePaciente.visible(ajeno, CUIDADOR, RolUsuario.CUIDADOR_ENFERMERO)).thenReturn(false);

        assertThatThrownBy(() -> casoHistorial().ejecutar(ajeno, 0, 20, cuidador()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(reportes, never()).listarPorPaciente(any(), anyInt(), anyInt());
    }

    // ------------------------------------------------------------------
    // Tipo de reporte
    // ------------------------------------------------------------------

    @Test
    void tipoReporteDesconocidoEs400() {
        assertThatThrownBy(() -> TipoReporte.desde("NO_EXISTE"))
                .isInstanceOf(ValidacionDeNegocioException.class);
        assertThat(TipoReporte.desde("historia-clinica")).isEqualTo(TipoReporte.HISTORIA_CLINICA);
        assertThat(TipoReporte.desde("bitacora")).isEqualTo(TipoReporte.BITACORA);
    }
}