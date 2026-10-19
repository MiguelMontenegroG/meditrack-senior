package co.edu.uniquindio.meditrack.bitacora.application;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Datos de un registro de signos vitales. El autor ({@code registradoPor}) no
 * forma parte del comando: sale del usuario autenticado. {@code fechaHora} es
 * opcional (si falta, el servidor usa el reloj). Todos los signos son opcionales
 * por separado, pero al menos uno debe venir informado (lo valida el dominio).
 */
public record ComandoSignosVitales(
        Instant fechaHora,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer glucosaMgDl,
        BigDecimal temperaturaC,
        String observaciones) {
}
