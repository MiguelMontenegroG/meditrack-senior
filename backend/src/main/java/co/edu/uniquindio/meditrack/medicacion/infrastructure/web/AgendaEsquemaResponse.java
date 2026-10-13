package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import java.time.Instant;
import java.util.List;

/**
 * Agenda de un esquema para un dia: el resumen del esquema y sus slots de toma
 * con estado derivado.
 */
public record AgendaEsquemaResponse(
        Long esquemaId,
        Long medicamentoId,
        String medicamentoNombre,
        String dosis,
        List<SlotTomaResponse> slots) {

    /**
     * Slot de toma calculado (hora programada y estado derivado).
     */
    public record SlotTomaResponse(Instant fechaHoraProgramada, String estado) {
    }
}
