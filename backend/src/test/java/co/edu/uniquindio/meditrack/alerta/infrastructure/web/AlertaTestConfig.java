package co.edu.uniquindio.meditrack.alerta.infrastructure.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Aporta el bean {@link Clock} a las pruebas de slice web del modulo de alertas,
 * fijado a una fecha concreta (zona del centro).
 */
@TestConfiguration
public class AlertaTestConfig {

    @Bean
    Clock relojPrueba() {
        return Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), ZoneId.of("America/Bogota"));
    }
}
