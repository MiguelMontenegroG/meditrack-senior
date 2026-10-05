package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import co.edu.uniquindio.meditrack.config.CorsConfig;
import co.edu.uniquindio.meditrack.config.HandlersSeguridad;
import co.edu.uniquindio.meditrack.config.JwtAuthenticationFilter;
import co.edu.uniquindio.meditrack.config.SecurityConfig;
import co.edu.uniquindio.meditrack.config.UsuarioDetailsService;
import co.edu.uniquindio.meditrack.usuario.application.ActualizarUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CambiarEstadoUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CrearUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ListarUsuariosUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ObtenerUsuarioUseCase;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de autorizacion de UsuarioController.
 *
 * Verifican que solo el rol ADMINISTRADOR puede acceder a la administracion de
 * usuarios: un cuidador autenticado recibe 403; sin autenticacion, 401; el
 * administrador recibe 200. Se importa la configuracion de seguridad real.
 */
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "CORS_ALLOWED_ORIGINS=http://localhost:5173")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationFilter.class, HandlersSeguridad.AutenticacionEntryPoint.class, HandlersSeguridad.AccesoDenegadoHandler.class, ErrorResponseWriter.class, GlobalExceptionHandler.class})
class UsuarioControllerAutorizacionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearUsuarioUseCase crear;

    @MockitoBean
    private ListarUsuariosUseCase listar;

    @MockitoBean
    private ObtenerUsuarioUseCase obtener;

    @MockitoBean
    private ActualizarUsuarioUseCase actualizar;

    @MockitoBean
    private CambiarEstadoUsuarioUseCase cambiarEstado;

    @MockitoBean
    private JwtPort jwtPort;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    private Usuario usuarioAdmin() {
        return new Usuario(1L, Correo.de("admin@meditrack.co"), "Admin", null,
                "$2a$hash", new Rol(1L, RolNombre.ADMINISTRADOR, null), true, null, null, null);
    }

    @Test
    void listarSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUIDADOR_ENFERMERO")
    void listarConCuidadorDevuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarConAdministradorDevuelve200() throws Exception {
        when(listar.ejecutar(any(), any(), anyInt(), anyInt())).thenReturn(List.of(usuarioAdmin()));
        when(listar.contar(any(), any())).thenReturn(1L);

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerUsuarioDevuelve200() throws Exception {
        when(obtener.ejecutar(anyLong())).thenReturn(usuarioAdmin());

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("admin@meditrack.co"));
    }
}