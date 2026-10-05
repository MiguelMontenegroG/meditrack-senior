package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.shared.domain.CredencialesInvalidasException;
import co.edu.uniquindio.meditrack.usuario.application.AutenticarUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CambiarContrasenaUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ConsultarUsuarioActualUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ResultadoLogin;
import co.edu.uniquindio.meditrack.usuario.application.port.JwtPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.shared.infrastructure.web.ErrorResponseWriter;
import co.edu.uniquindio.meditrack.shared.infrastructure.web.GlobalExceptionHandler;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas web de AuthController con MockMvc.
 *
 * Se importa la configuracion de seguridad real; el filtro JWT es el real, pero
 * sus colaboradores (JwtPort y UsuarioDetailsService) se mockean para no depender
 * de tokens ni de base de datos.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class, HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class, ErrorResponseWriter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticarUsuarioUseCase autenticar;

    @MockitoBean
    private ConsultarUsuarioActualUseCase consultarActual;

    @MockitoBean
    private CambiarContrasenaUseCase cambiarContrasena;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private Usuario usuarioAdmin() {
        return new Usuario(1L, Correo.de("admin@meditrack.co"), "Admin", null,
                "$2a$hash", new Rol(1L, RolNombre.ADMINISTRADOR, null), true, null, null, null);
    }

    @Test
    void loginExitosoDevuelve200ConToken() throws Exception {
        when(autenticar.ejecutar(any(), any()))
                .thenReturn(new ResultadoLogin("jwt.token", 3600L, usuarioAdmin()));

        String cuerpo = "{\"correo\":\"admin@meditrack.co\",\"contrasena\":\"secreta123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"));
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        when(autenticar.ejecutar(any(), any())).thenThrow(new CredencialesInvalidasException());

        String cuerpo = "{\"correo\":\"admin@meditrack.co\",\"contrasena\":\"mala\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void loginConCuerpoInvalidoDevuelve400() throws Exception {
        String cuerpo = "{\"correo\":\"no-es-un-correo\",\"contrasena\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    void loginConJsonMalformadoDevuelve400() throws Exception {
        // JSON invalido: el parser de Jackson no puede deserializar el cuerpo.
        String cuerpo = "{\"correo\": \"a@b.com\", \"contrasena\": ,, }";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"));
    }

    @Test
    void meSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }
}