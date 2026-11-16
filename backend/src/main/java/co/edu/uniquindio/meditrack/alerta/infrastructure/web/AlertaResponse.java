package co.edu.uniquindio.meditrack.alerta.infrastructure.web;

import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.OrigenAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.SeveridadAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;

import java.time.Instant;

/**
 * Vista de una alerta. El mensaje es generico y nunca incluye valores clinicos;
 * el detalle se consulta en la bitacora. Incluye quien y cuando la atendio.
 */
public record AlertaResponse(
        Long id,
        Long pacienteId,
        TipoAlerta tipo,
        SeveridadAlerta severidad,
        String mensaje,
        OrigenAlerta origen,
        Long signosId,
        Long tomaId,
        Long citaId,
        EstadoAlerta estado,
        Instant creadoEn,
        Instant revisadaEn,
        Long revisadaPor) {
}
