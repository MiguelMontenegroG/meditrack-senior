package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.application.ActualizarEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.CambiarEstadoEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ComandoEsquema;
import co.edu.uniquindio.meditrack.medicacion.application.CrearEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ListarEsquemasUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ObtenerEsquemaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
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
 * Endpoints de los esquemas de medicacion de un residente.
 *
 * <p>Base: {@code /api/pacientes/{pacienteId}/esquemas}. Lectura segun alcance
 * (ADMINISTRADOR, CUIDADOR_ENFERMERO y FAMILIAR_AUTORIZADO). Escritura solo
 * ADMINISTRADOR.</p>
 */
@RestController
@RequestMapping("/api/pacientes/{pacienteId}/esquemas")
@Tag(name = "Esquemas de medicacion",
        description = "Tratamientos de un residente con dosis, frecuencia, via y vigencia, "
                + "con sus horarios")
public class EsquemaMedicacionController {

    private final CrearEsquemaUseCase crear;
    private final ActualizarEsquemaUseCase actualizar;
    private final CambiarEstadoEsquemaUseCase cambiarEstado;
    private final ListarEsquemasUseCase listar;
    private final ObtenerEsquemaUseCase obtener;
    private final MedicamentoRepositoryPort medicamentos;
    private final Clock reloj;

    public EsquemaMedicacionController(
            CrearEsquemaUseCase crear,
            ActualizarEsquemaUseCase actualizar,
            CambiarEstadoEsquemaUseCase cambiarEstado,
            ListarEsquemasUseCase listar,
            ObtenerEsquemaUseCase obtener,
            MedicamentoRepositoryPort medicamentos,
            Clock reloj) {
        this.crear = crear;
        this.actualizar = actualizar;
        this.cambiarEstado = cambiarEstado;
        this.listar = listar;
        this.obtener = obtener;
        this.medicamentos = medicamentos;
        this.reloj = reloj;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Listar los esquemas de un residente (lectura segun alcance)")
    public List<EsquemaResponse> listarEsquemas(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Boolean vigente,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        LocalDate hoy = LocalDate.now(reloj);
        return listar.ejecutar(pacienteId, activo, vigente, hoy, alcance).stream()
                .map(esquema -> MedicacionWebMapper.aResponse(esquema, nombreMedicamento(esquema), hoy))
                .toList();
    }

    @GetMapping("/{esquemaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Detalle de un esquema de un residente (lectura segun alcance)")
    public EsquemaResponse obtenerEsquema(
            @PathVariable Long pacienteId,
            @PathVariable Long esquemaId,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        LocalDate hoy = LocalDate.now(reloj);
        EsquemaMedicacion esquema = obtener.ejecutar(pacienteId, esquemaId, alcance);
        return MedicacionWebMapper.aResponse(esquema, nombreMedicamento(esquema), hoy);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un esquema de medicacion con sus horarios (solo ADMINISTRADOR)")
    public ResponseEntity<EsquemaResponse> crearEsquema(
            @PathVariable Long pacienteId,
            @Valid @RequestBody EsquemaRequest request,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        EsquemaMedicacion creado = crear.ejecutar(pacienteId, aComando(request), alcance);
        LocalDate hoy = LocalDate.now(reloj);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MedicacionWebMapper.aResponse(creado, nombreMedicamento(creado), hoy));
    }

    @PutMapping("/{esquemaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un esquema y reemplazar sus horarios (solo ADMINISTRADOR)")
    public EsquemaResponse actualizarEsquema(
            @PathVariable Long pacienteId,
            @PathVariable Long esquemaId,
            @Valid @RequestBody EsquemaRequest request,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        EsquemaMedicacion actualizado = actualizar.ejecutar(pacienteId, esquemaId, aComando(request), alcance);
        LocalDate hoy = LocalDate.now(reloj);
        return MedicacionWebMapper.aResponse(actualizado, nombreMedicamento(actualizado), hoy);
    }

    @PatchMapping("/{esquemaId}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o suspender un esquema (solo ADMINISTRADOR; borrado logico)")
    public EsquemaResponse cambiarEstadoEsquema(
            @PathVariable Long pacienteId,
            @PathVariable Long esquemaId,
            @Valid @RequestBody CambiarEstadoMedicacionRequest request,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        EsquemaMedicacion actualizado = cambiarEstado.ejecutar(pacienteId, esquemaId, request.activo(), alcance);
        LocalDate hoy = LocalDate.now(reloj);
        return MedicacionWebMapper.aResponse(actualizado, nombreMedicamento(actualizado), hoy);
    }

    private ComandoEsquema aComando(EsquemaRequest request) {
        return new ComandoEsquema(
                request.medicamentoId(),
                request.dosis(),
                request.frecuenciaHoras(),
                request.via(),
                request.indicaciones(),
                request.fechaInicio(),
                request.fechaFin(),
                request.horarios());
    }

    private String nombreMedicamento(EsquemaMedicacion esquema) {
        return medicamentos.buscarPorId(esquema.getMedicamentoId())
                .map(m -> m.getNombre())
                .orElse(null);
    }
}
