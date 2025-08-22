package dev.shiwa.jwtstarter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import dev.shiwa.jwtstarter.core.JwtTokenGenerator;
import dev.shiwa.jwtstarter.core.JwtTokenVerifier;

/**
 * Auto-configuration class for JWT token verification.
 *
 * <p>
 * This configuration registers a {@link JwtTokenVerifier} and
 * {@link JwtTokenGenerator} bean if none are already defined in the Spring
 * context. It is only activated if {@link JwtTokenVerifier} is present on the
 * classpath and {@link JwtAuthProperties} is available as a configuration
 * properties bean.
 *
 * <p>
 * The {@code secret}, TTL, and other JWT-related settings are read from
 * {@link JwtAuthProperties}, which is populated via application configuration
 * (e.g., {@code application.yml}).
 *
 * <p>
 * This class enables convenient use of JWT generation and verification in
 * applications without requiring manual setup.
 */
@AutoConfiguration
@ConditionalOnClass(JwtTokenVerifier.class)
@EnableConfigurationProperties(JwtAuthProperties.class)
public class JwtAutoConfiguration {

    /**
     * Registers a default {@link JwtTokenVerifier} bean if none exists in the
     * application context.
     *
     * <p>
     * The verifier uses the configured secret and validation settings from
     * {@link JwtAuthProperties}.
     *
     * @param properties the JWT authentication properties containing the secret
     * @return a {@link JwtTokenVerifier} initialized with the configured secret
     */
    @ConditionalOnMissingBean
    @Bean
    JwtTokenVerifier jwtTokenVerifier(JwtAuthProperties properties) {
	return new JwtTokenVerifier(properties);
    }

    /**
     * Registers a default {@link JwtTokenGenerator} bean if none exists in the
     * application context.
     *
     * <p>
     * The generator uses the issuer, secret, and TTL values defined in
     * {@link JwtAuthProperties} to create JWT tokens.
     *
     * @param properties the JWT authentication properties containing generation
     *                   config
     * @return a {@link JwtTokenGenerator} initialized with the application settings
     */
    @ConditionalOnMissingBean
    @Bean
    JwtTokenGenerator jwtTokenGenerator(JwtAuthProperties properties) {
	return new JwtTokenGenerator(properties);
    }
}