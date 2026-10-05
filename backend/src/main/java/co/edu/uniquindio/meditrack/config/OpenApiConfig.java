package co.edu.uniquindio.meditrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de OpenAPI/Swagger.
 *
 * <p>Define el esquema de seguridad Bearer para poder probar la API con el boton
 * "Authorize" de Swagger UI. Swagger solo se expone en el perfil dev.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI meditrackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediTrack Senior API")
                        .description("API REST del backend de MediTrack Senior (solo dev)")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER))
                .components(new Components().addSecuritySchemes(
                        ESQUEMA_BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
