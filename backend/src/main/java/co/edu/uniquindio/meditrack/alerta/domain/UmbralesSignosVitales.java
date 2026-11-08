package co.edu.uniquindio.meditrack.alerta.domain;

/**
 * Umbrales de referencia de signos vitales usados por la generacion de alertas.
 *
 * <p>Son valores de <b>referencia</b> para disparar una revision, no un
 * diagnostico clinico ni un rango terapeutico: cualquier decision clinica es del
 * personal de salud. Se configuran en {@code meditrack.alertas.umbrales.*}.</p>
 *
 * @param sistolicaMin   minima presion sistolica aceptada (por debajo se alerta)
 * @param sistolicaMax   maxima presion sistolica aceptada (por encima se alerta)
 * @param diastolicaMin  minima presion diastolica aceptada
 * @param diastolicaMax  maxima presion diastolica aceptada
 * @param glucosaMin     minima glucosa aceptada (mg/dL)
 * @param glucosaMax     maxima glucosa aceptada (mg/dL)
 * @param temperaturaMin minima temperatura aceptada (grados C)
 * @param temperaturaMax maxima temperatura aceptada (grados C)
 */
public record UmbralesSignosVitales(
        int sistolicaMin,
        int sistolicaMax,
        int diastolicaMin,
        int diastolicaMax,
        int glucosaMin,
        int glucosaMax,
        double temperaturaMin,
        double temperaturaMax) {

    /**
     * Indica si un registro esta fuera de los umbrales de referencia. Un valor
     * nulo no evalua ese signo. El limite se considera dentro del rango (solo se
     * alerta lo estrictamente fuera).
     */
    public boolean fueraDeRango(
            Integer sistolica, Integer diastolica, Integer glucosa, Double temperatura) {
        return fuera(sistolica, sistolicaMin, sistolicaMax)
                || fuera(diastolica, diastolicaMin, diastolicaMax)
                || fuera(glucosa, glucosaMin, glucosaMax)
                || temperatura != null && (temperatura < temperaturaMin || temperatura > temperaturaMax);
    }

    private static boolean fuera(Number valor, double minimo, double maximo) {
        if (valor == null) {
            return false;
        }
        double v = valor.doubleValue();
        return v < minimo || v > maximo;
    }
}
