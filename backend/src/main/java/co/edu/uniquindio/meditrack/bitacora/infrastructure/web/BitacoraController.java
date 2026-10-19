package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import co.edu.uniquindio.meditrack.bitacora.application.ComandoSignosVitales;
import co.edu.uniquindio.meditrack.bitacora.application.ListarSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.ObtenerUltimoSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.application.PaginaSignosVitalesDominio;
import co.edu.uniquindio.meditrack.bitacora.application.RegistrarSignosVitalesUseCase;
import co.edu.uniquindio.meditrack.bitacora.domain.AlcanceBitacora;
import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Endpoints de la bitacora de signos vitales de los residentes (Proceso 2).
 *
 * <ul>
 *   <li>{@code POST /api/pacientes/{pacienteId}/signos-vitales}: registrar (solo
 *   ADMINISTRADOR o CUIDADOR_ENFERMERO).</li>
 *   <li>{@code GET /api/pacientes/{pacienteId}/signos-vitales}: historial
 *   paginado con filtros opcionales {@code desde}/{@code hasta}, orden por fecha
 *   descendente (los tres roles, lectura segun alcance).</li>
 *   <li>{@code GET /api/pacientes/{pacienteId}/signos-vitales/ultimo}: registro
 *   mas reciente; 404 si no hay (los tres roles, lectura segun alcance).</li>
 * </ul>
 *
 * <p>El filtrado por alcance se hace en el backend (404, nunca 403, cuando el
 * residente esta fuera de alcance). La autorizacion por rol se aplica aqui con
 * {@code @PreAuthorize} y ademas se refuerza en los casos de uso. Los registros
 * son inmutables: no hay endpoints de edicion ni de borrado.</p>
 */
@RestController
@Tag(name = "Bitacora de signos vitales",
        description = "Registro diario e historial de signos vitales de un residente (presion, glucosa, temperatura)")
public class BitacoraController {

    /** Tamano de pagina por defecto y maximo, iguales a los demas listados. */
    private static final int TAMANO_PAGINA_DEFECTO = 20;
    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final RegistrarSignosVitalesUseCase registrar;
    private final ListarSignosVitalesUseCase historial;
    private final ObtenerUltimoSignosVitalesUseCase ultimo;

    public BitacoraController(
            RegistrarSignosVitalesUseCase registrar,
            ListarSignosVitalesUseCase historial,
            ObtenerUltimoSignosVitalesUseCase ultimo) {
        this.registrar = registrar;
        this.historial = historial;
        this.ultimo = ultimo;
    }

    @PostMapping("/api/pacientes/{pacienteId}/signos-vitales")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO')")
    @Operation(summary = "Registrar signos vitales de un residente (ADMINISTRADOR o CUIDADOR_ENFERMERO)")
    public ResponseEntity<SignosVitalesResponse> registrar(
            @PathVariable Long pacienteId,
            @Valid @RequestBody SignosVitalesRequest request,
            Authentication autenticacion) {
        AlcanceBitacora alcance = AlcanceBitacoraWeb.de(autenticacion);
        RegistroSignosVitales registro = registrar.ejecutar(pacienteId, aComando(request), alcance);
        return ResponseEntity.status(HttpStatus.CREATED).body(SignosVitalesWebMapper.aResponse(registro));
    }

    @GetMapping("/api/pacientes/{pacienteId}/signos-vitales")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Historial paginado de signos vitales, con desde/hasta opcionales, orden por fecha descendente (lectura segun alcance)")
    public PageResponse<SignosVitalesResponse> historial(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication autenticacion) {
        AlcanceBitacora alcance = AlcanceBitacoraWeb.de(autenticacion);
        int tamanoAcotado = acotarTamano(tamano);
        PaginaSignosVitalesDominio resultado =
                historial.ejecutar(pacienteId, desde, hasta, pagina, tamanoAcotado, alcance);
        List<SignosVitalesResponse> contenido = resultado.contenido().stream()
                .map(SignosVitalesWebMapper::aResponse)
                .toList();
        return PageResponse.de(contenido, pagina, tamanoAcotado, resultado.total());
    }

    @GetMapping("/api/pacientes/{pacienteId}/signos-vitales/ultimo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Ultimo registro de signos vitales del residente; 404 si no tiene ninguno (lectura segun alcance)")
    public SignosVitalesResponse ultimo(
            @PathVariable Long pacienteId,
            Authentication autenticacion) {
        AlcanceBitacora alcance = AlcanceBitacoraWeb.de(autenticacion);
        return SignosVitalesWebMapper.aResponse(ultimo.ejecutar(pacienteId, alcance));
    }

    private ComandoSignosVitales aComando(SignosVitalesRequest request) {
        return new ComandoSignosVitales(
                request.fechaHora(),
                request.presionSistolica(),
                request.presionDiastolica(),
                request.glucosaMgDl(),
                request.temperaturaC(),
                request.observaciones());
    }

    private static int acotarTamano(int tamano) {
        if (tamano < 1) {
            return TAMANO_PAGINA_DEFECTO;
        }
        return Math.min(tamano, TAMANO_PAGINA_MAXIMO);
    }
}
