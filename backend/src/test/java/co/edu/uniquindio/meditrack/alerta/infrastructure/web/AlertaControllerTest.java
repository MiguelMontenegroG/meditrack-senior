package co.edu.uniquindio.meditrack.alerta.infrastructure.web;

import co.edu.uniquindio.meditrack.alerta.application.AtenderAlertaUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ListarAlertasUseCase;
import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.OrigenAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio;
import co.edu.uniquindio.meditrack.alerta.domain.SeveridadAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.infrastructure.web.ErrorResponseWriter;
import co.edu.uniquindio.meditrack.shared.infrastructure.web.GlobalExceptionHandler;
import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas web de {@link AlertaController}: autorizacion por rol, formato de error
 * y mapeo de la respuesta.
 */
@WebMvcTest(AlertaController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "CORS_ALLOWED_ORIGINS=http://localhost:5173",
        "meditrack.alertas.habilitado=false"
})
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class,
        HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class,
        ErrorResponseWriter.class, GlobalExceptionHandler.class, AlertaTestConfig.class})
class AlertaControllerTest {

    private static final Instant CREADA = Instant.parse("2026-03-10T14:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListarAlertasUseCase listar;

    @MockitoBean
    private AtenderAlertaUseCase atender;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private Alerta activa() {
        return new Alerta(77L, 10L, TipoAlerta.DOSIS_ATRASADA, SeveridadAlerta.MEDIA,
                "Toma de medicacion programada sin registro", OrigenAlerta.SISTEMA,
                null, 501L, null, EstadoAlerta.ACTIVA, CREADA, null, null);
    }

    @Test
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void listarDevuelvePaginaDeAlertas() throws Exception {
        when(listar.ejecutar(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PaginaAlertasDominio(List.of(activa()), 1L));

        mockMvc.perform(get("/api/alertas").param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(77))
                .andExpect(jsonPath("$.contenido[0].tipo").value("DOSIS_ATRASADA"))
                .andExpect(jsonPath("$.contenido[0].severidad").value("MEDIA"))
                .andExpect(jsonPath("$.contenido[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$.contenido[0].tomaId").value(501))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.tamano").value(20));
    }

    @Test
    @WithMockUser(username = "7", roles = "FAMILIAR_AUTORIZADO")
    void listarFiltradoPorResidenteDevuelvePaginaVaciaSiNoEsVisible() throws Exception {
        when(listar.ejecutar(any(), any(), anyLong(), anyInt(), anyInt(), any()))
                .thenReturn(new PaginaAlertasDominio(List.of(), 0L));

        mockMvc.perform(get("/api/alertas").param("pacienteId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void atenderPorAdministradorDevuelve200() throws Exception {
        when(atender.ejecutar(anyLong(), any())).thenReturn(activa());

        mockMvc.perform(patch("/api/alertas/77/atender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77));
    }

    @Test
    @WithMockUser(username = "7", roles = "FAMILIAR_AUTORIZADO")
    void atenderComoFamiliarDevuelve403() throws Exception {
        mockMvc.perform(patch("/api/alertas/77/atender"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @WithMockUser(username = "9", roles = "CUIDADOR_ENFERMERO")
    void atenderAlertaFueraDeAlcanceDevuelve404() throws Exception {
        when(atender.ejecutar(anyLong(), any()))
                .thenThrow(new RecursoNoEncontradoException("Residente no encontrado"));

        mockMvc.perform(patch("/api/alertas/77/atender"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void atenderAlertaYaAtendidaDevuelve409() throws Exception {
        when(atender.ejecutar(anyLong(), any()))
                .thenThrow(new ConflictoDeNegocioException("La alerta ya fue atendida"));

        mockMvc.perform(patch("/api/alertas/77/atender"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void atenderPorAdministradorPropagaAccesoDenegadoDeNegocio() throws Exception {
        when(atender.ejecutar(anyLong(), any()))
                .thenThrow(new AccesoDenegadoException("Solo el administrador o el cuidador pueden atender alertas"));

        mockMvc.perform(patch("/api/alertas/77/atender"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }
}
