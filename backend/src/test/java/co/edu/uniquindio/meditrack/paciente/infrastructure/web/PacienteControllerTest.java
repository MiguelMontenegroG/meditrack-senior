package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.paciente.application.ActualizarPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.CambiarEstadoPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.CrearPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ListarPacientesUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ObtenerPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.Sexo;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
 * Pruebas web de PacienteController: autorizacion por rol (solo ADMINISTRADOR
 * escribe), validacion y manejo de 404 segun alcance.
 */
@WebMvcTest(PacienteController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class,
        HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class,
        ErrorResponseWriter.class, GlobalExceptionHandler.class, PacienteTestConfig.class})
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearPacienteUseCase crear;

    @MockitoBean
    private ListarPacientesUseCase listar;

    @MockitoBean
    private ObtenerPacienteUseCase obtener;

    @MockitoBean
    private ActualizarPacienteUseCase actualizar;

    @MockitoBean
    private CambiarEstadoPacienteUseCase cambiarEstado;

    @MockitoBean
    private ContactoEmergenciaRepositoryPort contactos;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private Paciente paciente() {
        return new Paciente(10L, "1001", "Ana", "Gomez", LocalDate.of(1940, 1, 1), Sexo.FEMENINO,
                "101", "Hipertension", LocalDate.of(2020, 1, 1), true, null, null);
    }

    @Test
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void listarComoSAdministradorDevuelve200() throws Exception {
        when(listar.ejecutar(any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of(paciente()));
        when(listar.contar(any(), any(), any())).thenReturn(1L);

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(10))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void crearComoCuidadorDevuelve403() throws Exception {
        String cuerpo = "{\"documento\":\"1002\",\"nombres\":\"Luis\",\"apellidos\":\"Diaz\","
                + "\"fechaNacimiento\":\"1945-02-01\",\"sexo\":\"MASCULINO\"}";

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void crearConFechaFuturaDevuelve400() throws Exception {
        String cuerpo = "{\"documento\":\"1002\",\"nombres\":\"Luis\",\"apellidos\":\"Diaz\","
                + "\"fechaNacimiento\":\"2999-02-01\",\"sexo\":\"MASCULINO\"}";

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    @WithMockUser(username = "7", roles = "FAMILIAR_AUTORIZADO")
    void detalleFueraDeAlcanceDevuelve404() throws Exception {
        when(obtener.ejecutar(anyLong(), any())).thenThrow(new RecursoNoEncontradoException("Paciente no encontrado"));

        mockMvc.perform(get("/api/pacientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }
}
