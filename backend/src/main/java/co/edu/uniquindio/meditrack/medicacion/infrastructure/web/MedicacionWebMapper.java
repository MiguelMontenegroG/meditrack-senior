package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;

import java.time.LocalDate;
import java.util.List;

/**
 * Traduce del modelo de dominio a los DTOs de salida del modulo medicacion.
 */
final class MedicacionWebMapper {

    private MedicacionWebMapper() {
        // Utilidad: no se instancia.
    }

    static MedicamentoResponse aResponse(Medicamento medicamento) {
        return new MedicamentoResponse(
                medicamento.getId(),
                medicamento.getNombre(),
                medicamento.getPresentacion(),
                medicamento.isActivo());
    }

    static EsquemaResponse aResponse(
            EsquemaMedicacion esquema, String medicamentoNombre, LocalDate hoy) {
        return new EsquemaResponse(
                esquema.getId(),
                esquema.getPacienteId(),
                esquema.getMedicamentoId(),
                medicamentoNombre,
                esquema.getDosis(),
                esquema.getFrecuenciaHoras(),
                esquema.getVia(),
                esquema.getIndicaciones(),
                esquema.getFechaInicio(),
                esquema.getFechaFin(),
                esquema.isActivo(),
                esquema.vigenteEn(hoy),
                List.copyOf(esquema.horas()));
    }

    static RegistroTomaResponse aResponse(RegistroToma toma) {
        return new RegistroTomaResponse(
                toma.getId(),
                toma.getEsquemaId(),
                toma.getRegistradoPor(),
                toma.getFechaHoraProgramada(),
                toma.getFechaHoraRegistro(),
                toma.getEstado(),
                toma.getObservaciones());
    }
}
