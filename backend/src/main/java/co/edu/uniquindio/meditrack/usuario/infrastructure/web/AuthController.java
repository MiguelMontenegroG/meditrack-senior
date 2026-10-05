package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import co.edu.uniquindio.meditrack.usuario.application.AutenticarUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CambiarContrasenaUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ConsultarUsuarioActualUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ResultadoLogin;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticacion bajo /api/auth.
 *
 * <p>POST /api/auth/login es publico. GET /me y PUT /contrasena exigen JWT.
 * El identificador del usuario autenticado es el nombre del {@link Authentication}
 * (el sub del token).</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion y datos del usuario autenticado")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticar;
    private final ConsultarUsuarioActualUseCase consultarActual;
    private final CambiarContrasenaUseCase cambiarContrasena;

    public AuthController(
            AutenticarUsuarioUseCase autenticar,
            ConsultarUsuarioActualUseCase consultarActual,
            CambiarContrasenaUseCase cambiarContrasena) {
        this.autenticar = autenticar;
        this.consultarActual = consultarActual;
        this.cambiarContrasena = cambiarContrasena;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion y obtener un token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest peticion) {
        ResultadoLogin resultado = autenticar.ejecutar(peticion.correo(), peticion.contrasena());
        LoginResponse respuesta = new LoginResponse(
                resultado.token(),
                "Bearer",
                resultado.expiraEnSegundos(),
                UsuarioWebMapper.aAutenticado(resultado.usuario()));
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado")
    public ResponseEntity<UsuarioAutenticadoResponse> me(Authentication autenticacion) {
        Usuario usuario = consultarActual.ejecutar(usuarioId(autenticacion));
        return ResponseEntity.ok(UsuarioWebMapper.aAutenticado(usuario));
    }

    @PutMapping("/contrasena")
    @Operation(summary = "Cambiar la contrasena del usuario autenticado")
    public ResponseEntity<Void> cambiarContrasena(
            Authentication autenticacion,
            @Valid @RequestBody CambiarContrasenaRequest peticion) {
        cambiarContrasena.ejecutar(
                usuarioId(autenticacion),
                peticion.contrasenaActual(),
                peticion.contrasenaNueva());
        return ResponseEntity.noContent().build();
    }

    private Long usuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }
}
