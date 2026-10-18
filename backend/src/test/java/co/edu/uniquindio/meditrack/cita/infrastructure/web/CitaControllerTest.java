package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.application.AgendarCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.CambiarEstadoCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.EditarCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.ListarCalendarioCitasUseCase;
import co.edu.uniquindio.meditrack.cita.application.ObtenerCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.PaginaCitasDominio;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.shared.domain.AccesoDenegadoException;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas web de {@link CitaController}: autorizacion por rol, formato de error
 * y mapeo de la respuesta.
 */
@WebMvcTest(CitaController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class,
        HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class,
        ErrorResponseWriter.class, GlobalExceptionHandler.class, CitaTestConfig.class})
class CitaControllerTest {

    private static final Instant AHORA = Instant.parse("2026-01-15T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendarCitaUseCase agendar;

    @MockitoBean
    private EditarCitaUseCase editar;

    @MockitoBean
    private CambiarEstadoCitaUseCase cambiarEstado;

    @MockitoBean
    private ObtenerCitaUseCase obtener;

    @MockitoBean
    private ListarCalendarioCitasUseCase calendario;

    @MockitoBean
    private EspecialidadRepositoryPort especialidades;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private Cita cita(EstadoCita estado) {
        return new Cita(55L, 10L, 1L, 3L, "Dra. Perez", Instant.parse("2026-01-20T09:00:00Z"),
                "Consultorio 2", "Control", estado);
    }

    @Test
    void calendarioSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/citas")
                        .param("desde", "2026-01-01T00:00:00Z")
                        .param("hasta", "2026-01-31T23:59:59Z"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void calendarioDevuelvePaginaConCitas() throws Exception {
        when(calendario.ejecutar(any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PaginaCitasDominio(List.of(cita(EstadoCita.PROGRAMADA)), 1L));
        when(especialidades.nombrePorId(3L)).thenReturn(Optional.of("Cardiologia"));

        mockMvc.perform(get("/api/citas")
                        .param("desde", "2026-01-01T00:00:00Z")
                        .param("hasta", "2026-01-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(55))
                .andExpect(jsonPath("$.contenido[0].especialidadNombre").value("Cardiologia"))
                .andExpect(jsonPath("$.contenido[0].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamano").value(20))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void calendarioFiltradoPorResidenteDevuelvePaginaVaciaSiNoEsVisible() throws Exception {
        when(calendario.ejecutar(any(), any(), any(), anyLong(), anyInt(), anyInt(), any()))
                .thenReturn(new PaginaCitasDominio(List.of(), 0L));

        mockMvc.perform(get("/api/citas")
                        .param("desde", "2026-01-01T00:00:00Z")
                        .param("hasta", "2026-01-31T23:59:59Z")
                        .param("pacienteId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void agendarComoCuidadorDevuelve403() throws Exception {
        String cuerpo = "{\"especialidadId\":3,\"profesional\":\"Dra. Perez\","
                + "\"fechaHora\":\"2026-01-20T09:00:00Z\"}";

        mockMvc.perform(post("/api/pacientes/10/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void agendarConFechaPasadaDevuelve400() throws Exception {
        when(agendar.ejecutar(anyLong(), any(), any()))
                .thenThrow(new ValidacionDeNegocioException("La fecha y hora de la cita no puede estar en el pasado"));
        String cuerpo = "{\"especialidadId\":3,\"fechaHora\":\"2026-01-10T09:00:00Z\"}";

        mockMvc.perform(post("/api/pacientes/10/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void agendarCreaDevuelve201() throws Exception {
        when(agendar.ejecutar(anyLong(), any(), any())).thenReturn(cita(EstadoCita.PROGRAMADA));
        when(especialidades.nombrePorId(3L)).thenReturn(Optional.of("Cardiologia"));
        String cuerpo = "{\"especialidadId\":3,\"fechaHora\":\"2026-01-20T09:00:00Z\"}";

        mockMvc.perform(post("/api/pacientes/10/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void marcarFuturoComoCuidadorDevuelve400() throws Exception {
        when(cambiarEstado.ejecutar(anyLong(), anyLong(), any(), any()))
                .thenThrow(new ValidacionDeNegocioException("No se puede marcar una cita futura"));
        String cuerpo = "{\"estado\":\"CUMPLIDA\"}";

        mockMvc.perform(patch("/api/pacientes/10/citas/55/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void cancelarComoCuidadorDevuelve403() throws Exception {
        when(cambiarEstado.ejecutar(anyLong(), anyLong(), any(), any()))
                .thenThrow(new AccesoDenegadoException("Solo el administrador puede cancelar una cita"));
        String cuerpo = "{\"estado\":\"CANCELADA\"}";

        mockMvc.perform(patch("/api/pacientes/10/citas/55/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @WithMockUser(username = "7", roles = "FAMILIAR_AUTORIZADO")
    void cambiarEstadoComoFamiliarDevuelve403() throws Exception {
        String cuerpo = "{\"estado\":\"CUMPLIDA\"}";

        mockMvc.perform(patch("/api/pacientes/10/citas/55/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "7", roles = "FAMILIAR_AUTORIZADO")
    void detalleFueraDeAlcanceDevuelve404() throws Exception {
        when(obtener.ejecutar(anyLong(), anyLong(), any()))
                .thenThrow(new RecursoNoEncontradoException("Cita no encontrada"));

        mockMvc.perform(get("/api/pacientes/99/citas/55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void editarCitaNoInicialDevuelve409() throws Exception {
        when(editar.ejecutar(anyLong(), anyLong(), any(), any()))
                .thenThrow(new ConflictoDeNegocioException("No se puede editar una cita que ya no esta en estado inicial"));
        String cuerpo = "{\"especialidadId\":3,\"fechaHora\":\"2026-01-25T10:00:00Z\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/pacientes/10/citas/55")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
    }
}
