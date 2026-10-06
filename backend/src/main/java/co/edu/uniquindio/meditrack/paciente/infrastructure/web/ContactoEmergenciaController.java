package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.application.ComandoContacto;
import co.edu.uniquindio.meditrack.paciente.application.GestionarContactoUseCase;
import co.edu.uniquindio.meditrack.paciente.application.ListarContactosUseCase;
import co.edu.uniquindio.meditrack.paciente.domain.AlcancePaciente;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de contactos de emergencia de un paciente bajo
 * /api/pacientes/{id}/contactos.
 *
 * <p>La lectura respeta el alcance del usuario; la escritura requiere
 * ADMINISTRADOR.</p>
 */
@RestController
@RequestMapping("/api/pacientes/{pacienteId}/contactos")
@Tag(name = "Contactos de emergencia", description = "Contactos de emergencia de un paciente")
public class ContactoEmergenciaController {

    private final ListarContactosUseCase listar;
    private final GestionarContactoUseCase gestionar;

    public ContactoEmergenciaController(ListarContactosUseCase listar, GestionarContactoUseCase gestionar) {
        this.listar = listar;
        this.gestionar = gestionar;
    }

    @GetMapping
    @Operation(summary = "Listar contactos de emergencia (segun alcance)")
    public ResponseEntity<List<ContactoEmergenciaResponse>> listar(
            @PathVariable Long pacienteId,
            Authentication autenticacion) {
        AlcancePaciente alcance = AlcanceWeb.de(autenticacion);
        List<ContactoEmergenciaResponse> respuesta = listar.ejecutar(pacienteId, alcance).stream()
                .map(PacienteWebMapper::aContacto)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un contacto de emergencia (solo ADMINISTRADOR)")
    public ResponseEntity<ContactoEmergenciaResponse> crear(
            @PathVariable Long pacienteId,
            @Valid @RequestBody ContactoRequest peticion,
            Authentication autenticacion) {
        ContactoEmergencia creado = gestionar.crear(pacienteId, aComando(peticion), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteWebMapper.aContacto(creado));
    }

    @PutMapping("/{contactoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un contacto de emergencia (solo ADMINISTRADOR)")
    public ResponseEntity<ContactoEmergenciaResponse> actualizar(
            @PathVariable Long pacienteId,
            @PathVariable Long contactoId,
            @Valid @RequestBody ContactoRequest peticion,
            Authentication autenticacion) {
        ContactoEmergencia actualizado = gestionar.actualizar(
                pacienteId, contactoId, aComando(peticion), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.ok(PacienteWebMapper.aContacto(actualizado));
    }

    @DeleteMapping("/{contactoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar un contacto de emergencia (solo ADMINISTRADOR)")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long pacienteId,
            @PathVariable Long contactoId,
            Authentication autenticacion) {
        gestionar.eliminar(pacienteId, contactoId, AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.noContent().build();
    }

    private ComandoContacto aComando(ContactoRequest peticion) {
        return new ComandoContacto(peticion.nombre(), peticion.parentesco(), peticion.telefono(), peticion.principal());
    }
}
