package co.edu.uniquindio.meditrack.paciente.infrastructure.web;

import co.edu.uniquindio.meditrack.paciente.application.GestionarCuidadorUseCase;
import co.edu.uniquindio.meditrack.paciente.domain.AsignacionCuidador;
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
 * Endpoints de asignacion de cuidadores bajo /api/pacientes/{id}/cuidadores
 * (solo ADMINISTRADOR).
 */
@RestController
@RequestMapping("/api/pacientes/{pacienteId}/cuidadores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Cuidadores", description = "Asignacion de cuidadores a pacientes (solo ADMINISTRADOR)")
public class AsignacionCuidadorController {

    private final GestionarCuidadorUseCase gestionar;

    public AsignacionCuidadorController(GestionarCuidadorUseCase gestionar) {
        this.gestionar = gestionar;
    }

    @GetMapping
    @Operation(summary = "Listar cuidadores con asignacion vigente de un paciente")
    public ResponseEntity<List<AsignacionCuidadorResponse>> listar(@PathVariable Long pacienteId) {
        List<AsignacionCuidadorResponse> respuesta = gestionar.listar(pacienteId).stream()
                .map(PacienteWebMapper::aAsignacion)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    @Operation(summary = "Asignar un cuidador a un paciente")
    public ResponseEntity<AsignacionCuidadorResponse> asignar(
            @PathVariable Long pacienteId,
            @Valid @RequestBody CuidadorRequest peticion,
            Authentication autenticacion) {
        AsignacionCuidador asignacion = gestionar.asignar(
                pacienteId, peticion.usuarioId(), AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteWebMapper.aAsignacion(asignacion));
    }

    @DeleteMapping("/{usuarioId}")
    @Operation(summary = "Desasignar un cuidador (cierra la asignacion: hasta = hoy)")
    public ResponseEntity<Void> desasignar(
            @PathVariable Long pacienteId,
            @PathVariable Long usuarioId,
            Authentication autenticacion) {
        gestionar.desasignar(pacienteId, usuarioId, AlcanceWeb.usuarioId(autenticacion));
        return ResponseEntity.noContent().build();
    }
}
