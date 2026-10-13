package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.application.ActualizarMedicamentoUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.CambiarEstadoMedicamentoUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ComandoMedicamento;
import co.edu.uniquindio.meditrack.medicacion.application.CrearMedicamentoUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ListarMedicamentosUseCase;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;
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
 * Endpoints del catalogo de medicamentos.
 *
 * <p>Lectura: ADMINISTRADOR y CUIDADOR_ENFERMERO. Escritura: solo ADMINISTRADOR.
 * La autorizacion fina por rol se aplica con {@code @PreAuthorize}.</p>
 */
@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos",
        description = "Catalogo de medicamentos (lectura ADMINISTRADOR/CUIDADOR_ENFERMERO; "
                + "escritura solo ADMINISTRADOR)")
public class MedicamentoController {

    private final CrearMedicamentoUseCase crear;
    private final ActualizarMedicamentoUseCase actualizar;
    private final CambiarEstadoMedicamentoUseCase cambiarEstado;
    private final ListarMedicamentosUseCase listar;

    public MedicamentoController(
            CrearMedicamentoUseCase crear,
            ActualizarMedicamentoUseCase actualizar,
            CambiarEstadoMedicamentoUseCase cambiarEstado,
            ListarMedicamentosUseCase listar) {
        this.crear = crear;
        this.actualizar = actualizar;
        this.cambiarEstado = cambiarEstado;
        this.listar = listar;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO')")
    @Operation(summary = "Listar el catalogo de medicamentos (paginado, con busqueda y estado)")
    public PaginaMedicamentosResponse listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        int tamanoAcotado = acotarTamano(tamano);
        List<MedicamentoResponse> contenido = listar.ejecutar(texto, activo, pagina, tamanoAcotado).stream()
                .map(MedicacionWebMapper::aResponse)
                .toList();
        long total = listar.contar(texto, activo);
        int totalPaginas = (int) Math.ceil((double) total / tamanoAcotado);
        return new PaginaMedicamentosResponse(contenido, pagina, tamanoAcotado, total, totalPaginas);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un medicamento en el catalogo (solo ADMINISTRADOR)")
    public ResponseEntity<MedicamentoResponse> crear(
            @Valid @RequestBody MedicamentoRequest request, Authentication autenticacion) {
        Medicamento creado = crear.ejecutar(
                new ComandoMedicamento(request.nombre(), request.presentacion()),
                AlcanceMedicacionWeb.usuarioId(autenticacion));
        return ResponseEntity.status(HttpStatus.CREATED).body(MedicacionWebMapper.aResponse(creado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un medicamento del catalogo (solo ADMINISTRADOR)")
    public MedicamentoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicamentoRequest request,
            Authentication autenticacion) {
        Medicamento actualizado = actualizar.ejecutar(
                id,
                new ComandoMedicamento(request.nombre(), request.presentacion()),
                AlcanceMedicacionWeb.usuarioId(autenticacion));
        return MedicacionWebMapper.aResponse(actualizado);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar un medicamento (solo ADMINISTRADOR; borrado logico)")
    public MedicamentoResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoMedicacionRequest request,
            Authentication autenticacion) {
        Medicamento actualizado = cambiarEstado.ejecutar(
                id, request.activo(), AlcanceMedicacionWeb.usuarioId(autenticacion));
        return MedicacionWebMapper.aResponse(actualizado);
    }

    private static int acotarTamano(int tamano) {
        if (tamano < 1) {
            return 20;
        }
        return Math.min(tamano, 100);
    }
}
