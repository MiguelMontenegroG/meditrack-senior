package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import co.edu.uniquindio.meditrack.medicacion.domain.ViaAdministracion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Cuerpo de entrada para crear o actualizar un esquema de medicacion, incluidos
 * sus horarios del dia.
 */
public record EsquemaRequest(
        @NotNull(message = "El medicamento es obligatorio")
        Long medicamentoId,

        @NotBlank(message = "La dosis es obligatoria")
        @Size(max = 50, message = "La dosis no puede superar 50 caracteres")
        String dosis,

        @Min(value = 1, message = "La frecuencia minima es 1 hora")
        @Max(value = 24, message = "La frecuencia maxima es 24 horas")
        int frecuenciaHoras,

        @NotNull(message = "La via de administracion es obligatoria")
        ViaAdministracion via,

        String indicaciones,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        LocalDate fechaFin,

        @NotEmpty(message = "El esquema debe tener al menos un horario")
        List<LocalTime> horarios) {
}
