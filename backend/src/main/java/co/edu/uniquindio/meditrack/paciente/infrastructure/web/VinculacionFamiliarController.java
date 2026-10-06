package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.application.GestionarFamiliarUseCase;
import co.edu.uniquindio.meditrack.paciente.domain.VinculacionFamiliar;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de vinculacion de familiares bajo /api/pacientes/{id}/familiares
 * (solo ADMINISTRADOR).
 */
@RestController
@RequestMapping("/api/pacientes/{pacienteId}/familiares")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Familiares", description = "Vinculacion de familiares con pacientes (solo ADMINISTRADOR)")
public class VinculacionFamiliarController {

    private final GestionarFamiliarUseCase gestionar;

    public VinculacionFamiliarController(GestionarFamiliarUseCase gestionar) {
        this.gestionar = gestionar;
    }

    @GetMapping
    @Operation(summary = "Listar familiares vinculados a un paciente")
    public ResponseEntity<List<VinculacionFamiliarResponse>> listar(@PathVariable Long pacienteId) {
        List<VinculacionFamiliarResponse> respuesta = gestionar.listar(pacienteId).stream()
                .map(PacienteWebMapper::aVinculacion)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    @Operation(summary = "Vincular un familiar a un paciente")
    public ResponseEntity<VinculacionFamiliarResponse> vincular(
            @PathVariable Long pacienteId,
            @Valid @RequestBody FamiliarRequest peticion,
            Authentication autenticacion) {
        VinculacionFamiliar vinculacion = gestionar.vincular(
                pacienteId, peticion.usuarioId(), peticion.parentesco(), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteWebMapper.aVinculacion(vinculacion));
    }

    @DeleteMapping("/{usuarioId}")
    @Operation(summary = "Revocar la vinculacion de un familiar (autorizado = false)")
    public ResponseEntity<Void> revocar(
            @PathVariable Long pacienteId,
            @PathVariable Long usuarioId,
            Authentication autenticacion) {
        gestionar.revocar(pacienteId, usuarioId, AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.noContent().build();
    }
}
