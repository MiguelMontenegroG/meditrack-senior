package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.application.ListarEspecialidadesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de solo lectura del catalogo de especialidades medicas, que el
 * administrador usa para elegir la especialidad al agendar una cita.
 */
@RestController
@RequestMapping("/api/especialidades")
@Tag(name = "Especialidades", description = "Catalogo de especialidades medicas (solo lectura)")
public class EspecialidadController {

    private final ListarEspecialidadesUseCase listar;

    public EspecialidadController(ListarEspecialidadesUseCase listar) {
        this.listar = listar;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Listar el catalogo de especialidades medicas")
    public List<EspecialidadResponse> listar() {
        return listar.ejecutar().stream()
                .map(CitaWebMapper::aResponse)
                .toList();
    }
}
