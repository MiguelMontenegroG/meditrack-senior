package co.edu.uniquindio.meditrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuracion de seguridad: API stateless protegida con JWT.
 *
 * <p>Rutas publicas: POST /api/auth/login, /actuator/health y Swagger (solo dev).
 * Todo lo demas exige un JWT valido. La autorizacion fina por rol se expresa
 * con {@code @PreAuthorize} en los controladores (method security).</p>
 *
 * <p>Los errores de seguridad (401 y 403) se devuelven en JSON con el formato
 * de {@link co.edu.uniquindio.meditrack.shared.infrastructure.web.ApiError}.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] SWAGGER = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HandlersSeguridad.AutenticacionEntryPoint authenticationEntryPoint;
    private final HandlersSeguridad.AccesoDenegadoHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${meditrack.swagger-habilitado:false}")
    private boolean swaggerHabilitado;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            HandlersSeguridad.AutenticacionEntryPoint authenticationEntryPoint,
            HandlersSeguridad.AccesoDenegadoHandler accessDeniedHandler,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion ->
                        sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/login").permitAll();
                    auth.requestMatchers("/actuator/health").permitAll();
                    if (swaggerHabilitado) {
                        auth.requestMatchers(SWAGGER).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion)
            throws Exception {
        return configuracion.getAuthenticationManager();
    }
}
