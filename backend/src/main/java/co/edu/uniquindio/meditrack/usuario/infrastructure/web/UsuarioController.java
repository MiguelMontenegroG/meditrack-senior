package co.edu.uniquindio.meditrack.usuario.infrastructure.web;

import co.edu.uniquindio.meditrack.shared.dto.PageResponse;
import co.edu.uniquindio.meditrack.usuario.application.ActualizarUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.CambiarEstadoUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ComandoActualizarUsuario;
import co.edu.uniquindio.meditrack.usuario.application.ComandoCrearUsuario;
import co.edu.uniquindio.meditrack.usuario.application.CrearUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ListarUsuariosUseCase;
import co.edu.uniquindio.meditrack.usuario.application.ObtenerUsuarioUseCase;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de administracion de usuarios bajo /api/usuarios.
 *
 * <p>Todos exigen el rol ADMINISTRADOR (ademas de JWT valido). No existe borrado
 * fisico: el estado se cambia con activar/desactivar.</p>
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Usuarios", description = "Administracion de usuarios (solo ADMINISTRADOR)")
public class UsuarioController {

    private final CrearUsuarioUseCase crear;
    private final ListarUsuariosUseCase listar;
    private final ObtenerUsuarioUseCase obtener;
    private final ActualizarUsuarioUseCase actualizar;
    private final CambiarEstadoUsuarioUseCase cambiarEstado;

    public UsuarioController(
            CrearUsuarioUseCase crear,
            ListarUsuariosUseCase listar,
            ObtenerUsuarioUseCase obtener,
            ActualizarUsuarioUseCase actualizar,
            CambiarEstadoUsuarioUseCase cambiarEstado) {
        this.crear = crear;
        this.listar = listar;
        this.obtener = obtener;
        this.actualizar = actualizar;
        this.cambiarEstado = cambiarEstado;
    }

    @PostMapping
    @Operation(summary = "Crear un usuario")
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody CrearUsuarioRequest peticion,
            Authentication autenticacion) {
        ComandoCrearUsuario comando = new ComandoCrearUsuario(
                peticion.correo(),
                peticion.nombreCompleto(),
                peticion.telefono(),
                peticion.contrasena(),
                peticion.rol());
        Usuario creado = crear.ejecutar(comando, usuarioId(autenticacion));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioWebMapper.aResponse(creado));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios con filtros por rol y estado (paginado)")
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(
            @RequestParam(required = false) RolNombre rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        List<UsuarioResponse> contenido = listar.ejecutar(rol, activo, pagina, tamano).stream()
                .map(UsuarioWebMapper::aResponse)
                .toList();
        long total = listar.contar(rol, activo);
        return ResponseEntity.ok(PageResponse.de(contenido, pagina, tamano, total));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por id")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioWebMapper.aResponse(obtener.ejecutar(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nombre, telefono y rol de un usuario")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest peticion,
            Authentication autenticacion) {
        ComandoActualizarUsuario comando = new ComandoActualizarUsuario(
                peticion.nombreCompleto(),
                peticion.telefono(),
                peticion.rol());
        Usuario actualizado = actualizar.ejecutar(id, comando, usuarioId(autenticacion));
        return ResponseEntity.ok(UsuarioWebMapper.aResponse(actualizado));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un usuario (nunca borrado fisico)")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest peticion,
            Authentication autenticacion) {
        Usuario actualizado = cambiarEstado.ejecutar(id, peticion.activo(), usuarioId(autenticacion));
        return ResponseEntity.ok(UsuarioWebMapper.aResponse(actualizado));
    }

    private Long usuarioId(Authentication autenticacion) {
        return Long.valueOf(autenticacion.getName());
    }
}
