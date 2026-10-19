package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import co.edu.uniquindio.meditrack.bitacora.domain.RegistroSignosVitales;

/**
 * Traduce del modelo de dominio de signos vitales a los DTOs de salida.
 */
final class SignosVitalesWebMapper {

    private SignosVitalesWebMapper() {
        // Utilidad: no se instancia.
    }

    static SignosVitalesResponse aResponse(RegistroSignosVitales registro) {
        return new SignosVitalesResponse(
                registro.getId(),
                registro.getPacienteId(),
                registro.getRegistradoPor(),
                registro.getFechaHora(),
                registro.getPresionSistolica(),
                registro.getPresionDiastolica(),
                registro.getGlucosaMgDl(),
                registro.getTemperaturaC(),
                registro.getObservaciones());
    }
}
