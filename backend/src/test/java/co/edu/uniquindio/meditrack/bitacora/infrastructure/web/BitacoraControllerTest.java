package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import co.edu.uniquindio.meditrack.bitacora.application.ListarSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.ObtenerUltimoSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.PaginaSignosVitalesDominio;
import co.edu.uniquindio.meditrack.bitacora.application.RegistrarSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;
import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;
import co.edu.uniquindio.meditrack.shared.infrastructure.web.ErrorResponseWriter;
import co.edu.uniquindio.meditrack.shared.infrastructure.web.GlobalExceptionHandler;
import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas web de {@link BitacoraController}: autorizacion por rol, formato de
 * error y mapeo de la respuesta.
 */
@WebMvcTest(BitacoraController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class,
        HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class,
        ErrorResponseWriter.class, GlobalExceptionHandler.class, BitacoraTestConfig.class})
class BitacoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarSignosVitalesUseCase registrar;

    @MockitoBean
    private ListarSignosVitalesUseCase historial;

    @MockitoBean
    private ObtenerUltimoSignosVitalesUseCase ultimo;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private RegistroSignosVitales registro() {
        return new RegistroSignosVitales(500L, 10L, 7L, Instant.parse("2026-02-10T08:00:00Z"),
                120, 80, 95, new BigDecimal("36.7"), "Sin novedad");
    }

    @Test
    void registrarSinTokenDevuelve401() throws Exception {
        String cuerpo = "{\"temperaturaC\":36.7}";
        mockMvc.perform(post("/api/pacientes/10/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void cuidadorRegistraDevuelve201() throws Exception {
        when(registrar.ejecutar(anyLong(), any(), any())).thenReturn(registro());
        String cuerpo = "{\"presionSistolica\":120,\"presionDiastolica\":80,"
                + "\"glucosaMgDl\":95,\"temperaturaC\":36.7}";

        mockMvc.perform(post("/api/pacientes/10/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.registradoPor").value(7))
                .andExpect(jsonPath("$.temperaturaC").value(36.7));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void adminRegistraDevuelve201() throws Exception {
        when(registrar.ejecutar(anyLong(), any(), any())).thenReturn(registro());
        String cuerpo = "{\"temperaturaC\":36.7}";

        mockMvc.perform(post("/api/pacientes/10/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "40", roles = "FAMILIAR_AUTORIZADO")
    void familiarNoRegistraDevuelve403() throws Exception {
        String cuerpo = "{\"temperaturaC\":36.7}";

        mockMvc.perform(post("/api/pacientes/10/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void registrarFueraDeAlcanceDevuelve404() throws Exception {
        when(registrar.ejecutar(anyLong(), any(), any()))
                .thenThrow(new RecursoNoEncontradoException("Residente no encontrado"));
        String cuerpo = "{\"temperaturaC\":36.7}";

        mockMvc.perform(post("/api/pacientes/99/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void registrarConValorFueraDeRangoDevuelve400() throws Exception {
        when(registrar.ejecutar(anyLong(), any(), any()))
                .thenThrow(new ValidacionDeNegocioException(
                        "La presion sistolica debe estar entre 50 y 300 mmHg"));
        String cuerpo = "{\"presionSistolica\":40}";

        mockMvc.perform(post("/api/pacientes/10/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void registrarEnResidenteInactivoDevuelve409() throws Exception {
        when(registrar.ejecutar(anyLong(), any(), any()))
                .thenThrow(new ConflictoDeNegocioException("El residente esta inactivo"));
        String cuerpo = "{\"temperaturaC\":36.7}";

        mockMvc.perform(post("/api/pacientes/10/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void historialDevuelvePagina() throws Exception {
        when(historial.ejecutar(anyLong(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PaginaSignosVitalesDominio(List.of(registro()), 1L));

        mockMvc.perform(get("/api/pacientes/10/signos-vitales")
                        .param("desde", "2026-02-01T00:00:00Z")
                        .param("hasta", "2026-02-28T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(500))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    @WithMockUser(username = "40", roles = "FAMILIAR_AUTORIZADO")
    void historialFueraDeAlcanceDevuelve404() throws Exception {
        when(historial.ejecutar(anyLong(), any(), any(), anyInt(), anyInt(), any()))
                .thenThrow(new RecursoNoEncontradoException("Residente no encontrado"));

        mockMvc.perform(get("/api/pacientes/99/signos-vitales"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "40", roles = "FAMILIAR_AUTORIZADO")
    void ultimoDevuelveRegistro() throws Exception {
        when(ultimo.ejecutar(anyLong(), any())).thenReturn(registro());

        mockMvc.perform(get("/api/pacientes/10/signos-vitales/ultimo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.glucosaMgDl").value(95));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void ultimoSinRegistrosDevuelve404() throws Exception {
        when(ultimo.ejecutar(anyLong(), any()))
                .thenThrow(new RecursoNoEncontradoException("El residente no tiene registros de signos vitales"));

        mockMvc.perform(get("/api/pacientes/10/signos-vitales/ultimo"))
                .andExpect(status().isNotFound());
    }
}