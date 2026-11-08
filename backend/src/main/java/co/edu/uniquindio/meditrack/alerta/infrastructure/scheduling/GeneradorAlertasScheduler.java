package co.edu.uniquindio.meditrack.alerta.infrastructure.scheduling;

import co.edu.uniquindio.meditrack.alerta.application.GenerarAlertasUseCase;
import co.edu.uniquindio.meditrack.alerta.application.ResultadoGeneracion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Componente {@code @Scheduled} fino que dispara la generacion automatica de
 * alertas. Toda la logica vive en {@link GenerarAlertasUseCase} (POJO testable con
 * reloj fijo); aqui solo se controla la periodicidad.
 *
 * <p>Se activa con {@code meditrack.alertas.habilitado=true} (defecto) y corre cada
 * {@code meditrack.alertas.intervalo-ms} (defecto 60000) tras terminar la ejecucion
 * anterior ({@code fixedDelay}). En las pruebas queda deshabilitado.</p>
 *
 * <p>Una falla inesperada se registra y no se propaga: el ciclo siguiente debe
 * seguir ejecutandose.</p>
 */
@Component
@ConditionalOnProperty(prefix = "meditrack.alertas", name = "habilitado",
        havingValue = "true", matchIfMissing = true)
public class GeneradorAlertasScheduler {

    private static final Logger log = LoggerFactory.getLogger(GeneradorAlertasScheduler.class);

    private final GenerarAlertasUseCase generarAlertas;

    public GeneradorAlertasScheduler(GenerarAlertasUseCase generarAlertas) {
        this.generarAlertas = generarAlertas;
    }

    @Scheduled(
            fixedDelayString = "${meditrack.alertas.intervalo-ms:60000}",
            initialDelayString = "${meditrack.alertas.intervalo-ms:60000}")
    public void ejecutar() {
        try {
            ResultadoGeneracion resultado = generarAlertas.ejecutar();
            log.debug("Generacion de alertas: evaluadas={}, creadas={}, duplicadas={}",
                    resultado.evaluadas(), resultado.creadas(), resultado.duplicadas());
        } catch (RuntimeException excepcion) {
            log.error("Fallo la generacion automatica de alertas; se reintenta en el siguiente ciclo",
                    excepcion);
        }
    }
}
