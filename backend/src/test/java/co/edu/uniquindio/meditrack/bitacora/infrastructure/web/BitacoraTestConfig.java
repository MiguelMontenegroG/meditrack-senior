package co.edu.uniquindio.meditrack.bitacora.infrastructure.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Aporta el bean {@link Clock} a las pruebas de slice web del modulo de bitacora,
 * fijado a una fecha concreta para que las reglas basadas en tiempo sean
 * deterministas.
 */
@TestConfiguration
public class BitacoraTestConfig {

    @Bean
    Clock relojPrueba() {
        return Clock.fixed(Instant.parse("2026-02-10T12:00:00Z"), ZoneOffset.UTC);
    }
}