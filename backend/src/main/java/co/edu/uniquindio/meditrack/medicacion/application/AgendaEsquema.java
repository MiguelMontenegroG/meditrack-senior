package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.domain.CalendarioTomas;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;

import java.util.List;

/**
 * Agenda calculada de un esquema para un dia: el esquema y los slots de toma
 * con su estado derivado (PENDIENTE, ATRASADA, ADMINISTRADA u OMITIDA).
 */
public record AgendaEsquema(
        EsquemaMedicacion esquema,
        List<CalendarioTomas.SlotToma> slots) {
}
