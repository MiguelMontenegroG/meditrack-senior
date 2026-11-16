package co.edu.uniquindio.meditrack.alerta.infrastructure.web;

import co.edu.uniquindio.meditrack.alerta.domain.Alerta;

/**
 * Traduce del modelo de dominio de alertas al DTO de salida.
 */
final class AlertaWebMapper {

    private AlertaWebMapper() {
        // Utilidad: no se instancia.
    }

    static AlertaResponse aResponse(Alerta alerta) {
        return new AlertaResponse(
                alerta.getId(),
                alerta.getPacienteId(),
                alerta.getTipo(),
                alerta.getSeveridad(),
                alerta.getMensaje(),
                alerta.getOrigen(),
                alerta.getSignosId(),
                alerta.getTomaId(),
                alerta.getCitaId(),
                alerta.getEstado(),
                alerta.getCreadoEn(),
                alerta.getRevisadaEn(),
                alerta.getRevisadaPor());
    }
}
