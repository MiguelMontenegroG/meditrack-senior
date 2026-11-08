package co.edu.uniquindio.meditrack.alerta.config;

import co.edu.uniquindio.meditrack.alerta.application.ConfiguracionAlertas;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de {@code meditrack.alertas.*}.
 *
 * <p>Defectos: intervalo 60 s, generacion habilitada, tolerancia de toma 30 min,
 * de cita 60 min y umbrales de referencia (sistolica 90-180, diastolica 50-110,
 * glucosa 70-250, temperatura 35.0-38.0). Los umbrales son de <b>referencia</b>,
 * no diagnostico. En las pruebas la generacion queda deshabilitada
 * ({@code application-test.yml}).</p>
 */
@ConfigurationProperties(prefix = "meditrack.alertas")
public class PropiedadesAlertas {

    private boolean habilitado = true;
    private long intervaloMs = 60_000;
    private long toleranciaTomaMin = 30;
    private long toleranciaCitaMin = 60;
    private long ventanaSignosHoras = 24;
    private Umbrales umbrales = new Umbrales();

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public long getIntervaloMs() {
        return intervaloMs;
    }

    public void setIntervaloMs(long intervaloMs) {
        this.intervaloMs = intervaloMs;
    }

    public long getToleranciaTomaMin() {
        return toleranciaTomaMin;
    }

    public void setToleranciaTomaMin(long toleranciaTomaMin) {
        this.toleranciaTomaMin = toleranciaTomaMin;
    }

    public long getToleranciaCitaMin() {
        return toleranciaCitaMin;
    }

    public void setToleranciaCitaMin(long toleranciaCitaMin) {
        this.toleranciaCitaMin = toleranciaCitaMin;
    }

    public long getVentanaSignosHoras() {
        return ventanaSignosHoras;
    }

    public void setVentanaSignosHoras(long ventanaSignosHoras) {
        this.ventanaSignosHoras = ventanaSignosHoras;
    }

    public Umbrales getUmbrales() {
        return umbrales;
    }

    public void setUmbrales(Umbrales umbrales) {
        this.umbrales = umbrales;
    }

    /** Traduce las propiedades a la configuracion de dominio del caso de uso. */
    public ConfiguracionAlertas aConfiguracion() {
        return new ConfiguracionAlertas(
                toleranciaTomaMin,
                toleranciaCitaMin,
                ventanaSignosHoras,
                new ConfiguracionAlertas.Umbrales(
                        umbrales.getSistolicaMin(), umbrales.getSistolicaMax(),
                        umbrales.getDiastolicaMin(), umbrales.getDiastolicaMax(),
                        umbrales.getGlucosaMin(), umbrales.getGlucosaMax(),
                        umbrales.getTemperaturaMin(), umbrales.getTemperaturaMax()));
    }

    /** Umbrales de referencia de signos vitales (no diagnostico). */
    public static class Umbrales {
        private int sistolicaMin = 90;
        private int sistolicaMax = 180;
        private int diastolicaMin = 50;
        private int diastolicaMax = 110;
        private int glucosaMin = 70;
        private int glucosaMax = 250;
        private double temperaturaMin = 35.0;
        private double temperaturaMax = 38.0;

        public int getSistolicaMin() {
            return sistolicaMin;
        }

        public void setSistolicaMin(int sistolicaMin) {
            this.sistolicaMin = sistolicaMin;
        }

        public int getSistolicaMax() {
            return sistolicaMax;
        }

        public void setSistolicaMax(int sistolicaMax) {
            this.sistolicaMax = sistolicaMax;
        }

        public int getDiastolicaMin() {
            return diastolicaMin;
        }

        public void setDiastolicaMin(int diastolicaMin) {
            this.diastolicaMin = diastolicaMin;
        }

        public int getDiastolicaMax() {
            return diastolicaMax;
        }

        public void setDiastolicaMax(int diastolicaMax) {
            this.diastolicaMax = diastolicaMax;
        }

        public int getGlucosaMin() {
            return glucosaMin;
        }

        public void setGlucosaMin(int glucosaMin) {
            this.glucosaMin = glucosaMin;
        }

        public int getGlucosaMax() {
            return glucosaMax;
        }

        public void setGlucosaMax(int glucosaMax) {
            this.glucosaMax = glucosaMax;
        }

        public double getTemperaturaMin() {
            return temperaturaMin;
        }

        public void setTemperaturaMin(double temperaturaMin) {
            this.temperaturaMin = temperaturaMin;
        }

        public double getTemperaturaMax() {
            return temperaturaMax;
        }

        public void setTemperaturaMax(double temperaturaMax) {
            this.temperaturaMax = temperaturaMax;
        }
    }
}
