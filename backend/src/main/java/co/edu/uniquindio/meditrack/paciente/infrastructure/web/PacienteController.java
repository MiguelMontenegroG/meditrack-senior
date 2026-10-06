package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.application.ActualizarPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.CambiarEstadoPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ComandoCrearPaciente;
import co.edu.uniquindio.meditrack.paciente.application.CrearPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ListarPacientesUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ObtenerPacienteUseCase;
import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.dto.PageResponse;
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

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de pacientes bajo /api/pacientes.
 *
 * <p>El listado y el detalle aplican el alcance del usuario (filtrado en el
 * backend). La escritura requiere ADMINISTRADOR mediante {@code @PreAuthorize}.</p>
 */
@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Perfiles de adultos mayores")
public class PacienteController {

    private final CrearPacienteUseCase crear;
    private final ListarPacientesUseCase listar;
    private final ObtenerPacienteUseCase obtener;
    private final ActualizarPacienteUseCase actualizar;
    private final CambiarEstadoPacienteUseCase cambiarEstado;
    private final ContactoEmergenciaRepositoryPort contactos;
    private final Clock reloj;

    public PacienteController(
            CrearPacienteUseCase crear,
            ListarPacientesUseCase listar,
            ObtenerPacienteUseCase obtener,
            ActualizarPacienteUseCase actualizar,
            CambiarEstadoPacienteUseCase cambiarEstado,
            ContactoEmergenciaRepositoryPort contactos,
            Clock reloj) {
        this.crear = crear;
        this.listar = listar;
        this.obtener = obtener;
        this.actualizar = actualizar;
        this.cambiarEstado = cambiarEstado;
        this.contactos = contactos;
        this.reloj = reloj;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un paciente (solo ADMINISTRADOR)")
    public ResponseEntity<PacienteDetalleResponse> crear(
            @Valid @RequestBody CrearPacienteRequest peticion,
            Authentication autenticacion) {
        Paciente creado = crear.ejecutar(aComando(peticion), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.status(HttpStatus.CREATED).body(aDetalle(creado));
    }

    @GetMapping
    @Operation(summary = "Listar pacientes segun el alcance del usuario (paginado)")
    public ResponseEntity<PageResponse<PacienteResumenResponse>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication autenticacion) {
        AlcancePaciente alcance = AlcanceWeb.de(autenticacion);
        LocalDate hoy = LocalDate.now(reloj);
        List<PacienteResumenResponse> contenido = listar.ejecutar(texto, activo, alcance, pagina, tamano).stream()
                .map(p -> PacienteWebMapper.aResumen(p, hoy))
                .toList();
        long total = listar.contar(texto, activo, alcance);
        return ResponseEntity.ok(PageResponse.de(contenido, pagina, tamano, total));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un paciente segun el alcance del usuario")
    public ResponseEntity<PacienteDetalleResponse> obtener(
            @PathVariable Long id,
            Authentication autenticacion) {
        AlcancePaciente alcance = AlcanceWeb.de(autenticacion);
        return ResponseEntity.ok(aDetalle(obtener.ejecutar(id, alcance)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar los datos de un paciente (solo ADMINISTRADOR)")
    public ResponseEntity<PacienteDetalleResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CrearPacienteRequest peticion,
            Authentication autenticacion) {
        Paciente actualizado = actualizar.ejecutar(id, aComando(peticion), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.ok(aDetalle(actualizado));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar un paciente (solo ADMINISTRADOR; nunca borrado fisico)")
    public ResponseEntity<PacienteDetalleResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoPacienteRequest peticion,
            Authentication autenticacion) {
        Paciente actualizado = cambiarEstado.ejecutar(id, peticion.activo(), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.ok(aDetalle(actualizado));
    }

    private PacienteDetalleResponse aDetalle(Paciente paciente) {
        List<ContactoEmergencia> lista = contactos.listarPorPaciente(paciente.getId());
        return PacienteWebMapper.aDetalle(paciente, lista, LocalDate.now(reloj));
    }

    private ComandoCrearPaciente aComando(CrearPacienteRequest peticion) {
        return new ComandoCrearPaciente(
                peticion.documento(),
                peticion.nombres(),
                peticion.apellidos(),
                peticion.fechaNacimiento(),
                peticion.sexo(),
                peticion.habitacion(),
                peticion.situacionClinica(),
                peticion.fechaIngreso());
    }
}
