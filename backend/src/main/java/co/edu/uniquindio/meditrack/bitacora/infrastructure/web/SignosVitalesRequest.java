package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Cuerpo de entrada para registrar signos vitales.
 *
 * <p>El autor ({@code registradoPor}) y el residente no forman parte de la
 * peticion: el autor sale del usuario autenticado y el residente va en la ruta.
 * Todos los signos son opcionales por separado, pero al menos uno debe venir
 * informado; los rangos de plausibilidad, la coherencia de la presion y la fecha
 * no futura se validan en el dominio (400). {@code fechaHora} es opcional: si
 * falta, el servidor usa el reloj.</p>
 */
public record SignosVitalesRequest(
        Instant fechaHora,

        Integer presionSistolica,

        Integer presionDiastolica,

        Integer glucosaMgDl,

        BigDecimal temperaturaC,

        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
        String observaciones) {
}
