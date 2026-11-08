package co.edu.uniquindio.meditrack.alerta.infrastructure.web;

import co.edu.uniquindio.meditrack.alerta.application.AtenderAlertaUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ListarAlertasUseCase;
import co.edu.uniquindio.meditrack.alerta.domain.AlcanceAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import co.edu.uniquindio.meditrack.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de las alertas de seguimiento de los residentes (Proceso 3).
 *
 * <ul>
 *   <li>{@code GET /api/alertas}: listado paginado con filtros opcionales
 *   {@code estado}, {@code tipo} y {@code pacienteId} (los tres roles, lectura
 *   segun alcance). Un residente fuera de alcance devuelve pagina vacia.</li>
 *   <li>{@code PATCH /api/alertas/{id}/atender}: atender una alerta
 *   (ADMINISTRADOR o CUIDADOR_ENFERMERO; el familiar recibe 403; fuera de alcance
 *   404; ya atendida 409).</li>
 * </ul>
 *
 * <p>El filtrado por alcance se hace en el backend (404, nunca 403, cuando el
 * residente esta fuera de alcance; en el listado, pagina vacia sin revelar su
 * existencia). Las alertas no se borran.</p>
 */
@RestController
@Tag(name = "Alertas",
        description = "Alertas automaticas de seguimiento (tomas atrasadas, citas vencidas y signos fuera de rango) y su atencion")
public class AlertaController {

    /** Tamano de pagina por defecto y maximo, iguales a los demas listados. */
    private static final int TAMANO_PAGINA_DEFECTO = 20;
    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final ListarAlertasUseCase listar;
    private final AtenderAlertaUseCase atender;

    public AlertaController(ListarAlertasUseCase listar, AtenderAlertaUseCase atender) {
        this.listar = listar;
        this.atender = atender;
    }

    @GetMapping("/api/alertas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO', 'FAMILIAR_AUTORIZADO')")
    @Operation(summary = "Listado paginado de alertas con filtros opcionales estado, tipo y pacienteId (lectura segun alcance)")
    public PageResponse<AlertaResponse> listar(
            @RequestParam(required = false) EstadoAlerta estado,
            @RequestParam(required = false) TipoAlerta tipo,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication autenticacion) {
        AlcanceAlerta alcance = AlcanceAlertaWeb.de(autenticacion);
        int tamanoAcotado = acotarTamano(tamano);
        PaginaAlertasDominio resultado =
                listar.ejecutar(estado, tipo, pacienteId, pagina, tamanoAcotado, alcance);
        List<AlertaResponse> contenido = resultado.contenido().stream()
                .map(AlertaWebMapper::aResponse)
                .toList();
        return PageResponse.de(contenido, pagina, tamanoAcotado, resultado.total());
    }

    @PatchMapping("/api/alertas/{id}/atender")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CUIDADOR_ENFERMERO')")
    @Operation(summary = "Atender una alerta (ADMINISTRADOR o CUIDADOR_ENFERMERO); 409 si ya estaba atendida")
    public AlertaResponse atender(
            @PathVariable Long id,
            Authentication autenticacion) {
        AlcanceAlerta alcance = AlcanceAlertaWeb.de(autenticacion);
        return AlertaWebMapper.aResponse(atender.ejecutar(id, alcance));
    }

    private static int acotarTamano(int tamano) {
        if (tamano < 1) {
            return TAMANO_PAGINA_DEFECTO;
        }
        return Math.min(tamano, TAMANO_PAGINA_MAXIMO);
    }
}
