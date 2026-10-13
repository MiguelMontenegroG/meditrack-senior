package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.application.AgendaEsquema;
import co.edu.uniquindio.meditrack.medicacion.application.ComandoRegistrarToma;
import co.edu.uniquindio.meditrack.medicacion.application.ConsultarAgendaDiaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.ListarHistorialTomasUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.RegistrarTomaUseCase;
import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de la agenda, el historial y el registro de tomas de un residente.
 *
 * <p>Base: {@code /api/pacientes/{pacienteId}}. El registro de una toma es un
 * subrecurso del esquema al que pertenece:
 * {@code POST /api/pacientes/{pacienteId}/esquemas/{esquemaId}/tomas}. La agenda
 * del dia y el historial son agregados a nivel del residente (varios esquemas),
 * por eso cuelgan de {@code /tomas} con {@code esquemaId} opcional.</p>
 *
 * <p>El registro de tomas lo realizan ADMINISTRADOR y CUIDADOR_ENFERMERO (segun
 * alcance). El historial y la agenda son de lectura para los tres roles (segun
 * alcance).</p>
 */
@RestController
@RequestMapping("/api/pacientes/{pacienteId}")
@Tag(name = "Tomas",
        description = "Agenda del dia, historial y registro de tomas "
                + "(pendiente/atrasado calculados, nunca persistidos)")
public class TomaController {

    private final RegistrarTomaUseCase registrar;
    private final ConsultarAgendaDiaUseCase agenda;
    private final ListarHistorialTomasUseCase historial;
    private final MedicamentoRepositoryPort medicamentos;
    private final Clock reloj;

    public TomaController(
            RegistrarTomaUseCase registrar,
            ConsultarAgendaDiaUseCase agenda,
            ListarHistorialTomasUseCase historial,
            MedicamentoRepositoryPort medicamentos,
            Clock reloj) {
        this.registrar = registrar;
        this.agenda = agenda;
        this.historial = historial;
        this.medicamentos = medicamentos;
        this.reloj = reloj;
    }

    @GetMapping("/tomas/agenda")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Agenda de medicacion del dia de un residente (pendiente/atrasado calculados)")
    public List<AgendaEsquemaResponse> agenda(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) LocalDate fecha,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        LocalDate dia = fecha == null ? LocalDate.now(reloj) : fecha;
        return agenda.ejecutar(pacienteId, dia, alcance).stream()
                .map(this::aResponse)
                .toList();
    }

    @GetMapping("/tomas/historial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Historial de tomas de un residente, paginado y filtrable (lectura segun alcance)")
    public PaginaTomasResponse historial(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) Long esquemaId,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        int tamanoAcotado = acotarTamano(tamano);
        List<RegistroTomaResponse> contenido = historial
                .ejecutar(pacienteId, esquemaId, desde, hasta, pagina, tamanoAcotado, alcance).stream()
                .map(MedicacionWebMapper::aResponse)
                .toList();
        long total = historial.contar(pacienteId, esquemaId, desde, hasta, alcance);
        int totalPaginas = (int) Math.ceil((double) total / tamanoAcotado);
        return new PaginaTomasResponse(contenido, pagina, tamanoAcotado, total, totalPaginas);
    }

    @PostMapping("/esquemas/{esquemaId}/tomas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO')")
    @Operation(summary = "Registrar la toma de un esquema (ADMINISTRADOR o CUIDADOR_ENFERMERO)")
    public ResponseEntity<RegistroTomaResponse> registrarToma(
            @PathVariable Long pacienteId,
            @PathVariable Long esquemaId,
            @Valid @RequestBody RegistrarTomaRequest request,
            Authentication autenticacion) {
        AlcanceMedicacion alcance = AlcanceMedicacionWeb.de(autenticacion);
        RegistroToma registro = registrar.ejecutar(
                pacienteId,
                esquemaId,
                new ComandoRegistrarToma(
                        request.fechaHoraProgramada(), request.estado(), request.observaciones()),
                alcance);
        return ResponseEntity.status(HttpStatus.CREATED).body(MedicacionWebMapper.aResponse(registro));
    }

    private AgendaEsquemaResponse aResponse(AgendaEsquema agendaEsquema) {
        EsquemaMedicacion esquema = agendaEsquema.esquema();
        List<AgendaEsquemaResponse.SlotTomaResponse> slots = agendaEsquema.slots().stream()
                .map(slot -> new AgendaEsquemaResponse.SlotTomaResponse(
                        slot.fechaHoraProgramada(), slot.estado().name()))
                .toList();
        return new AgendaEsquemaResponse(
                esquema.getId(),
                esquema.getMedicamentoId(),
                nombreMedicamento(esquema),
                esquema.getDosis(),
                slots);
    }

    private String nombreMedicamento(EsquemaMedicacion esquema) {
        return medicamentos.buscarPorId(esquema.getMedicamentoId())
                .map(m -> m.getNombre())
                .orElse(null);
    }

    private static int acotarTamano(int tamano) {
        if (tamano < 1) {
            return 20;
        }
        return Math.min(tamano, 100);
    }
}