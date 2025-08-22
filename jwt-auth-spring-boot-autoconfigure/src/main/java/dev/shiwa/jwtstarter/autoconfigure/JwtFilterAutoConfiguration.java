package dev.shiwa.jwtstarter.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.shiwa.jwtstarter.core.JwtTokenVerifier;
import dev.shiwa.jwtstarter.filter.JwtAuthFilter;

/**
 * Auto-configuration for registering the {@link JwtAuthFilter} as a servlet
 * filter.
 *
 * <p>
 * This configuration is conditionally enabled via the property
 * {@code jwt.filter.enabled=true}. If the property is missing or set to false,
 * the filter will not be registered.
 *
 * <p>
 * The filter ensures that incoming HTTP requests to configured URL patterns are
 * authenticated using a valid JWT token.
 *
 * <p>
 * The URL patterns to secure are configured via
 * {@link JwtAuthProperties#getProtectedPaths()}.
 */
@Configuration
@ConditionalOnProperty(name = "jwt.filter.enabled", havingValue = "true", matchIfMissing = false)
public class JwtFilterAutoConfiguration {

    /**
     * Registers the {@link JwtAuthFilter} to intercept and validate JWT tokens on
     * incoming requests.
     *
     * <p>
     * The filter is applied to all URL patterns defined in
     * {@link JwtAuthProperties#getProtectedPaths()}.
     *
     * <p>
     * This bean will only be created if no other {@link JwtAuthFilter}
     * {@code FilterRegistrationBean} is present in the context.
     *
     * @param verifier the JWT token verifier used to validate tokens
     * @param props    the authentication properties including URL patterns
     * @return a filter registration bean for {@link JwtAuthFilter}
     */
    @Bean
    @ConditionalOnMissingBean
    FilterRegistrationBean<JwtAuthFilter> jwtFilter(JwtTokenVerifier verifier, JwtAuthProperties props) {
	FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
	registration.setFilter(new JwtAuthFilter(verifier, props));
	registration.setOrder(1);

	for (String pattern : props.getProtectedPaths()) {
	    registration.addUrlPatterns(pattern);
	}

	return registration;
    }
}
