package dev.shiwa.jwtstarter.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuration class for customizing the OpenAPI (Swagger) documentation.
 *
 * <p>
 * This class sets general API metadata like title, description, version,
 * contact information, and configures a bearer token security scheme used for
 * JWT-protected endpoints.
 * </p>
 *
 * <p>
 * Used by SpringDoc to auto-generate Swagger UI and OpenAPI definitions.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the base OpenAPI specification for the application.
     *
     * <p>
     * Includes basic metadata and a security scheme for JWT-based authentication.
     * </p>
     *
     * @return the configured OpenAPI bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
	final String securitySchemeName = "bearerAuth";

	return new OpenAPI()
		.info(new Info().title("JWT Starter Demo API").version("1.0")
			.description("Demo-Endpunkte zur JWT-Verifikation"))
		.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
		.components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
			.name("Authorization").type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}