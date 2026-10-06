package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.paciente.application.GestionarContactoUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ListarContactosUseCase;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas web de ContactoEmergenciaController: la lectura respeta el alcance
 * (cualquier rol autenticado) y la escritura exige ADMINISTRADOR.
 */
@WebMvcTest(ContactoEmergenciaController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class,
        HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class,
        ErrorResponseWriter.class, GlobalExceptionHandler.class})
class ContactoEmergenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListarContactosUseCase listar;

    @MockitoBean
    private GestionarContactoUseCase gestionar;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @WithMockUser(username = "7", roles = "FAMILIAR_AUTORIZADO")
    void familiarListaContactosDeSuPaciente() throws Exception {
        when(listar.ejecutar(anyLong(), any())).thenReturn(
                List.of(new ContactoEmergencia(1L, 10L, "Luis", "hijo", "300", true, null)));

        mockMvc.perform(get("/api/pacientes/10/contactos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Luis"))
                .andExpect(jsonPath("$[0].principal").value(true));
    }

    @Test
    @WithMockUser(username = "7", roles = "CUIDADOR_ENFERMERO")
    void cuidadorNoPuedeCrearContacto() throws Exception {
        String cuerpo = "{\"nombre\":\"Marta\",\"telefono\":\"301\",\"principal\":false}";

        mockMvc.perform(post("/api/pacientes/10/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMINISTRADOR")
    void administradorCreaContacto() throws Exception {
        when(gestionar.crear(anyLong(), any(), anyLong())).thenReturn(
                new ContactoEmergencia(2L, 10L, "Marta", "hija", "301", true, null));
        String cuerpo = "{\"nombre\":\"Marta\",\"parentesco\":\"hija\",\"telefono\":\"301\",\"principal\":true}";

        mockMvc.perform(post("/api/pacientes/10/contactos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }
}
