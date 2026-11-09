package co.edu.uniquindio.meditrack.reporte.domain;

import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.util.Locale;

/**
 * Tipo de reporte clinico, coincidente con el CHECK {@code chk_reporte_tipo} de
 * la tabla {@code reporte_generado} (migracion V2).
 *
 * <p>Cada tipo determina que secciones del historial del residente se incluyen en
 * el PDF:</p>
 * <ul>
 *   <li>{@code HISTORIA_CLINICA}: cabecera del residente, esquemas de medicacion,
 *   citas y bitacora de signos vitales.</li>
 *   <li>{@code MEDICACION}: cabecera y esquemas de medicacion.</li>
 *   <li>{@code BITACORA}: cabecera, bitacora de signos vitales y citas.</li>
 *   <li>{@code GENERAL}: cabecera y resumen de citas y alertas.</li>
 * </ul>
 *
 * <p>Los reportes se generan siempre sobre un rango de fechas obligatorio (la
 * ley 1581 exige acotar los datos personales a lo estrictamente necesario).</p>
 */
public enum TipoReporte {

    HISTORIA_CLINICA,
    BITACORA,
    MEDICACION,
    GENERAL;

    /**
     * Interpreta el parametro de consulta, sin distinguir mayusculas y admite
     * guion bajo o guion como separador. Un valor desconocido responde 400 con
     * el catalogo completo.
     */
    public static TipoReporte desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionDeNegocioException(
                    "El tipo de reporte es obligatorio; valores: HISTORIA_CLINICA, BITACORA, "
                            + "MEDICACION, GENERAL");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (TipoReporte tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }
        throw new ValidacionDeNegocioException(
                "El tipo de reporte no es valido; valores: HISTORIA_CLINICA, BITACORA, "
                        + "MEDICACION, GENERAL");
    }

    /** Indica si el reporte incluye la seccion de medicacion. */
    public boolean incluyeMedicacion() {
        return this == HISTORIA_CLINICA || this == MEDICACION;
    }

    /** Indica si el reporte incluye la bitacora de signos vitales. */
    public boolean incluyeBitacora() {
        return this == HISTORIA_CLINICA || this == BITACORA;
    }

    /** Indica si el reporte incluye la agenda de citas. */
    public boolean incluyeCitas() {
        return this == HISTORIA_CLINICA || this == BITACORA;
    }

    /** Indica si el reporte incluye el resumen de alertas del periodo. */
    public boolean incluyeAlertas() {
        return this == GENERAL;
    }

    /** Indica si el reporte incluye el resumen de medicacion (no el detalle). */
    public boolean esSoloResumen() {
        return this == GENERAL;
    }
}
