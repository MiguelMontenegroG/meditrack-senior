package co.edu.uniquindio.meditrack.alerta.application;

/**
 * Configuracion de la generacion automatica de alertas, leida de las propiedades
 * {@code meditrack.alertas.*} en {@code alerta/config}.
 *
 * <p>Los umbrales de signos vitales son de <b>referencia</b>, no diagnostico.</p>
 *
 * @param toleranciaTomaMin   minutos transcurridos desde la hora programada de una
 *                            toma sin registro para considerarla atrasada
 * @param toleranciaCitaMin   minutos transcurridos desde la hora de una cita
 *                            PROGRAMADA para considerarla vencida
 * @param ventanaSignosHoras  horas hacia atras a revisar en la bitacora
 * @param umbrales            umbrales de referencia de signos vitales
 */
public record ConfiguracionAlertas(
        long toleranciaTomaMin,
        long toleranciaCitaMin,
        long ventanaSignosHoras,
        Umbrales umbrales) {

    public ConfiguracionAlertas {
        if (toleranciaTomaMin < 0 || toleranciaCitaMin < 0 || ventanaSignosHoras <= 0) {
            throw new IllegalArgumentException("La configuracion de alertas no puede ser negativa ni cero");
        }
    }

    /** Umbrales de referencia de signos vitales (no diagnostico). */
    public record Umbrales(
            int sistolicaMin,
            int sistolicaMax,
            int diastolicaMin,
            int diastolicaMax,
            int glucosaMin,
            int glucosaMax,
            double temperaturaMin,
            double temperaturaMax) {
    }
}
