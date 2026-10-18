package co.edu.uniquindio.meditrack.cita.infrastructure.web;

import co.edu.uniquindio.meditrack.cita.application.AgendarCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.CambiarEstadoCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.ComandoCita;
import co.edu.uniquindio.meditrack.cita.application.EditarCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.ListarCalendarioCitasUseCase;
import co.edu.uniquindio.meditrack.cita.application.ObtenerCitaUseCase;
import co.edu.uniquindio.meditrack.cita.application.PaginaCitasDominio;
import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.Cita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import co.edu.uniquindio.meditrack.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.Instant;
import java.util.List;

/**
 * Endpoints de las citas medicas de los residentes.
 *
 * <ul>
 *   <li>{@code GET /api/citas}: calendario por rango de fechas, paginado y
 *   filtrable opcionalmente por residente, filtrado por alcance (los tres
 *   roles).</li>
 *   <li>{@code GET /api/pacientes/{pacienteId}/citas/{citaId}}: detalle segun
 *   alcance.</li>
 *   <li>{@code POST /api/pacientes/{pacienteId}/citas}: agendar (solo
 *   ADMINISTRADOR).</li>
 *   <li>{@code PUT /api/pacientes/{pacienteId}/citas/{citaId}}: editar (solo
 *   ADMINISTRADOR, estado inicial).</li>
 *   <li>{@code PATCH /api/pacientes/{pacienteId}/citas/{citaId}/estado}: cambiar
 *   estado (ADMINISTRADOR o CUIDADOR_ENFERMERO; CANCELADA solo
 *   ADMINISTRADOR).</li>
 * </ul>
 *
 * <p>El filtrado por alcance se hace en el backend (404, nunca 403, cuando el
 * residente esta fuera de alcance; en el listado, un residente fuera de alcance
 * devuelve pagina vacia sin revelar su existencia). La autorizacion por rol se
 * aplica aqui con {@code @PreAuthorize} y ademas se refuerza en los casos de
 * uso.</p>
 */
@RestController
@Tag(name = "Citas",
        description = "Calendario de citas medicas, agendamiento, edicion y cambio de estado")
public class CitaController {

    /** Tamano de pagina por defecto y maximo, iguales a los demas listados. */
    private static final int TAMANO_PAGINA_DEFECTO = 20;
    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final AgendarCitaUseCase agendar;
    private final EditarCitaUseCase editar;
    private final CambiarEstadoCitaUseCase cambiarEstado;
    private final ObtenerCitaUseCase obtener;
    private final ListarCalendarioCitasUseCase calendario;
    private final EspecialidadRepositoryPort especialidades;

    public CitaController(
            AgendarCitaUseCase agendar,
            EditarCitaUseCase editar,
            CambiarEstadoCitaUseCase cambiarEstado,
            ObtenerCitaUseCase obtener,
            ListarCalendarioCitasUseCase calendario,
            EspecialidadRepositoryPort especialidades) {
        this.agendar = agendar;
        this.editar = editar;
        this.cambiarEstado = cambiarEstado;
        this.obtener = obtener;
        this.calendario = calendario;
        this.especialidades = especialidades;
    }

    @GetMapping("/api/citas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Calendario de citas paginado por rango de fechas, filtrable por residente (lectura segun alcance)")
    public PageResponse<CitaResponse> calendario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication autenticacion) {
        AlcanceCita alcance = AlcanceCitaWeb.de(autenticacion);
        int tamanoAcotado = acotarTamano(tamano);
        PaginaCitasDominio resultado = calendario.ejecutar(
                desde, hasta, estado, pacienteId, pagina, tamanoAcotado, alcance);
        List<CitaResponse> contenido = resultado.contenido().stream()
                .map(this::aResponse)
                .toList();
        return PageResponse.de(contenido, pagina, tamanoAcotado, resultado.total());
    }

    @GetMapping("/api/pacientes/{pacienteId}/citas/{citaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Detalle de una cita de un residente (lectura segun alcance)")
    public CitaResponse obtener(
            @PathVariable Long pacienteId,
            @PathVariable Long citaId,
            Authentication autenticacion) {
        AlcanceCita alcance = AlcanceCitaWeb.de(autenticacion);
        return aResponse(obtener.ejecutar(pacienteId, citaId, alcance));
    }

    @PostMapping("/api/pacientes/{pacienteId}/citas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Agendar una cita de un residente (solo ADMINISTRADOR)")
    public ResponseEntity<CitaResponse> agendar(
            @PathVariable Long pacienteId,
            @Valid @RequestBody CitaRequest request,
            Authentication autenticacion) {
        AlcanceCita alcance = AlcanceCitaWeb.de(autenticacion);
        Cita creada = agendar.ejecutar(pacienteId, aComando(request), alcance);
        return ResponseEntity.status(HttpStatus.CREATED).body(aResponse(creada));
    }

    @PutMapping("/api/pacientes/{pacienteId}/citas/{citaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Editar una cita en estado inicial (solo ADMINISTRADOR)")
    public CitaResponse editar(
            @PathVariable Long pacienteId,
            @PathVariable Long citaId,
            @Valid @RequestBody CitaRequest request,
            Authentication autenticacion) {
        AlcanceCita alcance = AlcanceCitaWeb.de(autenticacion);
        return aResponse(editar.ejecutar(pacienteId, citaId, aComando(request), alcance));
    }

    @PatchMapping("/api/pacientes/{pacienteId}/citas/{citaId}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO')")
    @Operation(summary = "Cambiar el estado de una cita (ADMINISTRADOR o CUIDADOR_ENFERMERO)")
    public CitaResponse cambiarEstado(
            @PathVariable Long pacienteId,
            @PathVariable Long citaId,
            @Valid @RequestBody CambiarEstadoCitaRequest request,
            Authentication autenticacion) {
        AlcanceCita alcance = AlcanceCitaWeb.de(autenticacion);
        return aResponse(cambiarEstado.ejecutar(pacienteId, citaId, request.estado(), alcance));
    }

    private ComandoCita aComando(CitaRequest request) {
        return new ComandoCita(
                request.especialidadId(),
                request.profesional(),
                request.fechaHora(),
                request.lugar(),
                request.motivo());
    }

    private CitaResponse aResponse(Cita cita) {
        String nombre = cita.getEspecialidadId() == null
                ? null
                : especialidades.nombrePorId(cita.getEspecialidadId()).orElse(null);
        return CitaWebMapper.aResponse(cita, nombre);
    }

    private static int acotarTamano(int tamano) {
        if (tamano < 1) {
            return TAMANO_PAGINA_DEFECTO;
        }
        return Math.min(tamano, TAMANO_PAGINA_MAXIMO);
    }
}