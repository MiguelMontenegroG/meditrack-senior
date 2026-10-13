package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Aporta el bean {@link Clock} a las pruebas de slice web del modulo
 * medicacion, fijado a una fecha concreta para que la vigencia y la agenda sean
 * deterministas.
 */
@TestConfiguration
public class MedicacionTestConfig {

    @Bean
    Clock relojPrueba() {
        return Clock.fixed(Instant.parse("2026-01-15T12:00:00Z"), ZoneOffset.UTC);
    }
}
