package co.edu.uniquindio.meditrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuracion CORS.
 *
 * <p>Los origenes permitidos se leen de {@code CORS_ALLOWED_ORIGINS} (lista
 * separada por comas). Nunca se usa "*": los origenes permitidos deben ser
 * explicitos.</p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ALLOWED_ORIGINS}") String origenesPermitidos) {

        List<String> origenes = Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();

        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(origenes);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
